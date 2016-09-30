package com.example.android.inventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int MAIN_LOADER = 0;
    private InventoryCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(LOG_TAG, "onCreate");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(MainActivity.this, "hello", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                startActivity(intent);
            }
        });

        ListView listView = (ListView) findViewById(R.id.list);
        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);

//        final ArrayList<Inventory> inventories = new ArrayList<>();
//        for (int i = 0; i < 100; i++) {
//            inventories.add(new Inventory("name" + i, i));
//        }
//        InventoryAdapter inventoryAdapter = new InventoryAdapter(this, inventories);
//        listView.setAdapter(inventoryAdapter);

        mCursorAdapter = new InventoryCursorAdapter(this, null);
        listView.setAdapter(mCursorAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Log.d(LOG_TAG, "name: " + inventories.get(position).getName() +
//                        ", position: " + position + ", id: " + id);
                Log.d(LOG_TAG, "position: " + position + ", id: " + id);

                // Create new intent to go to {@link DetailActivity}
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);

                Uri currentInventoryUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

                intent.setData(currentInventoryUri);

                // Launch the DetailActivity to display the data for the current inventory
                startActivity(intent);

            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(MAIN_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_list.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_delete_all_entries:
                deleteAllInventories();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void reduceQuantity(int id) {
        Log.d(LOG_TAG, "reduceQuantity: " + id);

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

        Cursor data = getContentResolver().query(currentInventoryUri, projection, null, null, null);
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
                Toast.makeText(this, "No inventories", Toast.LENGTH_SHORT).show();
                return;
            }

            saleQuantity += 1;
            contentValues.put(InventoryEntry.COLUMN_INVENTORY_SALE_QUANTITY, saleQuantity);
            currentQuantity = totalQuantity - saleQuantity;
            contentValues.put(InventoryEntry.COLUMN_INVENTORY_CURRENT_QUANTITY, currentQuantity);
            int rowsAffected = getContentResolver().update(currentInventoryUri, contentValues, null, null);
        }


    }

    private void deleteAllInventories() {
        int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
        Log.d(LOG_TAG, rowsDeleted + " rows deleted from inventory database");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "onCreateLoader");

        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_INVENTORY_PRODUCT,
                InventoryEntry.COLUMN_INVENTORY_TOTAL_QUANTITY,
                InventoryEntry.COLUMN_INVENTORY_CURRENT_QUANTITY,
                InventoryEntry.COLUMN_INVENTORY_SALE_QUANTITY,
                InventoryEntry.COLUMN_INVENTORY_PRICE
        };

        return new CursorLoader(
                this,                           // Parent activity context
                InventoryEntry.CONTENT_URI,     // Provider content URI to query
                projection,                     // Columns to include in the resulting Cursor
                null,                           // No selection clause
                null,                           // No selection arguments
                null                            // Default sort order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(LOG_TAG, "onLoadFinished");

        // Update {@link InventoryCursorAdapter} with this new cursor containing updated pet data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(LOG_TAG, "onLoaderReset");

        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
