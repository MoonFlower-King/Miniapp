package com.example.pocketledger;

import java.util.ArrayList;
import java.util.List;

public class Category {
    private String name;
    private int iconRes;
    private int color; // Added color property for vibrant UI
    private List<String> subCategories;

    public Category(String name, int iconRes, int color) {
        this.name = name;
        this.iconRes = iconRes;
        this.color = color;
        this.subCategories = new ArrayList<>();
    }

    public Category(String name, int iconRes, int color, List<String> subCategories) {
        this.name = name;
        this.iconRes = iconRes;
        this.color = color;
        this.subCategories = subCategories;
    }

    public String getName() { return name; }
    public int getIconRes() { return iconRes; }
    public int getColor() { return color; }
    public List<String> getSubCategories() { return subCategories; }
}
