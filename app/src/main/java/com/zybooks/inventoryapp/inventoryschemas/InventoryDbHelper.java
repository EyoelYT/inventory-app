package com.zybooks.inventoryapp.inventoryschemas;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class InventoryDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "Inventory.db";
    private static InventoryDbHelper instance;

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + InventoryTable.TABLE_NAME + " (" +
                    InventoryTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    InventoryTable.COLUMN_NAME_TITLE + " TEXT," +
                    InventoryTable.COLUMN_NAME_IMAGE + " TEXT," +
                    InventoryTable.COLUMN_NAME_QUANTITY + " INTEGER," +
                    InventoryTable.COLUMN_NAME_DESCRIPTION + " TEXT)";

    private InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static InventoryDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new InventoryDbHelper(context.getApplicationContext());
        }
            return instance;
    }

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + InventoryTable.TABLE_NAME;

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }
}
