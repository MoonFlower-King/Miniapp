package com.example.pocketledger;

public class Transaction {
    private int id;
    private String type; // "income" or "expense"
    private double amount;
    private String category;
    private String note;
    private String date;

    public Transaction(int id, String type, double amount, String category, String note, String date) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.note = note;
        this.date = date;
    }

    public Transaction(String type, double amount, String category, String note, String date) {
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.note = note;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public String getNote() {
        return note;
    }

    public String getDate() {
        return date;
    }
}
