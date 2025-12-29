package com.example.pocketledger;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarActivity extends AppCompatActivity {

    private TextView tvCurrentMonth, tvSelectedDateTitle;
    private GridView calendarGrid;
    private RecyclerView rvDailyRecords;
    private View layoutEmpty;
    private DatabaseHelper dbHelper;
    private TransactionAdapter adapter;

    private Calendar currentCalendar = Calendar.getInstance();
    private Calendar selectedDate = Calendar.getInstance();
    private List<Date> daysOfMonth = new ArrayList<>();
    private Map<String, DailyTotal> monthlySummaries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        dbHelper = DatabaseHelper.getInstance(this);

        initViews();
        setupCalendar();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        tvCurrentMonth = findViewById(R.id.tvCurrentMonth);
        tvSelectedDateTitle = findViewById(R.id.tvSelectedDateTitle);
        calendarGrid = findViewById(R.id.calendarGrid);
        rvDailyRecords = findViewById(R.id.rvDailyRecords);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        findViewById(R.id.btnPrevMonth).setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            setupCalendar();
        });

        findViewById(R.id.btnNextMonth).setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            setupCalendar();
        });

        findViewById(R.id.btnToday).setOnClickListener(v -> {
            currentCalendar = Calendar.getInstance();
            selectedDate = Calendar.getInstance();
            setupCalendar();
        });

        rvDailyRecords.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(new ArrayList<>(), null);
        rvDailyRecords.setAdapter(adapter);

        layoutEmpty.setOnClickListener(v -> showAddSelectionDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            loadDailyRecords();
        }
    }

    private void showAddSelectionDialog() {
        String[] options = { "📝 记一笔 (账单)", "📖 写日记 (生活)", "✅ 加任务 (待办)" };
        new android.app.AlertDialog.Builder(this)
                .setTitle("记录生活点滴")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Transaction
                            startActivity(new android.content.Intent(this, AddTransactionActivity.class));
                            break;
                        case 1: // Diary
                            startActivity(new android.content.Intent(this, AddDiaryActivity.class));
                            break;
                        case 2: // Task
                            showAddTaskDialog();
                            break;
                    }
                })
                .show();
    }

    private void showAddTaskDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
        android.widget.EditText etTitle = dialogView.findViewById(R.id.etTitle);
        android.widget.EditText etDescription = dialogView.findViewById(R.id.etDescription);
        android.widget.EditText etTags = dialogView.findViewById(R.id.etTags);
        android.widget.RadioGroup rgStatus = dialogView.findViewById(R.id.rgStatus);
        android.widget.RadioGroup rgPriority = dialogView.findViewById(R.id.rgPriority);
        TextView tvDueDate = dialogView.findViewById(R.id.tvDueDate);
        TextView btnClearDate = dialogView.findViewById(R.id.btnClearDate);
        View layoutDueDate = dialogView.findViewById(R.id.layoutDueDate);

        final String[] selectedDueDate = { null };

        layoutDueDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new android.app.DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedDueDate[0] = String.format(Locale.CHINA, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                tvDueDate.setText(String.format(Locale.CHINA, "%02d/%02d/%d", month + 1, dayOfMonth, year));
                tvDueDate.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.text_main));
                btnClearDate.setVisibility(View.VISIBLE);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnClearDate.setOnClickListener(v -> {
            selectedDueDate[0] = null;
            tvDueDate.setText("选择截止日期 (可选)");
            tvDueDate.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.text_secondary));
            btnClearDate.setVisibility(View.GONE);
        });

        new android.app.AlertDialog.Builder(this)
                .setTitle("新建任务")
                .setView(dialogView)
                .setPositiveButton("添加", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    if (title.isEmpty()) {
                        android.widget.Toast.makeText(this, "请输入任务名称", android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String description = etDescription.getText().toString().trim();
                    String tags = etTags.getText().toString().trim();

                    String status = TodoItem.STATUS_NOT_STARTED;
                    int statusId = rgStatus.getCheckedRadioButtonId();
                    if (statusId == R.id.rbInProgress)
                        status = TodoItem.STATUS_IN_PROGRESS;
                    else if (statusId == R.id.rbCompleted)
                        status = TodoItem.STATUS_COMPLETED;

                    String priority = TodoItem.PRIORITY_MEDIUM;
                    int priorityId = rgPriority.getCheckedRadioButtonId();
                    if (priorityId == R.id.rbHigh)
                        priority = TodoItem.PRIORITY_HIGH;
                    else if (priorityId == R.id.rbLow)
                        priority = TodoItem.PRIORITY_LOW;

                    String today = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date());

                    TodoItem newItem = new TodoItem(title, description, status, priority,
                            selectedDueDate[0], tags, today, null, null);
                    dbHelper.addTodoItem(newItem);
                    android.widget.Toast.makeText(this, "任务已添加", android.widget.Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void setupCalendar() {
        daysOfMonth.clear();
        Calendar monthIter = (Calendar) currentCalendar.clone();
        monthIter.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = monthIter.get(Calendar.DAY_OF_WEEK);
        int shift = firstDayOfWeek - Calendar.MONDAY;
        if (shift < 0)
            shift += 7;

        monthIter.add(Calendar.DAY_OF_MONTH, -shift);

        for (int i = 0; i < 42; i++) {
            daysOfMonth.add(monthIter.getTime());
            monthIter.add(Calendar.DAY_OF_MONTH, 1);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.CHINA);
        monthlySummaries = dbHelper.getMonthlyDailySummaries(sdf.format(currentCalendar.getTime()));

        tvCurrentMonth.setText(new SimpleDateFormat("yyyy年MM月", Locale.CHINA).format(currentCalendar.getTime()));

        CalendarAdapter calendarAdapter = new CalendarAdapter();
        calendarGrid.setAdapter(calendarAdapter);

        calendarGrid.setOnItemClickListener((parent, view, position, id) -> {
            selectedDate.setTime(daysOfMonth.get(position));
            calendarAdapter.notifyDataSetChanged();
            loadDailyRecords();
        });

        loadDailyRecords();
    }

    private void loadDailyRecords() {
        String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(selectedDate.getTime());
        List<Transaction> records = dbHelper.getTransactionsByDate(dateStr);

        String titleDate = new SimpleDateFormat("MM月dd日", Locale.CHINA).format(selectedDate.getTime());
        String weekDay = new SimpleDateFormat("EEEE", Locale.CHINA).format(selectedDate.getTime());
        tvSelectedDateTitle.setText("记录列表 " + titleDate + " (" + weekDay + ")");

        if (records.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvDailyRecords.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvDailyRecords.setVisibility(View.VISIBLE);
            adapter.updateData(records);
        }
    }

    private class CalendarAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return daysOfMonth.size();
        }

        @Override
        public Object getItem(int position) {
            return daysOfMonth.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(CalendarActivity.this).inflate(R.layout.item_calendar_day, parent,
                        false);
            }

            Date date = daysOfMonth.get(position);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            TextView tvDay = convertView.findViewById(R.id.tvDayNumber);
            TextView tvInc = convertView.findViewById(R.id.tvDayIncome);
            TextView tvExp = convertView.findViewById(R.id.tvDayExpense);
            View layout = convertView.findViewById(R.id.layoutDay);

            tvDay.setText(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));

            if (cal.get(Calendar.MONTH) != currentCalendar.get(Calendar.MONTH)) {
                tvDay.setAlpha(0.2f);
            } else {
                tvDay.setAlpha(1.0f);
            }

            layout.setSelected(isSameDay(cal, selectedDate));

            String dateKey = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(date);
            if (monthlySummaries.containsKey(dateKey)) {
                DailyTotal dt = monthlySummaries.get(dateKey);
                if (dt.getIncome() > 0) {
                    tvInc.setVisibility(View.VISIBLE);
                    tvInc.setText(String.format(Locale.CHINA, "+%.0f", dt.getIncome()));
                } else
                    tvInc.setVisibility(View.GONE);

                if (dt.getExpense() > 0) {
                    tvExp.setVisibility(View.VISIBLE);
                    tvExp.setText(String.format(Locale.CHINA, "-%.0f", dt.getExpense()));
                } else
                    tvExp.setVisibility(View.GONE);
            } else {
                tvInc.setVisibility(View.GONE);
                tvExp.setVisibility(View.GONE);
            }

            return convertView;
        }

        private boolean isSameDay(Calendar c1, Calendar c2) {
            return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                    c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
        }
    }
}
