package com.example.android.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;

/**
 * Database helper for Inventory app. Manages database creation and version management.
 */
public class InventoryDbHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = InventoryDbHelper.class.getSimpleName();

    // Name of the database file
    private static final String DATABASE_NAME = "store.db";

    // Database version. If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of InventoryDbHelper
     *
     * @param context of the app
     */
    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(LOG_TAG, "InventoryDbHelper");
    }

    /**
     * This is called when the database is created for the first time.
     */
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
                + InventoryEntry.COLUMN_INVENTORY_PICTURE + " BLOB,"
                + InventoryEntry.COLUMN_INVENTORY_SUPPLIER_EMAIL + " TEXT,"
                + InventoryEntry.COLUMN_INVENTORY_PRICE + " TEXT NOT NULL"
                + ")";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    /**
     * This is called when the database is needed to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG_TAG, "onUpgrade");

        // The database is still at version 1, so there's nothing to do be done here.
    }
}
