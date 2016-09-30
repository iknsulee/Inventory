package com.example.android.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;

public class InventoryDbHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = InventoryDbHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "store.db";
    private static final int DATABASE_VERSION = 1;

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(LOG_TAG, "InventoryDbHelper");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_TAG, "onCreate");

        // Create a String that contains the SQL statement to create the inventory table.
        String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE " + InventoryEntry.TABLE_NAME + " ("
                + InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryEntry.COLUMN_INVENTORY_PRODUCT + " TEXT NOT NULL,"
                + InventoryEntry.COLUMN_INVENTORY_TOTAL_QUANTITY + " INTEGER NOT NULL,"
                + InventoryEntry.COLUMN_INVENTORY_CURRENT_QUANTITY + " INTEGER NOT NULL,"
                + InventoryEntry.COLUMN_INVENTORY_SALE_QUANTITY + " INTEGER,"
                + InventoryEntry.COLUMN_INVENTORY_PRICE + " TEXT NOT NULL"
                + ")";
        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG_TAG, "onUpgrade");

    }
}
