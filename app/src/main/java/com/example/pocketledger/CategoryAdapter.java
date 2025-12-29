package com.example.pocketledger;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private List<Category> categories;
    private OnCategoryClickListener listener;
    private int selectedPos = -1;
    private boolean isIncome = false;

    public interface OnCategoryClickListener {
        void onClick(Category category, int position);
    }

    public CategoryAdapter(List<Category> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    public void updateData(List<Category> newList) {
        this.categories = newList;
        this.selectedPos = -1;
        notifyDataSetChanged();
    }

    public void setIncomeMode(boolean income) {
        this.isIncome = income;
        this.selectedPos = -1;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.name.setText(category.getName());
        holder.icon.setImageResource(category.getIconRes());

        boolean isSelected = selectedPos == position;
        holder.itemView.setSelected(isSelected);

        // Vibrant Color Logic
        int themeColor = category.getColor();
        
        if (isSelected) {
            holder.name.setTextColor(themeColor);
            holder.icon.setBackgroundResource(R.drawable.bg_category_item);
            holder.icon.getBackground().setColorFilter(themeColor, PorterDuff.Mode.SRC_ATOP);
            holder.icon.getBackground().setAlpha(40); // Faint background
            holder.icon.setColorFilter(themeColor, PorterDuff.Mode.SRC_IN);
        } else {
            holder.name.setTextColor(Color.parseColor("#2C2C2C"));
            holder.icon.setBackgroundResource(R.drawable.bg_category_item);
            holder.icon.getBackground().setColorFilter(Color.parseColor("#F5F5F5"), PorterDuff.Mode.SRC_ATOP);
            holder.icon.getBackground().setAlpha(255);
            holder.icon.setColorFilter(Color.parseColor("#757575"), PorterDuff.Mode.SRC_IN);
        }

        holder.itemView.setOnClickListener(v -> {
            int oldPos = selectedPos;
            selectedPos = position;
            notifyItemChanged(oldPos);
            notifyItemChanged(selectedPos);
            listener.onClick(category, position);
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;
        ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.ivCategoryIcon);
            name = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}
