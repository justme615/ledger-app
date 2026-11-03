package com.example.personal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "personal_ledger.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE = "records";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "type TEXT NOT NULL, " +
                "amount REAL NOT NULL, " +
                "category TEXT NOT NULL, " +
                "date TEXT NOT NULL, " +
                "note TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,
                          int newVersion) {
    }

    public long insertRecord(String type, double amount,
                             String category, String date, String note) {
        ContentValues values = new ContentValues();
        values.put("type", type);
        values.put("amount", amount);
        values.put("category", category);
        values.put("date", date);
        values.put("note", note);

        return getWritableDatabase().insert(TABLE, null, values);
    }

    public ArrayList<LedgerRecord> getAllRecords() {
        ArrayList<LedgerRecord> records = new ArrayList<>();

        try (Cursor cursor = getReadableDatabase().query(
                TABLE, null, null, null, null, null, "id DESC")) {

            while (cursor.moveToNext()) {
                records.add(new LedgerRecord(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("type")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("amount")),
                        cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        cursor.getString(cursor.getColumnIndexOrThrow("date")),
                        cursor.getString(cursor.getColumnIndexOrThrow("note"))
                ));
            }
        }

        return records;
    }

    public double getTotal(String type) {
        double total = 0;

        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT SUM(amount) FROM " + TABLE + " WHERE type = ?",
                new String[]{type})) {

            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                total = cursor.getDouble(0);
            }
        }

        return total;
    }

    public int deleteRecord(int id) {
        return getWritableDatabase().delete(
                TABLE, "id = ?", new String[]{String.valueOf(id)});
    }
}
