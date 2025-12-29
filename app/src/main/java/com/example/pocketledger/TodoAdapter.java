package com.example.pocketledger;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {

    private List<TodoItem> todoList;
    private OnTodoActionListener listener;

    public interface OnTodoActionListener {
        void onTodoToggle(TodoItem item, boolean completed);

        void onTodoClick(TodoItem item);

        void onTodoLongClick(TodoItem item);

        void onStatusClick(TodoItem item);
    }

    private boolean showAssignee = true;
    private boolean showAttachment = true;
    private boolean showStatus = true;
    private boolean showPriority = true;
    private boolean showDueDate = true;

    public TodoAdapter(List<TodoItem> todoList, OnTodoActionListener listener) {
        this.todoList = todoList;
        this.listener = listener;
    }

    public void setVisibilityConfig(boolean showAssignee, boolean showAttachment, boolean showStatus,
            boolean showPriority, boolean showDueDate) {
        this.showAssignee = showAssignee;
        this.showAttachment = showAttachment;
        this.showStatus = showStatus;
        this.showPriority = showPriority;
        this.showDueDate = showDueDate;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_todo, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        TodoItem item = todoList.get(position);

        // Title with strikethrough if completed
        holder.tvTitle.setText(item.getTitle());
        if (item.isCompleted()) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_hint));
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvTitle.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_main));
        }

        // Checkbox
        holder.cbCompleted.setOnCheckedChangeListener(null);
        holder.cbCompleted.setChecked(item.isCompleted());
        holder.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onTodoToggle(item, isChecked);
            }
        });

        // Status tag
        if (showStatus) {
            holder.tvStatus.setVisibility(View.VISIBLE);
            String statusText = item.getStatusText();
            int statusColor = item.getStatusColorRes();
            String status = item.getStatus();
            String statusDot;
            if (status == null || TodoItem.STATUS_NOT_STARTED.equals(status)) {
                statusDot = "○ ";
            } else if (TodoItem.STATUS_IN_PROGRESS.equals(status)) {
                statusDot = "● ";
            } else if (TodoItem.STATUS_COMPLETED.equals(status)) {
                statusDot = "✓ ";
            } else {
                statusDot = "○ ";
            }
            holder.tvStatus.setText(statusDot + statusText);
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), statusColor));

            holder.tvStatus.setOnClickListener(v -> {
                if (listener != null)
                    listener.onStatusClick(item);
            });
        } else {
            holder.tvStatus.setVisibility(View.GONE);
        }

        // Priority tag
        if (showPriority && item.getPriority() != null && !item.getPriority().isEmpty()) {
            holder.tvPriority.setVisibility(View.VISIBLE);
            holder.tvPriority.setText(item.getPriorityText());
            // Set background color
            int colorRes;
            switch (item.getPriority()) {
                case TodoItem.PRIORITY_HIGH:
                    colorRes = R.color.expense_red;
                    break;
                case TodoItem.PRIORITY_MEDIUM:
                    colorRes = R.color.income_yellow;
                    break;
                case TodoItem.PRIORITY_LOW:
                    colorRes = R.color.income_green;
                    break;
                default:
                    colorRes = R.color.text_hint;
                    break;
            }
            holder.tvPriority.getBackground().mutate().setTint(
                    ContextCompat.getColor(holder.itemView.getContext(), colorRes));
        } else {
            holder.tvPriority.setVisibility(View.GONE); // Use GONE instead of INVISIBLE to collapse
        }

        // Assignee
        if (showAssignee && item.getAssignee() != null && !item.getAssignee().isEmpty()) {
            holder.tvAssignee.setVisibility(View.VISIBLE);
            String assignee = item.getAssignee();
            // Show only first char or 2 chars for compactness if needed, or full name
            holder.tvAssignee.setText(assignee.length() > 3 ? assignee.substring(0, 3) : assignee);
        } else {
            holder.tvAssignee.setVisibility(View.GONE);
        }

        // Attachment
        if (showAttachment && item.getAttachmentPath() != null && !item.getAttachmentPath().isEmpty()) {
            holder.tvAttachment.setVisibility(View.VISIBLE);
        } else {
            holder.tvAttachment.setVisibility(View.GONE);
        }

        // Due date
        if (showDueDate && item.hasDueDate()) {
            holder.tvDueDate.setVisibility(View.VISIBLE);
            holder.tvDueDate.setText(item.getFormattedDueDate());
            if (item.isOverdue()) {
                holder.tvDueDate
                        .setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.expense_red));
            } else {
                holder.tvDueDate
                        .setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_secondary));
            }
        } else {
            holder.tvDueDate.setVisibility(View.GONE);
        }

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null)
                listener.onTodoClick(item);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null)
                listener.onTodoLongClick(item);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    public void updateData(List<TodoItem> newList) {
        this.todoList = newList;
        notifyDataSetChanged();
    }

    static class TodoViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbCompleted;
        TextView tvTitle, tvStatus, tvPriority, tvDueDate, tvAssignee, tvAttachment;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            cbCompleted = itemView.findViewById(R.id.cbCompleted);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPriority = itemView.findViewById(R.id.tvPriority);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            tvAssignee = itemView.findViewById(R.id.tvAssignee);
            tvAttachment = itemView.findViewById(R.id.tvAttachment);
        }
    }
}
