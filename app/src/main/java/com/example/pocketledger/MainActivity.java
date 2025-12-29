package com.example.pocketledger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvTotalBalance, tvIncome, tvExpense, tvBudgetLeft;
    private RecyclerView recyclerView;
    private View layoutFabAdd;
    private LinearProgressIndicator budgetProgress;
    private LinearLayout layoutStats;
    private DatabaseHelper dbHelper;
    private TransactionAdapter adapter;

    private static final String PREFS_NAME = "PocketLedgerPrefs";
    private static final String KEY_BUDGET = "monthly_budget";
    private static final double DEFAULT_BUDGET = 3000.0;

    private double monthlyBudget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = DatabaseHelper.getInstance(this);

        // Load saved budget
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        monthlyBudget = Double.longBitsToDouble(prefs.getLong(KEY_BUDGET, Double.doubleToLongBits(DEFAULT_BUDGET)));

        initViews();
        setupRecyclerView();

        layoutFabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.cardBudget).setOnClickListener(v -> showSetBudgetDialog());

        findViewById(R.id.cardDash).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.tvDataManage).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DataManageActivity.class);
            startActivity(intent);
        });

        // Navigation - smooth transitions
        findViewById(R.id.tvNavDiary).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DiaryListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });

        findViewById(R.id.tvNavAi).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AiAssistantActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        findViewById(R.id.btnAi).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AiAssistantActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void initViews() {
        tvTotalBalance = findViewById(R.id.tvTotalBalance);
        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        tvBudgetLeft = findViewById(R.id.tvBudgetLeft);
        budgetProgress = findViewById(R.id.budgetProgress);
        layoutStats = findViewById(R.id.layoutStats);
        recyclerView = findViewById(R.id.recyclerView);
        layoutFabAdd = findViewById(R.id.layoutFabAdd);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(new ArrayList<>(), this::showDeleteDialog);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboard();
    }

    private void loadDashboard() {
        try {
            double income = dbHelper.getMonthlyIncome();
            double expense = dbHelper.getMonthlyExpense();
            double balance = income - expense;

            tvIncome.setText(String.format(Locale.CHINA, "¥ %.2f", income));
            tvExpense.setText(String.format(Locale.CHINA, "¥ %.2f", expense));
            tvTotalBalance.setText(String.format(Locale.CHINA, "¥ %.2f", balance));

            double currentMonthExpense = dbHelper.getMonthlyExpense();
            double remaining = monthlyBudget - currentMonthExpense;
            tvBudgetLeft.setText(String.format(Locale.CHINA, "剩余 ¥ %.2f", remaining));

            int progress = (int) ((currentMonthExpense / monthlyBudget) * 100);
            budgetProgress.setProgress(Math.min(progress, 100));

            if (progress > 90) {
                budgetProgress.setIndicatorColor(ContextCompat.getColor(this, R.color.expense_red));
            } else {
                budgetProgress.setIndicatorColor(ContextCompat.getColor(this, R.color.primary));
            }

            updateCategoryStats();

            List<Transaction> list = dbHelper.getAllTransactions();
            adapter.updateData(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateCategoryStats() {
        layoutStats.removeAllViews();
        List<CategoryStat> stats = dbHelper.getCategoryStats();

        if (stats.isEmpty()) {
            findViewById(R.id.cardAnalysis).setVisibility(View.GONE);
            return;
        }

        findViewById(R.id.cardAnalysis).setVisibility(View.VISIBLE);
        LayoutInflater inflater = LayoutInflater.from(this);

        int[] colors = { R.color.primary, R.color.income_green, R.color.income_yellow,
                R.color.expense_red, R.color.accent };

        int count = 0;
        for (CategoryStat stat : stats) {
            if (count >= 3)
                break;

            View view = inflater.inflate(R.layout.item_category_stat, layoutStats, false);
            TextView tvCategory = view.findViewById(R.id.tvStatCategory);
            TextView tvPercent = view.findViewById(R.id.tvStatPercent);
            View colorDot = view.findViewById(R.id.colorDot);

            tvCategory.setText(stat.getCategory());
            tvPercent.setText(String.format(Locale.CHINA, "%.0f%%", stat.getPercentage()));
            colorDot.setBackgroundTintList(ContextCompat.getColorStateList(this, colors[count % colors.length]));

            layoutStats.addView(view);
            count++;
        }
    }

    private void showSetBudgetDialog() {
        final TextInputEditText input = new TextInputEditText(this);
        input.setHint("输入每月预算金额");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(String.format(Locale.CHINA, "%.0f", monthlyBudget));

        new AlertDialog.Builder(this)
                .setTitle("设置预算")
                .setView(input)
                .setPositiveButton("确定", (dialog, which) -> {
                    if (input.getText() != null) {
                        String value = input.getText().toString();
                        if (!value.isEmpty()) {
                            try {
                                monthlyBudget = Double.parseDouble(value);
                                // Save budget to SharedPreferences
                                getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                                        .edit()
                                        .putLong(KEY_BUDGET, Double.doubleToLongBits(monthlyBudget))
                                        .apply();
                                loadDashboard();
                            } catch (NumberFormatException e) {
                                Toast.makeText(this, "金额格式不正确", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showDeleteDialog(int id) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_msg)
                .setPositiveButton(R.string.delete_btn, (dialog, which) -> {
                    dbHelper.deleteTransaction(id);
                    Toast.makeText(this, R.string.delete_success, Toast.LENGTH_SHORT).show();
                    loadDashboard();
                })
                .setNegativeButton(R.string.cancel_btn, null)
                .show();
    }
}
