package com.zybooks.inventoryapp.userviewmodel;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zybooks.inventoryapp.userschemas.UserLoginTable;
import com.zybooks.inventoryapp.userschemas.UserDbHelper;

public class UserHandler {
    private UserDbHelper dbHelper;

    public UserHandler(Context context) {
        dbHelper = UserDbHelper.getInstance(context);
    }

    public long addUser(String username, String password) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Check if the user already exists
        String[] columns = { UserLoginTable.COLUMN_NAME_USERNAME };
        String selection = UserLoginTable.COLUMN_NAME_USERNAME + " = ?";
        String[] selectionArgs = { username };

        Cursor cursor = db.query(
                UserLoginTable.TABLE_NAME, columns, selection, selectionArgs,
                null, null, null
        );

        int userCount = cursor.getCount();
        cursor.close();
        if (userCount > 0) {return -1;}
        else {
            ContentValues values = new ContentValues();

            values.put(UserLoginTable.COLUMN_NAME_USERNAME, username);
            values.put(UserLoginTable.COLUMN_NAME_PASSWORD, password);

            return db.insert(UserLoginTable.TABLE_NAME, null, values);
        }
    }

    public boolean validateUser(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                UserLoginTable.COLUMN_NAME_USERNAME,
                UserLoginTable.COLUMN_NAME_PASSWORD
        };
        String selection        = UserLoginTable.COLUMN_NAME_USERNAME + " = ?";
        String[] selectionargs  = {username};

        Cursor cursor = db.query(UserLoginTable.TABLE_NAME, projection, selection, selectionargs,
                null, null, null);

        // If a username match is found, compare the passwords
        if (cursor.moveToFirst()) {
            String dbPassword = cursor.getString(cursor.getColumnIndexOrThrow(UserLoginTable.COLUMN_NAME_PASSWORD));
            cursor.close();
            return dbPassword.equals(password);
        } else {
            cursor.close();
            return false;
        }
    }
}
