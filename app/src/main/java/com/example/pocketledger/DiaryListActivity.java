package com.example.pocketledger;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DiaryListActivity extends AppCompatActivity implements TodoAdapter.OnTodoActionListener {

    private RecyclerView rvTodos;
    private View layoutEmptyState;
    private DatabaseHelper dbHelper;
    private TodoAdapter adapter;
    private List<TodoItem> todoList = new ArrayList<>();

    // View tabs
    private TextView tabAll, tabByStatus, tabToday, tabList;
    private int currentTab = 0; // 0=all, 1=byStatus, 2=today, 3=list

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_list);

        dbHelper = DatabaseHelper.getInstance(this);

        initViews();
        setupNavigation();
        setupViewTabs();
        setupRecyclerView();
    }

    private void initViews() {
        rvTodos = findViewById(R.id.rvTodos);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        tabAll = findViewById(R.id.tabAll);
        tabByStatus = findViewById(R.id.tabByStatus);
        tabToday = findViewById(R.id.tabToday);
        tabList = findViewById(R.id.tabList);

        findViewById(R.id.layoutAddTask).setOnClickListener(v -> showAddTaskDialog(null));
        findViewById(R.id.btnNewTask).setOnClickListener(v -> showAddTaskDialog(null));
    }

    private void setupNavigation() {
        TextView tvNavDiary = findViewById(R.id.tvNavDiary);
        TextView tvNavLedger = findViewById(R.id.tvNavLedger);
        TextView tvNavAi = findViewById(R.id.tvNavAi);

        // Highlight current tab
        tvNavDiary.setTextColor(ContextCompat.getColor(this, R.color.primary));
        tvNavDiary.setTypeface(null, Typeface.BOLD);

        tvNavLedger.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });

        tvNavAi.setOnClickListener(v -> {
            Intent intent = new Intent(this, AiAssistantActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void setupViewTabs() {
        tabAll.setOnClickListener(v -> switchTab(0));
        tabByStatus.setOnClickListener(v -> switchTab(1));
        tabToday.setOnClickListener(v -> switchTab(2));
        tabList.setOnClickListener(v -> switchTab(3));
    }

    private void switchTab(int tab) {
        currentTab = tab;

        // Update tab appearance
        TextView[] tabs = { tabAll, tabByStatus, tabToday, tabList };
        for (int i = 0; i < tabs.length; i++) {
            if (i == tab) {
                tabs[i].setTextColor(ContextCompat.getColor(this, R.color.text_main));
                tabs[i].setBackgroundResource(R.drawable.bg_tab_selected);
            } else {
                tabs[i].setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                tabs[i].setBackgroundResource(R.drawable.bg_tab_normal);
            }
        }

        loadTodoItems();
    }

    private void setupRecyclerView() {
        rvTodos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TodoAdapter(todoList, this);
        rvTodos.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTodoItems();
    }

    private void loadTodoItems() {
        switch (currentTab) {
            case 0: // All tasks
                todoList = dbHelper.getAllTodoItems();
                break;
            case 1: // By status - show in progress first
                todoList = dbHelper.getTodoItemsByStatus(TodoItem.STATUS_IN_PROGRESS);
                todoList.addAll(dbHelper.getTodoItemsByStatus(TodoItem.STATUS_NOT_STARTED));
                todoList.addAll(dbHelper.getTodoItemsByStatus(TodoItem.STATUS_COMPLETED));
                break;
            case 2: // Today's tasks
                todoList = dbHelper.getTodayTodoItems();
                break;
            case 3: // List view (same as all for now)
                todoList = dbHelper.getAllTodoItems();
                break;
        }
        adapter.updateData(todoList);
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (todoList.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            rvTodos.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            rvTodos.setVisibility(View.VISIBLE);
        }
    }

    private void showAddTaskDialog(TodoItem existingItem) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        EditText etTags = dialogView.findViewById(R.id.etTags);
        RadioGroup rgStatus = dialogView.findViewById(R.id.rgStatus);
        RadioGroup rgPriority = dialogView.findViewById(R.id.rgPriority);
        TextView tvDueDate = dialogView.findViewById(R.id.tvDueDate);
        TextView btnClearDate = dialogView.findViewById(R.id.btnClearDate);
        View layoutDueDate = dialogView.findViewById(R.id.layoutDueDate);

        final String[] selectedDueDate = { null };
        boolean isEdit = existingItem != null;

        if (isEdit) {
            etTitle.setText(existingItem.getTitle());
            etDescription.setText(existingItem.getDescription());
            etTags.setText(existingItem.getTags());

            // Set status
            String status = existingItem.getStatus();
            if (TodoItem.STATUS_IN_PROGRESS.equals(status)) {
                rgStatus.check(R.id.rbInProgress);
            } else if (TodoItem.STATUS_COMPLETED.equals(status)) {
                rgStatus.check(R.id.rbCompleted);
            } else {
                rgStatus.check(R.id.rbNotStarted);
            }

            // Set priority
            String priority = existingItem.getPriority();
            if (TodoItem.PRIORITY_HIGH.equals(priority)) {
                rgPriority.check(R.id.rbHigh);
            } else if (TodoItem.PRIORITY_LOW.equals(priority)) {
                rgPriority.check(R.id.rbLow);
            } else {
                rgPriority.check(R.id.rbMedium);
            }

            // Set due date
            if (existingItem.hasDueDate()) {
                selectedDueDate[0] = existingItem.getDueDate();
                tvDueDate.setText(existingItem.getFormattedDueDate());
                tvDueDate.setTextColor(ContextCompat.getColor(this, R.color.text_main));
                btnClearDate.setVisibility(View.VISIBLE);
            }
        }

        // Due date picker
        layoutDueDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedDueDate[0] = String.format(Locale.CHINA, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                tvDueDate.setText(String.format(Locale.CHINA, "%02d/%02d/%d", month + 1, dayOfMonth, year));
                tvDueDate.setTextColor(ContextCompat.getColor(this, R.color.text_main));
                btnClearDate.setVisibility(View.VISIBLE);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnClearDate.setOnClickListener(v -> {
            selectedDueDate[0] = null;
            tvDueDate.setText("选择截止日期 (可选)");
            tvDueDate.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            btnClearDate.setVisibility(View.GONE);
        });

        new AlertDialog.Builder(this)
                .setTitle(isEdit ? "编辑任务" : "新建任务")
                .setView(dialogView)
                .setPositiveButton(isEdit ? "保存" : "添加", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    if (title.isEmpty()) {
                        Toast.makeText(this, "请输入任务名称", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String description = etDescription.getText().toString().trim();
                    String tags = etTags.getText().toString().trim();

                    // Get status
                    String status = TodoItem.STATUS_NOT_STARTED;
                    int statusId = rgStatus.getCheckedRadioButtonId();
                    if (statusId == R.id.rbInProgress)
                        status = TodoItem.STATUS_IN_PROGRESS;
                    else if (statusId == R.id.rbCompleted)
                        status = TodoItem.STATUS_COMPLETED;

                    // Get priority
                    String priority = TodoItem.PRIORITY_MEDIUM;
                    int priorityId = rgPriority.getCheckedRadioButtonId();
                    if (priorityId == R.id.rbHigh)
                        priority = TodoItem.PRIORITY_HIGH;
                    else if (priorityId == R.id.rbLow)
                        priority = TodoItem.PRIORITY_LOW;

                    String today = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date());

                    if (isEdit) {
                        existingItem.setTitle(title);
                        existingItem.setDescription(description);
                        existingItem.setStatus(status);
                        existingItem.setPriority(priority);
                        existingItem.setDueDate(selectedDueDate[0]);
                        existingItem.setTags(tags);
                        dbHelper.updateTodoItem(existingItem);
                        Toast.makeText(this, "任务已更新", Toast.LENGTH_SHORT).show();
                    } else {
                        TodoItem newItem = new TodoItem(title, description, status, priority,
                                selectedDueDate[0], tags, today, null, null);
                        dbHelper.addTodoItem(newItem);
                        Toast.makeText(this, "任务已添加", Toast.LENGTH_SHORT).show();
                    }

                    loadTodoItems();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public void onTodoToggle(TodoItem item, boolean completed) {
        String newStatus = completed ? TodoItem.STATUS_COMPLETED : TodoItem.STATUS_NOT_STARTED;
        dbHelper.updateTodoStatus(item.getId(), newStatus);
        loadTodoItems();
    }

    @Override
    public void onTodoClick(TodoItem item) {
        showAddTaskDialog(item);
    }

    @Override
    public void onTodoLongClick(TodoItem item) {
        new AlertDialog.Builder(this)
                .setTitle("删除任务")
                .setMessage("确定要删除\"" + item.getTitle() + "\"吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    dbHelper.deleteTodoItem(item.getId());
                    loadTodoItems();
                    Toast.makeText(this, "任务已删除", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public void onStatusClick(TodoItem item) {
        // Cycle through statuses: not_started -> in_progress -> completed ->
        // not_started
        String currentStatus = item.getStatus();
        String newStatus;
        if (TodoItem.STATUS_NOT_STARTED.equals(currentStatus)) {
            newStatus = TodoItem.STATUS_IN_PROGRESS;
        } else if (TodoItem.STATUS_IN_PROGRESS.equals(currentStatus)) {
            newStatus = TodoItem.STATUS_COMPLETED;
        } else {
            newStatus = TodoItem.STATUS_NOT_STARTED;
        }
        dbHelper.updateTodoStatus(item.getId(), newStatus);
        loadTodoItems();
    }
}
