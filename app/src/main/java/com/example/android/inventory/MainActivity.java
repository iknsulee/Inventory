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

/**
 * Displays list of inventories that were entered and stored in the app.
 */
public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    // Identifier for the inventory data loader
    private static final int MAIN_LOADER = 0;

    // Adapter for the ListView
    private InventoryCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(LOG_TAG, "onCreate");

        // Setup FAB to open DetailActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the inventory data
        ListView listView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView,
        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of inventory data in the Cursor
        // There is no inventory data yet(until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new InventoryCursorAdapter(this, null);
        listView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(LOG_TAG, "position: " + position + ", id: " + id);

                // Create new intent to go to DetailActivity
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);

                // Form the content URI that represents the specific inventory that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // InventoryEntry.CONTENT_URI.
                // For example, the URI would be
                // "content://com.example.android.inventory/inventories/2"
                // if the inventory with ID 2 was clicked on.
                Uri currentInventoryUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentInventoryUri);

                // Launch the DetailActivity to display the data for the current inventory
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(MAIN_LOADER, null, this);
    }

    /**
     * Helper method to delete all inventories in the database.
     */
    private void deleteAllInventories() {
        int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
        Log.d(LOG_TAG, rowsDeleted + " rows deleted from inventory database");
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

        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Response to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllInventories();
                break;
        }

        return super.onOptionsItemSelected(item);
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

        // This loader will execute the ContentProvider's query method on a background thread
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

        // Update InventoryCursorAdapter with this new cursor containing updated inventory data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(LOG_TAG, "onLoaderReset");

        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
