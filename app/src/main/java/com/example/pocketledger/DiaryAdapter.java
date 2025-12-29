package com.example.pocketledger;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder> {

    private List<DiaryEntry> diaryList;
    private OnDiaryClickListener listener;

    public interface OnDiaryClickListener {
        void onDiaryClick(DiaryEntry entry);

        void onDiaryLongClick(DiaryEntry entry);
    }

    public DiaryAdapter(List<DiaryEntry> diaryList, OnDiaryClickListener listener) {
        this.diaryList = diaryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DiaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_diary, parent, false);
        return new DiaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiaryViewHolder holder, int position) {
        DiaryEntry entry = diaryList.get(position);

        holder.tvDate.setText(entry.getDate());
        holder.tvMood.setText(entry.getMoodEmoji());
        holder.tvTitle.setText(entry.getTitle() != null && !entry.getTitle().isEmpty()
                ? entry.getTitle()
                : "无标题");
        holder.tvContent.setText(entry.getContent());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDiaryClick(entry);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onDiaryLongClick(entry);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return diaryList.size();
    }

    public void updateData(List<DiaryEntry> newList) {
        this.diaryList = newList;
        notifyDataSetChanged();
    }

    static class DiaryViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvMood, tvTitle, tvContent;

        public DiaryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvMood = itemView.findViewById(R.id.tvMood);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
        }
    }
}
