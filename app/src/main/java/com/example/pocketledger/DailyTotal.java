package com.example.pocketledger;

public class DailyTotal {
    private String date;
    private double income;
    private double expense;

    public DailyTotal(String date, double income, double expense) {
        this.date = date;
        this.income = income;
        this.expense = expense;
    }

    public String getDate() { return date; }
    public double getIncome() { return income; }
    public double getExpense() { return expense; }
}
