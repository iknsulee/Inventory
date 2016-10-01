package com.example.android.inventory;


import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;

public class InventoryCursorAdapter extends CursorAdapter {

    private static final String LOG_TAG = InventoryCursorAdapter.class.getSimpleName();

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView currentQuantityTextView = (TextView) view.findViewById(R.id.current_quantity);
        TextView saleQuantityTextView = (TextView) view.findViewById(R.id.sale_quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);

        // Find the columns of inventory attributes that we're interested in.
        int idColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRODUCT);
        int currentQuantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_CURRENT_QUANTITY);
        int saleQuantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_SALE_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRICE);

        final int inventoryId = cursor.getInt(idColumnIndex);
        String inventoryName = cursor.getString(nameColumnIndex);
        int inventoryCurrentQuantity = cursor.getInt(currentQuantityColumnIndex);
        int inventorySaleQuantity = cursor.getInt(saleQuantityColumnIndex);
        String inventoryPrice = cursor.getString(priceColumnIndex);

        nameTextView.setText("name: " + inventoryName);
        currentQuantityTextView.setText("current quantity: " + inventoryCurrentQuantity);
        saleQuantityTextView.setText("sale quantity: " + inventorySaleQuantity);
        priceTextView.setText("price: " + inventoryPrice);

        Button saleButton = (Button) view.findViewById(R.id.button_sale);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v != null) {
                    Log.d(LOG_TAG, "id: " + inventoryId);

                    reduceQuantity(context, inventoryId);
                }
            }
        });

    }

    private void reduceQuantity(Context context, int id) {
        Log.d(LOG_TAG, "click: " + id);

        Uri currentInventoryUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_INVENTORY_PRODUCT,
                InventoryEntry.COLUMN_INVENTORY_TOTAL_QUANTITY,
                InventoryEntry.COLUMN_INVENTORY_CURRENT_QUANTITY,
                InventoryEntry.COLUMN_INVENTORY_SALE_QUANTITY,
                InventoryEntry.COLUMN_INVENTORY_PRICE
        };

        Log.d(LOG_TAG, "currentInventoryUri: " + currentInventoryUri);

        Cursor data = context.getContentResolver().query(currentInventoryUri, projection, null, null, null);
        if (data.moveToFirst()) {
            // Find the column of inventory attributes that we're interested in
            int nameColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRODUCT);
            int totalQuantityColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_TOTAL_QUANTITY);
            int currentQuantityColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_CURRENT_QUANTITY);
            int saleQuantityColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_SALE_QUANTITY);
            int priceColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRICE);

            // Extract out the value from the Cursor for the given column index
            String name = data.getString(nameColumnIndex);
            int totalQuantity = data.getInt(totalQuantityColumnIndex);
            int currentQuantity = data.getInt(currentQuantityColumnIndex);
            int saleQuantity = data.getInt(saleQuantityColumnIndex);
            String price = data.getString(priceColumnIndex);

            Log.d(LOG_TAG, "name: " + name);
            Log.d(LOG_TAG, "totalQuantity: " + totalQuantity);
            Log.d(LOG_TAG, "currentQuantity: " + currentQuantity);
            Log.d(LOG_TAG, "saleQuantity: " + saleQuantity);
            Log.d(LOG_TAG, "price: " + price);

            // Create a ContentValues object where column names are the key,
            // and inventory attributes from the editor are the values
            ContentValues contentValues = new ContentValues();

            if (currentQuantity == 0) {
                Toast.makeText(context, "No inventories", Toast.LENGTH_SHORT).show();
                return;
            }

            saleQuantity += 1;
            contentValues.put(InventoryEntry.COLUMN_INVENTORY_SALE_QUANTITY, saleQuantity);
            currentQuantity = totalQuantity - saleQuantity;
            contentValues.put(InventoryEntry.COLUMN_INVENTORY_CURRENT_QUANTITY, currentQuantity);
            int rowsUpdated = context.getContentResolver().update(currentInventoryUri, contentValues, null, null);
            Log.d(LOG_TAG, rowsUpdated + " rows updated from inventory database");
        }

    }

}
