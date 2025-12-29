package com.example.pocketledger;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {

    private RecyclerView rvCategories, rvSubCategories;
    private MaterialCardView cardSubCategories;
    private TextInputEditText etAmount, etNote, etDate;
    private TextView tabExpense, tabIncome;
    private Button btnSave;

    private CategoryAdapter categoryAdapter;
    private SubCategoryAdapter subCategoryAdapter;
    private DatabaseHelper dbHelper;
    private Calendar calendar = Calendar.getInstance();

    private boolean isIncome = false;
    private String currentParentCategory = "其他支出";
    private String selectedCategory = "其他支出";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        dbHelper = DatabaseHelper.getInstance(this);
        initViews();
        setupCategories();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        tabExpense = findViewById(R.id.tabExpense);
        tabIncome = findViewById(R.id.tabIncome);
        rvCategories = findViewById(R.id.rvCategories);
        rvSubCategories = findViewById(R.id.rvSubCategories);
        cardSubCategories = findViewById(R.id.cardSubCategories);
        etAmount = findViewById(R.id.etAmount);
        etNote = findViewById(R.id.etNote);
        etDate = findViewById(R.id.etDate);
        btnSave = findViewById(R.id.btnSave);

        updateDateLabel();
        etDate.setOnClickListener(v -> showDatePicker());

        tabExpense.setOnClickListener(v -> switchTab(false));
        tabIncome.setOnClickListener(v -> switchTab(true));

        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void switchTab(boolean income) {
        this.isIncome = income;
        int activeColor = ContextCompat.getColor(this, R.color.text_main);
        int inactiveColor = ContextCompat.getColor(this, R.color.text_secondary);

        tabIncome.setTypeface(null, income ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        tabIncome.setTextColor(income ? activeColor : inactiveColor);
        tabExpense.setTypeface(null, !income ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        tabExpense.setTextColor(!income ? activeColor : inactiveColor);

        categoryAdapter.setIncomeMode(income);
        categoryAdapter.updateData(income ? getIncomeCategories() : getExpenseCategories());
        cardSubCategories.setVisibility(View.GONE);

        // Reset defaults
        currentParentCategory = income ? "其他收入" : "其他支出";
        selectedCategory = currentParentCategory;
    }

    private void setupCategories() {
        rvCategories.setLayoutManager(new GridLayoutManager(this, 4));
        categoryAdapter = new CategoryAdapter(getExpenseCategories(), (category, pos) -> {
            currentParentCategory = category.getName();
            selectedCategory = category.getName(); // Default to parent
            if (!category.getSubCategories().isEmpty()) {
                showSubCategories(category.getSubCategories());
            } else {
                cardSubCategories.setVisibility(View.GONE);
            }
        });
        rvCategories.setAdapter(categoryAdapter);

        rvSubCategories.setLayoutManager(new GridLayoutManager(this, 4));
        subCategoryAdapter = new SubCategoryAdapter(new ArrayList<>(), subName -> {
            // Logic Fix: Combine Parent and Sub for better statistical grouping
            selectedCategory = currentParentCategory + "-" + subName;
        });
        rvSubCategories.setAdapter(subCategoryAdapter);
    }

    private void showSubCategories(List<String> subs) {
        cardSubCategories.setVisibility(View.VISIBLE);
        subCategoryAdapter.updateData(subs);
    }

    private List<Category> getExpenseCategories() {
        return Arrays.asList(
                new Category("餐饮", android.R.drawable.ic_menu_today, Color.parseColor("#FF7043"),
                        Arrays.asList("快餐", "火锅", "奶茶", "咖啡", "水果", "零食", "买菜")),
                new Category("购物", android.R.drawable.ic_menu_view, Color.parseColor("#42A5F5"),
                        Arrays.asList("服饰", "日用", "电子", "化妆品", "超市", "数码")),
                new Category("交通", android.R.drawable.ic_menu_directions, Color.parseColor("#66BB6A"),
                        Arrays.asList("公交", "打车", "油费", "停车", "地铁", "机票")),
                new Category("住房", android.R.drawable.ic_menu_agenda, Color.parseColor("#FFA726"),
                        Arrays.asList("房租", "水电", "宽带", "物业", "家居", "维修")),
                new Category("娱乐", android.R.drawable.ic_menu_gallery, Color.parseColor("#AB47BC"),
                        Arrays.asList("电影", "游戏", "旅游", "运动", "KTV", "健身")),
                new Category("医疗", android.R.drawable.ic_menu_compass, Color.parseColor("#EF5350"),
                        Arrays.asList("挂号", "药品", "手术", "体检", "牙医", "美容")),
                new Category("学习", android.R.drawable.ic_menu_edit, Color.parseColor("#26A69A"),
                        Arrays.asList("学费", "书籍", "培训", "文具", "订阅", "考证")),
                new Category("其他支出", android.R.drawable.ic_menu_help, Color.parseColor("#78909C"),
                        Arrays.asList("红包", "丢钱", "慈善", "税费", "意外", "其他")));
    }

    private List<Category> getIncomeCategories() {
        return Arrays.asList(
                new Category("职业收入", android.R.drawable.ic_menu_send, Color.parseColor("#FFCA28"),
                        Arrays.asList("工资", "奖金", "补贴", "加班费", "年终奖")),
                new Category("经营收入", android.R.drawable.ic_menu_save, Color.parseColor("#FFEE58"),
                        Arrays.asList("副业", "销售", "服务费", "兼职", "版权")),
                new Category("理财收益", android.R.drawable.ic_menu_sort_alphabetically, Color.parseColor("#D4E157"),
                        Arrays.asList("利息", "股息", "基金", "房租收入", "黄金")),
                new Category("礼金", android.R.drawable.ic_input_add, Color.parseColor("#9CCC65"),
                        Arrays.asList("红包", "压岁钱", "随礼", "中奖")),
                new Category("其他收入", android.R.drawable.ic_menu_help, Color.parseColor("#BDBDBD"),
                        Arrays.asList("退款", "报销", "借款", "变卖", "其他")));
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateLabel();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        etDate.setText(sdf.format(calendar.getTime()));
    }

    private void saveTransaction() {
        if (etAmount.getText() == null || etAmount.getText().toString().isEmpty()) {
            Toast.makeText(this, "请输入金额", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(etAmount.getText().toString());
            String note = etNote.getText() != null ? etNote.getText().toString() : "";
            String date = etDate.getText() != null ? etDate.getText().toString() : "";
            String type = isIncome ? "income" : "expense";

            Transaction transaction = new Transaction(type, amount, selectedCategory, note, date);
            if (dbHelper.addTransaction(transaction)) {
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "金额格式不正确", Toast.LENGTH_SHORT).show();
        }
    }
}
