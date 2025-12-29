package com.example.pocketledger;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class DataManageActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;
    private static final int CREATE_FILE_REQUEST = 2;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_manage);

        dbHelper = DatabaseHelper.getInstance(this);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnExport).setOnClickListener(v -> exportData());
        findViewById(R.id.btnImport).setOnClickListener(v -> importData());
    }

    private void exportData() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/comma-separated-values");
        intent.putExtra(Intent.EXTRA_TITLE, "pocket_ledger_backup.csv");
        startActivityForResult(intent, CREATE_FILE_REQUEST);
    }

    private void importData() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/comma-separated-values");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null)
            return;

        Uri uri = data.getData();
        if (requestCode == CREATE_FILE_REQUEST) {
            handleExport(uri);
        } else if (requestCode == PICK_FILE_REQUEST) {
            handleImport(uri);
        }
    }

    private void handleExport(Uri uri) {
        try (OutputStream os = getContentResolver().openOutputStream(uri)) {
            List<Transaction> transactions = dbHelper.getAllTransactions();
            StringBuilder csv = new StringBuilder();

            // Adding Byte Order Mark (BOM) for Excel UTF-8 compatibility
            byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
            os.write(bom);

            // Standard Header with Chinese column names
            csv.append("类型,分类,金额,日期,备注\n");

            for (Transaction t : transactions) {
                // Convert internal type to Chinese display name
                String displayType = convertTypeToChineseDisplay(t.getType());

                // Quote values that might contain special characters
                String safeNote = escapeCSVField(t.getNote());
                String safeCategory = escapeCSVField(t.getCategory());

                csv.append(displayType).append(",")
                        .append(safeCategory).append(",")
                        .append(String.format("%.2f", t.getAmount())).append(",")
                        .append(t.getDate()).append(",") // Date in yyyy-MM-dd format
                        .append(safeNote).append("\n");
            }

            os.write(csv.toString().getBytes(StandardCharsets.UTF_8));
            Toast.makeText(this, "导出成功，共 " + transactions.size() + " 条记录", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "导出失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Convert internal type value to Chinese display name
     */
    private String convertTypeToChineseDisplay(String type) {
        if ("income".equals(type)) {
            return "收入";
        } else if ("expense".equals(type)) {
            return "支出";
        }
        return type;
    }

    /**
     * Convert Chinese display name back to internal type value
     */
    private String convertChineseDisplayToType(String displayType) {
        if ("收入".equals(displayType)) {
            return "income";
        } else if ("支出".equals(displayType)) {
            return "expense";
        }
        // Also accept English type names for backward compatibility
        if ("income".equals(displayType) || "expense".equals(displayType)) {
            return displayType;
        }
        return null; // Invalid type
    }

    /**
     * Escape special characters in CSV field according to RFC 4180
     */
    private String escapeCSVField(String field) {
        if (field == null) {
            return "";
        }
        // If field contains comma, quote, or newline, wrap in quotes and escape quotes
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    private void handleImport(Uri uri) {
        int count = 0;
        int errors = 0;
        try (InputStream is = getContentResolver().openInputStream(uri);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;
            int lineNumber = 0;

            // Handle optional BOM in import
            reader.mark(3);
            if (reader.read() != 0xFEFF) {
                reader.reset();
            }

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    try {
                        String typeInput = parts[0].trim();
                        String category = parts[1].trim();
                        String amountStr = parts[2].trim();
                        String date = parts[3].trim();
                        String note = parts.length > 4 ? parts[4].trim() : "";

                        // Convert Chinese display name to internal type, or validate English type
                        String type = convertChineseDisplayToType(typeInput);
                        if (type == null) {
                            errors++;
                            continue;
                        }

                        // Validate and parse amount
                        double amount;
                        try {
                            amount = Double.parseDouble(amountStr);
                            if (amount < 0) {
                                errors++;
                                continue;
                            }
                        } catch (NumberFormatException e) {
                            errors++;
                            continue;
                        }

                        // Validate date format (basic check)
                        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                            errors++;
                            continue;
                        }

                        dbHelper.addTransaction(new Transaction(type, amount, category, note, date));
                        count++;
                    } catch (Exception e) {
                        errors++;
                    }
                } else {
                    errors++;
                }
            }

            String msg = "成功导入 " + count + " 条记录";
            if (errors > 0) {
                msg += "，跳过 " + errors + " 条无效数据";
            }
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "导入失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
