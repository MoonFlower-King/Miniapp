package com.example.pocketledger;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SubCategoryAdapter extends RecyclerView.Adapter<SubCategoryAdapter.ViewHolder> {

    private List<String> subs;
    private OnSubClickListener listener;
    private int selectedPos = -1;

    public interface OnSubClickListener {
        void onClick(String subName);
    }

    public SubCategoryAdapter(List<String> subs, OnSubClickListener listener) {
        this.subs = subs;
        this.listener = listener;
    }

    public void updateData(List<String> newList) {
        this.subs = newList;
        this.selectedPos = -1;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sub_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = subs.get(position);
        holder.tvName.setText(name);

        boolean isSelected = selectedPos == position;
        holder.itemView.setSelected(isSelected);
        holder.tvName.setTextColor(isSelected ? Color.WHITE : Color.parseColor("#5C6BC0"));
        holder.tvName.setBackgroundResource(isSelected ? R.drawable.bg_sub_selected : R.drawable.bg_sub_normal);

        holder.itemView.setOnClickListener(v -> {
            int oldPos = selectedPos;
            selectedPos = position;
            notifyItemChanged(oldPos);
            notifyItemChanged(selectedPos);
            listener.onClick(name);
        });
    }

    @Override
    public int getItemCount() {
        return subs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView;
        }
    }
}
