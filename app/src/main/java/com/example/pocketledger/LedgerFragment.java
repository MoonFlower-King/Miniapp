package com.example.pocketledger;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Fragment for the Ledger (记账) section
 */
public class LedgerFragment extends Fragment {

    private TextView tvTotalBalance, tvIncome, tvExpense, tvBudgetLeft;
    private RecyclerView recyclerView;
    private LinearProgressIndicator budgetProgress;
    private LinearLayout layoutStats;
    private DatabaseHelper dbHelper;
    private TransactionAdapter adapter;

    private static final String PREFS_NAME = "PocketLedgerPrefs";
    private static final String KEY_BUDGET = "monthly_budget";
    private static final double DEFAULT_BUDGET = 3000.0;

    private double monthlyBudget;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = DatabaseHelper.getInstance(requireContext());
        monthlyBudget = requireContext().getSharedPreferences(PREFS_NAME, 0)
                .getLong(KEY_BUDGET, Double.doubleToLongBits(DEFAULT_BUDGET));
        monthlyBudget = Double.longBitsToDouble((long) monthlyBudget);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ledger, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupRecyclerView();
    }

    private void initViews(View view) {
        tvTotalBalance = view.findViewById(R.id.tvTotalBalance);
        tvIncome = view.findViewById(R.id.tvIncome);
        tvExpense = view.findViewById(R.id.tvExpense);
        tvBudgetLeft = view.findViewById(R.id.tvBudgetLeft);
        budgetProgress = view.findViewById(R.id.budgetProgress);
        layoutStats = view.findViewById(R.id.layoutStats);
        recyclerView = view.findViewById(R.id.recyclerView);

        view.findViewById(R.id.layoutFabAdd).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddTransactionActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.cardBudget).setOnClickListener(v -> showSetBudgetDialog());

        view.findViewById(R.id.cardDash).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CalendarActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.tvDataManage).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), DataManageActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.btnAi).setOnClickListener(v -> {
            // Navigate to AI tab (index 2)
            if (getActivity() instanceof MainContainerActivity) {
                ((MainContainerActivity) getActivity()).navigateToTab(2);
            }
        });
    }

    private void setupRecyclerView() {
        List<Transaction> list = dbHelper.getAllTransactions();
        adapter = new TransactionAdapter(list, id -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("删除交易")
                    .setMessage("确定要删除这条记录吗？")
                    .setPositiveButton("删除", (dialog, which) -> {
                        dbHelper.deleteTransaction(id);
                        refreshData();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    private void refreshData() {
        double income = dbHelper.getMonthlyIncome();
        double expense = dbHelper.getMonthlyExpense();
        double balance = income - expense;

        tvTotalBalance.setText(String.format(Locale.CHINA, "¥%.2f", balance));
        tvIncome.setText(String.format(Locale.CHINA, "¥%.2f", income));
        tvExpense.setText(String.format(Locale.CHINA, "¥%.2f", expense));

        double budgetLeft = monthlyBudget - expense;
        tvBudgetLeft.setText(String.format(Locale.CHINA, "¥%.0f", Math.max(0, budgetLeft)));

        int progress = monthlyBudget > 0 ? (int) ((expense / monthlyBudget) * 100) : 0;
        budgetProgress.setProgress(Math.min(progress, 100));

        if (progress > 100) {
            budgetProgress.setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.expense_red));
        } else if (progress > 80) {
            budgetProgress.setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.income_yellow));
        } else {
            budgetProgress.setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.primary));
        }

        adapter.updateData(dbHelper.getAllTransactions());
        updateCategoryStats();
    }

    private void updateCategoryStats() {
        layoutStats.removeAllViews();
        List<CategoryStat> stats = dbHelper.getCategoryStats();
        int[] colors = { R.color.primary, R.color.income_green, R.color.income_yellow,
                R.color.expense_red, R.color.accent };

        for (int i = 0; i < Math.min(stats.size(), 5); i++) {
            CategoryStat stat = stats.get(i);
            View statView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_category_stat, layoutStats, false);

            TextView tvCategory = statView.findViewById(R.id.tvStatCategory);
            TextView tvPercent = statView.findViewById(R.id.tvStatPercent);
            View colorDot = statView.findViewById(R.id.colorDot);

            tvCategory.setText(stat.getCategory());
            tvPercent.setText(String.format(Locale.CHINA, "%.0f%%", stat.getPercentage()));
            colorDot.setBackgroundTintList(
                    ContextCompat.getColorStateList(requireContext(), colors[i % colors.length]));

            layoutStats.addView(statView);
        }
    }

    private void showSetBudgetDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_set_budget, null);
        TextInputEditText etBudget = dialogView.findViewById(R.id.etBudget);
        etBudget.setText(String.format(Locale.CHINA, "%.0f", monthlyBudget));

        new AlertDialog.Builder(requireContext())
                .setTitle("设置月预算")
                .setView(dialogView)
                .setPositiveButton("确定", (dialog, which) -> {
                    String input = etBudget.getText().toString();
                    if (!input.isEmpty()) {
                        try {
                            monthlyBudget = Double.parseDouble(input);
                            requireContext().getSharedPreferences(PREFS_NAME, 0)
                                    .edit()
                                    .putLong(KEY_BUDGET, Double.doubleToLongBits(monthlyBudget))
                                    .apply();
                            refreshData();
                            Toast.makeText(requireContext(), "预算已更新", Toast.LENGTH_SHORT).show();
                        } catch (NumberFormatException e) {
                            Toast.makeText(requireContext(), "请输入有效金额", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
