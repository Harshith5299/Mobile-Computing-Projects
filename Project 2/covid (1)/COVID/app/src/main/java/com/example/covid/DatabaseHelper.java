package com.example.covid;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "harshith.db";
    public static final String TABLE_NAME = "harshith_table";
    public static final String COL_1 = "TIMESTAMP";
    public static final String COL_2 = "HRT_RATE";
    public static final String COL_3 = "RSP_RATE";
    public static final String COL_4 = "NAUSEA";
    public static final String COL_5 = "HEADACHE";
    public static final String COL_6 = "DIARRHEA";
    public static final String COL_7 = "SORE_THROAT";
    public static final String COL_8 = "FEVER";
    public static final String COL_9 = "MUSCLE_ACHE";
    public static final String COL_10 = "LOSS_SM_TA";
    public static final String COL_11 = "COUGH";
    public static final String COL_12 = "SHORT_B";
    public static final String COL_13 = "FEEL_TIRED";
    public static final String COL_14 = "LONGITUDE";
    public static final String COL_15 = "LATITUDE";

    public static final Map<String, String> columnToSymptomNameMap = new HashMap<String, String>() {{
        put(COL_4, "Nausea");
        put(COL_5, "Headache");
        put(COL_6, "Diarrhea");
        put(COL_7, "Sore Throat");
        put(COL_8, "Fever");
        put(COL_9, "Muscle Ache");
        put(COL_10, "Loss of Smell or Taste");
        put(COL_11, "Cough");
        put(COL_12, "Shortness of Breath");
        put(COL_13, "Feeling Tired");
    }};


    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " ("+  COL_1+ " INTEGER PRIMARY KEY, "+
                COL_2 + " REAL, " + COL_3 + " REAL, " + COL_4 + " REAL, " +
                COL_5 + " REAL, " + COL_6 + " REAL, " + COL_7 + " REAL, " +
                COL_8 + " REAL, " + COL_9 + " REAL, " + COL_10 + " REAL, " +
                COL_11 + " REAL, " + COL_12 + " REAL, " + COL_13 + " REAL, " +
                COL_14 + " REAL, " + COL_15 + " REAL" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        // Create tables again
        onCreate(db);
    }

    public boolean insertNewRow(long timeStamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        //db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        //onCreate(db);
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, timeStamp);
        contentValues.put(COL_2, 0.0f);
        contentValues.put(COL_3, 0.0f);
        contentValues.put(COL_4, 0.0f);
        contentValues.put(COL_5, 0.0f);
        contentValues.put(COL_6, 0.0f);
        contentValues.put(COL_7, 0.0f);
        contentValues.put(COL_8, 0.0f);
        contentValues.put(COL_9, 0.0f);
        contentValues.put(COL_10, 0.0f);
        contentValues.put(COL_11, 0.0f);
        contentValues.put(COL_12, 0.0f);
        contentValues.put(COL_13, 0.0f);
        contentValues.put(COL_14, 0.0f);
        contentValues.put(COL_15, 0.0f);

        long result = db.insert(TABLE_NAME, null, contentValues);
        db.close();

        return result != -1;
    }

    public boolean updateRowMain(long timestamp, float col2, float col3, float col14, float col15) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, timestamp);
        contentValues.put(COL_2, col2);
        contentValues.put(COL_3, col3);
        contentValues.put(COL_14, col14);
        contentValues.put(COL_15, col15);
        long result = db.update(TABLE_NAME, contentValues, "TIMESTAMP = ?",
                new String[] {String.valueOf(timestamp)});
        db.close();
        return result==1;
    }

    public boolean updateRowSymptoms(long timestamp, Map<String, Float> spinnerRatingMap) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, timestamp);
        contentValues.put(COL_4, spinnerRatingMap.get(columnToSymptomNameMap.get(COL_4)));
        contentValues.put(COL_5, spinnerRatingMap.get(columnToSymptomNameMap.get(COL_5)));
        contentValues.put(COL_6, spinnerRatingMap.get(columnToSymptomNameMap.get(COL_6)));
        contentValues.put(COL_7, spinnerRatingMap.get(columnToSymptomNameMap.get(COL_7)));
        contentValues.put(COL_8, spinnerRatingMap.get(columnToSymptomNameMap.get(COL_8)));
        contentValues.put(COL_9, spinnerRatingMap.get(columnToSymptomNameMap.get(COL_9)));
        contentValues.put(COL_10, spinnerRatingMap.get(columnToSymptomNameMap.get(COL_10)));
        contentValues.put(COL_11, spinnerRatingMap.get(columnToSymptomNameMap.get(COL_11)));
        contentValues.put(COL_12, spinnerRatingMap.get(columnToSymptomNameMap.get(COL_12)));
        contentValues.put(COL_13, spinnerRatingMap.get(columnToSymptomNameMap.get(COL_13)));
        long result = db.update(TABLE_NAME, contentValues, "TIMESTAMP = ?",
                new String[] {String.valueOf(timestamp)});
        db.close();
        return result==1;
    }

    public HashMap<String, Float> readSymptoms(long timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME +" WHERE TIMESTAMP =?",
                new String[] {String.valueOf(timestamp)});

        HashMap<String, Float> spinnerRatingMap = new HashMap<>();
        if (cursor.moveToFirst()) {
            String[] columnNames = cursor.getColumnNames();
            for (int i = 0; i < columnNames.length; i++) {
                if (columnToSymptomNameMap.containsKey(columnNames[i])) {
                    spinnerRatingMap.put(columnToSymptomNameMap.get(columnNames[i]),
                            cursor.getFloat(cursor.getColumnIndex(columnNames[i])));
                }
            }
        }
        cursor.close();
        db.close();

        return spinnerRatingMap;
    }

    public boolean deleteAllRows() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_NAME);
        db.close();
        return true;
    }
}
