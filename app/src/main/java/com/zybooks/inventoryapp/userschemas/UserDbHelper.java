package com.zybooks.inventoryapp.userschemas;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// This class is a database creator for each app/context instance
public class UserDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "User.db";

    private static UserDbHelper instance;

//    private static final class UserLoginTable {
//        private static final String TABLE_NAME = "users";
//        private static final String COLUMN_ID = "_id";
//        private static final String COLUMN_NAME_USERNAME = "username";
//        private static final String COLUMN_NAME_PASSWORD = "password";
//    }

    private UserDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static UserDbHelper getInstance(Context context) {
         if (instance == null) {
             instance = new UserDbHelper(context.getApplicationContext());
         }
         return instance;
    }

    private static final String SQL_CREATE_ENTRIES = "create table " + UserLoginTable.TABLE_NAME + " ( " +
            UserLoginTable.COLUMN_ID + " integer primary key autoincrement, " +
            UserLoginTable.COLUMN_NAME_USERNAME + " text, " +
            UserLoginTable.COLUMN_NAME_PASSWORD + " text)";
    private static final String SQL_DELETE_ENTRIES = "drop table if exists " + UserLoginTable.TABLE_NAME;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
