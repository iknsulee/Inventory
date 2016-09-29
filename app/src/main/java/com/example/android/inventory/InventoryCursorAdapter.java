package com.example.android.inventory;


import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

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
    public void bindView(View view, Context context, Cursor cursor) {

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

        Button saleButton = (Button)view.findViewById(R.id.button_sale);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v != null) {
                    Log.d(LOG_TAG, "id: " + inventoryId);
                }
            }
        });

    }
}
