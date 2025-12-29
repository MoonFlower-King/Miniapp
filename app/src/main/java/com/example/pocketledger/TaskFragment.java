package com.example.pocketledger;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragment for the Task List (任务清单) section
 */
public class TaskFragment extends Fragment implements TodoAdapter.OnTodoActionListener {

    private RecyclerView rvTodos;
    private View layoutEmptyState;
    private DatabaseHelper dbHelper;
    private TodoAdapter adapter;
    private List<TodoItem> todoList = new ArrayList<>();

    // View tabs
    private TextView tabAll, tabByStatus, tabToday, tabList;
    private int currentTab = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = DatabaseHelper.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupViewTabs();
        setupRecyclerView();
    }

    private void initViews(View view) {
        rvTodos = view.findViewById(R.id.rvTodos);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        tabAll = view.findViewById(R.id.tabAll);
        tabByStatus = view.findViewById(R.id.tabByStatus);
        tabToday = view.findViewById(R.id.tabToday);
        tabList = view.findViewById(R.id.tabList);

        view.findViewById(R.id.layoutAddTask).setOnClickListener(v -> showAddTaskDialog(null));
        view.findViewById(R.id.btnNewTask).setOnClickListener(v -> showAddTaskDialog(null));
        view.findViewById(R.id.btnVisibility).setOnClickListener(v -> showVisibilityDialog());
    }

    private void setupViewTabs() {
        tabAll.setOnClickListener(v -> switchTab(0));
        tabByStatus.setOnClickListener(v -> switchTab(1));
        tabToday.setOnClickListener(v -> switchTab(2));
        tabList.setOnClickListener(v -> switchTab(3));
    }

    private void switchTab(int tab) {
        currentTab = tab;

        TextView[] tabs = { tabAll, tabByStatus, tabToday, tabList };
        for (int i = 0; i < tabs.length; i++) {
            if (i == tab) {
                tabs[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.text_main));
                tabs[i].setBackgroundResource(R.drawable.bg_tab_selected);
            } else {
                tabs[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
                tabs[i].setBackgroundResource(R.drawable.bg_tab_normal);
            }
        }

        loadTodoItems();
    }

    private void setupRecyclerView() {
        rvTodos.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TodoAdapter(todoList, this);
        rvTodos.setAdapter(adapter);
        loadVisibilitySettings();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTodoItems();
        loadVisibilitySettings();
    }

    private void loadVisibilitySettings() {
        if (adapter == null)
            return;
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("task_prefs",
                android.content.Context.MODE_PRIVATE);
        boolean showAssignee = prefs.getBoolean("vis_assignee", true);
        boolean showAttachment = prefs.getBoolean("vis_attachment", true);
        boolean showStatus = prefs.getBoolean("vis_status", true);
        boolean showPriority = prefs.getBoolean("vis_priority", true);
        boolean showDueDate = prefs.getBoolean("vis_duedate", true);
        adapter.setVisibilityConfig(showAssignee, showAttachment, showStatus, showPriority, showDueDate);
    }

    private void showVisibilityDialog() {
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("task_prefs",
                android.content.Context.MODE_PRIVATE);
        boolean showAssignee = prefs.getBoolean("vis_assignee", true);
        boolean showAttachment = prefs.getBoolean("vis_attachment", true);
        boolean showStatus = prefs.getBoolean("vis_status", true);
        boolean showPriority = prefs.getBoolean("vis_priority", true);
        boolean showDueDate = prefs.getBoolean("vis_duedate", true);

        String[] options = { "负责人", "附件", "状态", "优先级", "截止日期" };
        boolean[] checked = { showAssignee, showAttachment, showStatus, showPriority, showDueDate };

        new AlertDialog.Builder(requireContext())
                .setTitle("属性可见性")
                .setMultiChoiceItems(options, checked, (dialog, which, isChecked) -> {
                    // Update settings immediately or on dismiss
                })
                .setPositiveButton("确定", (dialog, which) -> {
                    android.content.SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("vis_assignee", checked[0]);
                    editor.putBoolean("vis_attachment", checked[1]);
                    editor.putBoolean("vis_status", checked[2]);
                    editor.putBoolean("vis_priority", checked[3]);
                    editor.putBoolean("vis_duedate", checked[4]);
                    editor.apply();
                    loadVisibilitySettings();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void loadTodoItems() {
        switch (currentTab) {
            case 0:
                todoList = dbHelper.getAllTodoItems();
                break;
            case 1:
                todoList = dbHelper.getTodoItemsByStatus(TodoItem.STATUS_IN_PROGRESS);
                todoList.addAll(dbHelper.getTodoItemsByStatus(TodoItem.STATUS_NOT_STARTED));
                todoList.addAll(dbHelper.getTodoItemsByStatus(TodoItem.STATUS_COMPLETED));
                break;
            case 2:
                todoList = dbHelper.getTodayTodoItems();
                break;
            case 3:
                todoList = dbHelper.getAllTodoItems();
                break;
        }
        if (adapter != null)
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
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_task, null);
        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        EditText etTags = dialogView.findViewById(R.id.etTags);
        EditText etAssignee = dialogView.findViewById(R.id.etAssignee); // New

        RadioGroup rgStatus = dialogView.findViewById(R.id.rgStatus);
        RadioGroup rgPriority = dialogView.findViewById(R.id.rgPriority);

        TextView tvDueDate = dialogView.findViewById(R.id.tvDueDate);
        TextView btnClearDate = dialogView.findViewById(R.id.btnClearDate);
        View layoutDueDate = dialogView.findViewById(R.id.layoutDueDate);

        TextView tvAttachment = dialogView.findViewById(R.id.tvAttachment); // New
        TextView btnClearAttachment = dialogView.findViewById(R.id.btnClearAttachment); // New
        View layoutAttachment = dialogView.findViewById(R.id.layoutAttachment); // New

        final String[] selectedDueDate = { null };
        final String[] selectedAttachment = { null }; // Simple string for now

        boolean isEdit = existingItem != null;

        if (isEdit) {
            etTitle.setText(existingItem.getTitle());
            etDescription.setText(existingItem.getDescription());
            etTags.setText(existingItem.getTags());
            etAssignee.setText(existingItem.getAssignee());

            String status = existingItem.getStatus();
            if (TodoItem.STATUS_IN_PROGRESS.equals(status)) {
                rgStatus.check(R.id.rbInProgress);
            } else if (TodoItem.STATUS_COMPLETED.equals(status)) {
                rgStatus.check(R.id.rbCompleted);
            } else {
                rgStatus.check(R.id.rbNotStarted);
            }

            String priority = existingItem.getPriority();
            if (TodoItem.PRIORITY_HIGH.equals(priority)) {
                rgPriority.check(R.id.rbHigh);
            } else if (TodoItem.PRIORITY_LOW.equals(priority)) {
                rgPriority.check(R.id.rbLow);
            } else {
                rgPriority.check(R.id.rbMedium);
            }

            if (existingItem.hasDueDate()) {
                selectedDueDate[0] = existingItem.getDueDate();
                tvDueDate.setText(existingItem.getFormattedDueDate());
                tvDueDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_main));
                btnClearDate.setVisibility(View.VISIBLE);
            }

            if (existingItem.getAttachmentPath() != null && !existingItem.getAttachmentPath().isEmpty()) {
                selectedAttachment[0] = existingItem.getAttachmentPath();
                tvAttachment.setText("已添加附件");
                tvAttachment.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_main));
                btnClearAttachment.setVisibility(View.VISIBLE);
            }
        }

        layoutDueDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                selectedDueDate[0] = String.format(Locale.CHINA, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                tvDueDate.setText(String.format(Locale.CHINA, "%02d/%02d/%d", month + 1, dayOfMonth, year));
                tvDueDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_main));
                btnClearDate.setVisibility(View.VISIBLE);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnClearDate.setOnClickListener(v -> {
            selectedDueDate[0] = null;
            tvDueDate.setText("选择截止日期 (可选)");
            tvDueDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            btnClearDate.setVisibility(View.GONE);
        });

        layoutAttachment.setOnClickListener(v -> {
            // Simulate attachment picking
            selectedAttachment[0] = "demo_file.pdf";
            tvAttachment.setText("demo_file.pdf (模拟)");
            tvAttachment.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_main));
            btnClearAttachment.setVisibility(View.VISIBLE);
            Toast.makeText(requireContext(), "已模拟添加附件", Toast.LENGTH_SHORT).show();
        });

        btnClearAttachment.setOnClickListener(v -> {
            selectedAttachment[0] = null;
            tvAttachment.setText("添加附件 (可选)");
            tvAttachment.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            btnClearAttachment.setVisibility(View.GONE);
        });

        new AlertDialog.Builder(requireContext())
                .setTitle(isEdit ? "编辑任务" : "新建任务")
                .setView(dialogView)
                .setPositiveButton(isEdit ? "保存" : "添加", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    if (title.isEmpty()) {
                        Toast.makeText(requireContext(), "请输入任务名称", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String description = etDescription.getText().toString().trim();
                    String tags = etTags.getText().toString().trim();
                    String assignee = etAssignee.getText().toString().trim();

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

                    if (isEdit) {
                        existingItem.setTitle(title);
                        existingItem.setDescription(description);
                        existingItem.setStatus(status);
                        existingItem.setPriority(priority);
                        existingItem.setDueDate(selectedDueDate[0]);
                        existingItem.setTags(tags);
                        existingItem.setAssignee(assignee);
                        existingItem.setAttachmentPath(selectedAttachment[0]);
                        dbHelper.updateTodoItem(existingItem);
                        Toast.makeText(requireContext(), "任务已更新", Toast.LENGTH_SHORT).show();
                    } else {
                        TodoItem newItem = new TodoItem(title, description, status, priority,
                                selectedDueDate[0], tags, today, assignee, selectedAttachment[0]);
                        dbHelper.addTodoItem(newItem);
                        Toast.makeText(requireContext(), "任务已添加", Toast.LENGTH_SHORT).show();
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
        new AlertDialog.Builder(requireContext())
                .setTitle("删除任务")
                .setMessage("确定要删除\"" + item.getTitle() + "\"吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    dbHelper.deleteTodoItem(item.getId());
                    loadTodoItems();
                    Toast.makeText(requireContext(), "任务已删除", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public void onStatusClick(TodoItem item) {
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
