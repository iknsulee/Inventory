package com.example.android.inventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the inventory app
 */
public class InventoryContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor
    private InventoryContract() {
    }

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website. A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventory";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which app whill use to contact
     * the content provider
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     */
    public static final String PATH_INVENTORIES = "inventories";

    /**
     * Inner class that defines constant values for the inventory database table.
     * Each entry in the table represents a single inventory.
     */
    public static final class InventoryEntry implements BaseColumns {

        /**
         * The content URI to access the inventory data in the provider
         */
        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORIES);

        /**
         * The MIME type of the CONTENT_URI for a list of inventories
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/"
                        + PATH_INVENTORIES;

        /**
         * The MIME type of the CONTENT_URI for a single inventory.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/"
                        + PATH_INVENTORIES;

        /**
         * Name of database table for inventory
         */
        public static final String TABLE_NAME = "inventory";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_INVENTORY_PRODUCT = "product";
        public static final String COLUMN_INVENTORY_TOTAL_QUANTITY = "total_quantity";
        public static final String COLUMN_INVENTORY_CURRENT_QUANTITY = "current_quantity";
        public static final String COLUMN_INVENTORY_SALE_QUANTITY = "sale_quantity";
        public static final String COLUMN_INVENTORY_PRICE = "price";
        public static final String COLUMN_INVENTORY_PICTURE = "picture";
        public static final String COLUMN_INVENTORY_SUPPLIER_EMAIL = "supplier_email";
    }


}
