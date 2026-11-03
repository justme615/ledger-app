package com.example.personal;

public class LedgerRecord {
    public int id;
    public String type;
    public double amount;
    public String category;
    public String date;
    public String note;

    public LedgerRecord(int id, String type, double amount,
                        String category, String date, String note) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.note = note;
    }

    @Override
    public String toString() {
        return category + " " + amount + "元";
    }
}