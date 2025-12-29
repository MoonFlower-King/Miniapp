package com.example.pocketledger;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<Transaction> transactionList;
    private OnTransactionLongClickListener longClickListener;

    public interface OnTransactionLongClickListener {
        void onLongClick(int id);
    }

    public TransactionAdapter(List<Transaction> transactionList, OnTransactionLongClickListener longClickListener) {
        this.transactionList = transactionList != null ? transactionList : new ArrayList<>();
        this.longClickListener = longClickListener;
    }

    public void updateData(List<Transaction> newList) {
        this.transactionList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        holder.tvCategory.setText(transaction.getCategory());

        String note = transaction.getNote();
        if (note == null || note.trim().isEmpty()) {
            holder.tvDesc.setText(transaction.getDate());
        } else {
            holder.tvDesc.setText(String.format("%s • %s", transaction.getDate(), note));
        }

        if ("income".equals(transaction.getType())) {
            holder.tvAmount.setText(String.format(Locale.CHINA, "+%.2f", transaction.getAmount()));
            holder.tvAmount.setTextColor(Color.parseColor("#2E7D32")); // Green
            holder.viewIndicator.setBackgroundColor(Color.parseColor("#2E7D32"));
        } else {
            holder.tvAmount.setText(String.format(Locale.CHINA, "-%.2f", transaction.getAmount()));
            holder.tvAmount.setTextColor(Color.parseColor("#C62828")); // Red
            holder.viewIndicator.setBackgroundColor(Color.parseColor("#C62828"));
        }

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onLongClick(transaction.getId());
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvDesc, tvAmount;
        View viewIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            viewIndicator = itemView.findViewById(R.id.viewIndicator);
        }
    }
}
