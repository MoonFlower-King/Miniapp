package com.example.pocketledger;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatMessage> messages;
    private OnConfirmListener confirmListener;
    private OnTaskConfirmListener taskConfirmListener;

    public interface OnConfirmListener {
        void onConfirm(Transaction transaction, int position);
    }

    public interface OnTaskConfirmListener {
        void onConfirmTask(TodoItem todoItem, int position);
    }

    public ChatAdapter(List<ChatMessage> messages, OnConfirmListener confirmListener,
            OnTaskConfirmListener taskConfirmListener) {
        this.messages = messages;
        this.confirmListener = confirmListener;
        this.taskConfirmListener = taskConfirmListener;
    }

    // Legacy constructor for backward compatibility
    public ChatAdapter(List<ChatMessage> messages, OnConfirmListener confirmListener) {
        this(messages, confirmListener, null);
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ChatMessage.TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_ai, parent, false);
            return new AiViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).tvUserMsg.setText(message.getContent());
        } else if (holder instanceof AiViewHolder) {
            AiViewHolder aiHolder = (AiViewHolder) holder;

            // Handle welcome message - show text content
            if (message.isWelcomeMessage()) {
                aiHolder.layoutThinking.setVisibility(View.GONE);
                aiHolder.cardPreview.setVisibility(View.GONE);
                aiHolder.cardTaskPreview.setVisibility(View.GONE);
                aiHolder.tvWelcome.setVisibility(View.VISIBLE);
                aiHolder.tvWelcome.setText(message.getContent());
                return;
            }

            // Hide welcome text for non-welcome messages
            aiHolder.tvWelcome.setVisibility(View.GONE);

            if (message.isThinking()) {
                aiHolder.layoutThinking.setVisibility(View.VISIBLE);
                aiHolder.cardPreview.setVisibility(View.GONE);
                aiHolder.cardTaskPreview.setVisibility(View.GONE);
            } else {
                aiHolder.layoutThinking.setVisibility(View.GONE);

                // Check for pending task first
                TodoItem task = message.getPendingTodoItem();
                if (task != null) {
                    aiHolder.cardPreview.setVisibility(View.GONE);
                    aiHolder.cardTaskPreview.setVisibility(View.VISIBLE);
                    bindTaskPreview(aiHolder, task, position);
                } else {
                    // Check for pending transaction
                    Transaction t = message.getPendingTransaction();
                    if (t != null) {
                        aiHolder.cardPreview.setVisibility(View.VISIBLE);
                        aiHolder.cardTaskPreview.setVisibility(View.GONE);
                        bindTransactionPreview(aiHolder, t, message, position);
                    } else {
                        aiHolder.cardPreview.setVisibility(View.GONE);
                        aiHolder.cardTaskPreview.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    private void bindTransactionPreview(AiViewHolder aiHolder, Transaction t, ChatMessage message, int position) {
        aiHolder.tvPreviewCategory.setText(t.getCategory());
        String amountPrefix = "income".equals(t.getType()) ? "+¥" : "-¥";
        aiHolder.tvPreviewAmount.setText(String.format(Locale.CHINA, "%s%.2f", amountPrefix, t.getAmount()));
        aiHolder.tvPreviewNote.setText(t.getNote().isEmpty() ? message.getContent() : t.getNote());
        aiHolder.tvPreviewTime.setText(t.getDate());

        aiHolder.btnConfirm.setOnClickListener(v -> {
            if (confirmListener != null)
                confirmListener.onConfirm(t, position);
        });
        aiHolder.btnCancel.setOnClickListener(v -> {
            messages.remove(position);
            notifyItemRemoved(position);
        });
    }

    private void bindTaskPreview(AiViewHolder aiHolder, TodoItem task, int position) {
        aiHolder.tvTaskTitle.setText(task.getTitle());
        aiHolder.tvTaskPriority.setText(task.getPriorityText());
        aiHolder.tvTaskStatus.setText("○ " + task.getStatusText());

        // Set priority color
        int colorRes;
        String priority = task.getPriority();
        if (TodoItem.PRIORITY_HIGH.equals(priority)) {
            colorRes = R.color.expense_red;
        } else if (TodoItem.PRIORITY_LOW.equals(priority)) {
            colorRes = R.color.income_green;
        } else {
            colorRes = R.color.income_yellow;
        }
        aiHolder.tvTaskPriority.getBackground().mutate().setTint(
                ContextCompat.getColor(aiHolder.itemView.getContext(), colorRes));

        // Show due date if present
        if (task.hasDueDate()) {
            aiHolder.tvTaskDueDate.setVisibility(View.VISIBLE);
            aiHolder.tvTaskDueDate.setText("📅 " + task.getFormattedDueDate());
        } else {
            aiHolder.tvTaskDueDate.setVisibility(View.GONE);
        }

        // Show description if present
        String description = task.getDescription();
        if (description != null && !description.isEmpty()) {
            aiHolder.tvTaskDescription.setVisibility(View.VISIBLE);
            aiHolder.tvTaskDescription.setText(description);
        } else {
            aiHolder.tvTaskDescription.setVisibility(View.GONE);
        }

        aiHolder.btnTaskConfirm.setOnClickListener(v -> {
            if (taskConfirmListener != null) {
                taskConfirmListener.onConfirmTask(task, position);
            }
        });
        aiHolder.btnTaskCancel.setOnClickListener(v -> {
            messages.remove(position);
            notifyItemRemoved(position);
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserMsg;

        UserViewHolder(View itemView) {
            super(itemView);
            tvUserMsg = itemView.findViewById(R.id.tvUserMsg);
        }
    }

    static class AiViewHolder extends RecyclerView.ViewHolder {
        View layoutThinking, cardPreview, cardTaskPreview;
        TextView tvPreviewCategory, tvPreviewAmount, tvPreviewNote, tvPreviewTime, tvWelcome;
        TextView tvTaskTitle, tvTaskPriority, tvTaskStatus, tvTaskDueDate, tvTaskDescription;
        Button btnConfirm, btnCancel, btnTaskConfirm, btnTaskCancel;

        AiViewHolder(View itemView) {
            super(itemView);
            layoutThinking = itemView.findViewById(R.id.layoutThinking);
            cardPreview = itemView.findViewById(R.id.cardPreview);
            cardTaskPreview = itemView.findViewById(R.id.cardTaskPreview);
            tvPreviewCategory = itemView.findViewById(R.id.tvPreviewCategory);
            tvPreviewAmount = itemView.findViewById(R.id.tvPreviewAmount);
            tvPreviewNote = itemView.findViewById(R.id.tvPreviewNote);
            tvPreviewTime = itemView.findViewById(R.id.tvPreviewTime);
            btnConfirm = itemView.findViewById(R.id.btnConfirm);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            tvWelcome = itemView.findViewById(R.id.tvWelcome);

            // Task preview
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvTaskPriority = itemView.findViewById(R.id.tvTaskPriority);
            tvTaskStatus = itemView.findViewById(R.id.tvTaskStatus);
            tvTaskDueDate = itemView.findViewById(R.id.tvTaskDueDate);
            tvTaskDescription = itemView.findViewById(R.id.tvTaskDescription);
            btnTaskConfirm = itemView.findViewById(R.id.btnTaskConfirm);
            btnTaskCancel = itemView.findViewById(R.id.btnTaskCancel);
        }
    }
}
