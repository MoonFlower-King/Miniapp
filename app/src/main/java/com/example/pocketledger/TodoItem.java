package com.example.pocketledger;

/**
 * Model class representing a todo/task item (Notion-style)
 * With comprehensive properties: status, priority, due date, tags, description
 */
public class TodoItem {
    private int id;
    private String title;
    private String description;
    private String status; // not_started, in_progress, completed
    private String priority; // high, medium, low
    private String dueDate;
    private String tags; // Comma-separated tags
    private String date; // Creation date
    private String createdAt;

    // Status constants
    public static final String STATUS_NOT_STARTED = "not_started";
    public static final String STATUS_IN_PROGRESS = "in_progress";
    public static final String STATUS_COMPLETED = "completed";

    // Priority constants
    public static final String PRIORITY_HIGH = "high";
    public static final String PRIORITY_MEDIUM = "medium";
    public static final String PRIORITY_LOW = "low";

    // Constructor for creating new item
    public TodoItem(String title, String priority, String date) {
        this.title = title;
        this.priority = priority;
        this.date = date;
        this.status = STATUS_NOT_STARTED;
    }

    private String assignee; // New field
    private String attachmentPath; // New field

    // Full constructor for creating new item
    public TodoItem(String title, String description, String status, String priority,
            String dueDate, String tags, String date, String assignee, String attachmentPath) {
        this.title = title;
        this.description = description;
        this.status = status != null ? status : STATUS_NOT_STARTED;
        this.priority = priority;
        this.dueDate = dueDate;
        this.tags = tags;
        this.date = date;
        this.assignee = assignee; // New
        this.attachmentPath = attachmentPath; // New
    }

    // Constructor for reading from database
    public TodoItem(int id, String title, String description, String status, String priority,
            String dueDate, String tags, String date, String createdAt, String assignee, String attachmentPath) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status != null ? status : STATUS_NOT_STARTED;
        this.priority = priority;
        this.dueDate = dueDate;
        this.tags = tags;
        this.date = date;
        this.createdAt = createdAt;
        this.assignee = assignee; // New
        this.attachmentPath = attachmentPath; // New
    }

    // Compatibility constructor (used in some places, defaults new fields to null)
    public TodoItem(String title, String description, String status, String priority,
            String dueDate, String tags, String date) {
        this(title, description, status, priority, dueDate, tags, date, null, null);
    }

    // Legacy constructor for compatibility
    public TodoItem(int id, String title, String priority, boolean completed, String date, String createdAt) {
        this.id = id;
        this.title = title;
        this.priority = priority;
        this.status = completed ? STATUS_COMPLETED : STATUS_NOT_STARTED;
        this.date = date;
        this.createdAt = createdAt;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getPriority() {
        return priority;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getTags() {
        return tags;
    }

    public String getDate() {
        return date;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getAssignee() {
        return assignee;
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    // Legacy compatibility
    public boolean isCompleted() {
        return STATUS_COMPLETED.equals(status);
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }

    // Legacy compatibility
    public void setCompleted(boolean completed) {
        this.status = completed ? STATUS_COMPLETED : STATUS_NOT_STARTED;
    }

    // Get priority display text
    public String getPriorityText() {
        if (priority == null)
            return "";
        switch (priority) {
            case PRIORITY_HIGH:
                return "高";
            case PRIORITY_MEDIUM:
                return "中";
            case PRIORITY_LOW:
                return "低";
            default:
                return "";
        }
    }

    // Get status display text
    public String getStatusText() {
        if (status == null)
            return "未开始";
        switch (status) {
            case STATUS_NOT_STARTED:
                return "未开始";
            case STATUS_IN_PROGRESS:
                return "进行中";
            case STATUS_COMPLETED:
                return "已完成";
            default:
                return "未开始";
        }
    }

    // Get priority color resource
    public int getPriorityColorRes() {
        if (priority == null)
            return R.color.text_hint;
        switch (priority) {
            case PRIORITY_HIGH:
                return R.color.expense_red;
            case PRIORITY_MEDIUM:
                return R.color.income_yellow;
            case PRIORITY_LOW:
                return R.color.income_green;
            default:
                return R.color.text_hint;
        }
    }

    // Get status color resource
    public int getStatusColorRes() {
        if (status == null)
            return R.color.text_hint;
        switch (status) {
            case STATUS_NOT_STARTED:
                return R.color.text_hint;
            case STATUS_IN_PROGRESS:
                return R.color.primary;
            case STATUS_COMPLETED:
                return R.color.income_green;
            default:
                return R.color.text_hint;
        }
    }

    // Check if has due date
    public boolean hasDueDate() {
        return dueDate != null && !dueDate.isEmpty();
    }

    // Check if overdue
    public boolean isOverdue() {
        if (!hasDueDate() || isCompleted())
            return false;
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.CHINA);
            java.util.Date due = sdf.parse(dueDate);
            java.util.Date today = sdf.parse(sdf.format(new java.util.Date()));
            return due != null && due.before(today);
        } catch (Exception e) {
            return false;
        }
    }

    // Format due date for display
    public String getFormattedDueDate() {
        if (!hasDueDate())
            return "";
        try {
            // Format as MM/dd
            String[] parts = dueDate.split("-");
            if (parts.length >= 3) {
                return parts[1] + "/" + parts[2] + "/" + parts[0];
            }
        } catch (Exception e) {
            // ignore
        }
        return dueDate;
    }
}
