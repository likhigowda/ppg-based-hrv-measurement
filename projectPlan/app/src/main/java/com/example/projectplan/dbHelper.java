package com.example.projectplan;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class dbHelper extends SQLiteOpenHelper {

    public static final String USER_DATA_TABLE = "userData_table";
    public static final String COLUMN_HR = "hr";
    public static final String COLUMN_HRV = "hrv";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_LABEL = "label";

    public dbHelper(@Nullable Context context) {
        super(context, "userData.db", null, 1);
    }

    // this is called when database is created for first time
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement = "CREATE TABLE " + USER_DATA_TABLE + " (" + COLUMN_HR + " VARCHAR(30) NOT NULL, " + COLUMN_HRV + " VARCHAR(30) NOT NULL, " + COLUMN_TIME + " VARCHAR(50) NOT NULL, " + COLUMN_LABEL + " VARCHAR(45) NOT NULL DEFAULT '-')";

        db.execSQL(createTableStatement);

    }

    // this is called if the database version is changed
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean addOne(MainActivity2 data) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_HR, data.hrOnly);
        cv.put(COLUMN_HRV, data.hrvOnly);
        cv.put(COLUMN_TIME, data.dateTime);
        cv.put(COLUMN_LABEL, data.label.getText().toString());

        long insert = db.insert(USER_DATA_TABLE, null, cv);

        if (insert == -1) {
            return false;
        }
        else {
            return true;
        }
    }

    public List<String> getEverything() {
        ArrayList<String> result = new ArrayList<>();
        String queryString = "SELECT * FROM " + USER_DATA_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString,null);

        if (cursor.moveToFirst()) {
            do {

                String Time = cursor.getString(2);
                String HR = cursor.getString(0);
                String HRV = cursor.getString(1);
                String LABEL = cursor.getString(3);

                if (LABEL == null || LABEL.length() == 0) {
                    result.add("\n" + Time + "\n" +
                            "HR: " + HR + "\n" +
                            "HRV: " + HRV + "\n");

                }
                else {
                    result.add("\n" + Time + "\n" + "Label: " + LABEL + "\n" +
                            "HR: " + HR + "\n" +
                            "HRV: " + HRV + "\n");
                }



            } while (cursor.moveToNext());

        }
        return result;
    }

    public void deleteData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ USER_DATA_TABLE);
    }
}

