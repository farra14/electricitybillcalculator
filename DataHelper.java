package com.example.electricitybillcalculator;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "electricity_bill.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_BILL = "bill";
    public static final String COL_ID = "id";
    public static final String COL_MONTH = "month";
    public static final String COL_UNITS = "units";
    public static final String COL_TOTAL = "total_charges";
    public static final String COL_REBATE = "rebate";
    public static final String COL_FINAL = "final_cost";

    public DataHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_BILL + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_MONTH + " TEXT NOT NULL, " +
                COL_UNITS + " INTEGER NOT NULL, " +
                COL_TOTAL + " REAL NOT NULL, " +
                COL_REBATE + " INTEGER NOT NULL, " +
                COL_FINAL + " REAL NOT NULL)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BILL);
        onCreate(db);
    }

    public long insertBill(String month, int units, double total, int rebate, double finalCost) {
        SQLiteDatabase db = getWritableDatabase();
        String sql = "INSERT INTO " + TABLE_BILL + "(" +
                COL_MONTH + ", " + COL_UNITS + ", " + COL_TOTAL + ", " +
                COL_REBATE + ", " + COL_FINAL + ") VALUES('" +
                month + "', " + units + ", " + total + ", " + rebate + ", " + finalCost + ")";
        db.execSQL(sql);
        return 1;
    }

    public Cursor getAllBills() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_BILL + " ORDER BY " + COL_ID + " DESC", null);
    }

    public Cursor getBill(int id) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_BILL + " WHERE " + COL_ID + "=?",
                new String[]{String.valueOf(id)});
    }

    public int updateBill(int id, String month, int units, double total, int rebate, double finalCost) {
        SQLiteDatabase db = getWritableDatabase();
        String sql = "UPDATE " + TABLE_BILL + " SET " +
                COL_MONTH + "='" + month + "', " +
                COL_UNITS + "=" + units + ", " +
                COL_TOTAL + "=" + total + ", " +
                COL_REBATE + "=" + rebate + ", " +
                COL_FINAL + "=" + finalCost +
                " WHERE " + COL_ID + "=" + id;
        db.execSQL(sql);
        return 1;
    }

    public int deleteBill(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_BILL + " WHERE " + COL_ID + "=" + id);
        return 1;
    }

}
