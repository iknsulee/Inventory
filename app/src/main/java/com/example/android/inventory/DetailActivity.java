package com.example.android.inventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();
    private static final int EXISTING_INVENTORY_LOADER = 0;

    private Uri mCurrentInventoryUri;
    private EditText mNameEditText;
    private EditText mCurrentQuantityEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Log.d(LOG_TAG, "onCreate");

        Intent intent = getIntent();
        mCurrentInventoryUri = intent.getData();
        Log.d(LOG_TAG, "mCurrentInventoryUri:" + mCurrentInventoryUri);

        if (mCurrentInventoryUri == null) {
            setTitle("Add a Inventory");

            // TODO: 2016-09-29 hide button
        } else {
            setTitle("Edit Inventory");

            // Initialize a loader to read the inventory data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_INVENTORY_LOADER, null, this);
        }

        mNameEditText = (EditText) findViewById(R.id.edit_inventory_name);
        mCurrentQuantityEditText = (EditText) findViewById(R.id.edit_inventory_current_quantity);

    }

    public void onOrder(View view) {
        composeEmail(new String[]{"a@b.c.kr"}, "order");
    }

    public void composeEmail(String[] addresses, String subject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, "this is text");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void onDelete(View view) {

        showDeleteConfirmationDialog();

    }

    /**
     * Prompt the user to confirm that they want to delete this inventory.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteInventory();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    /**
     * Perform the deletion of the pet in the database.
     */
    private void deleteInventory() {
        // Only perform the delete if this is an existing inventory.
        if (mCurrentInventoryUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentInventoryUri
            // content URI already identifies the inventory that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentInventoryUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_inventory_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_inventory_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_INVENTORY_NAME,
                InventoryEntry.COLUMN_INVENTORY_CURRENT_QUANTITY,
                InventoryEntry.COLUMN_INVENTORY_SALE_QUANTITY,
                InventoryEntry.COLUMN_INVENTORY_PRICE
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,
                mCurrentInventoryUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (data == null || data.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data fro it
        // (This should be the only row in the cursor)
        if (data.moveToFirst()) {
            // Find the column of inventory attributes that we're interested in
            int nameColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_NAME);
            int currentQuantityColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_CURRENT_QUANTITY);

            // Extract out the value from the Cursor for the given column index
            String name = data.getString(nameColumnIndex);
            int currentQuantity = data.getInt(currentQuantityColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mCurrentQuantityEditText.setText(Integer.toString(currentQuantity));
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save inventory to database
                saveInventory();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Get user input from editor and save inventory into database
     */
    private void saveInventory() {

        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String currentQuantityString = mCurrentQuantityEditText.getText().toString().trim();

        // Create a ContentValues object where column names are the key,
        // and inventory attributes from the editor are the values
        ContentValues contentValues = new ContentValues();
        contentValues.put(InventoryEntry.COLUMN_INVENTORY_NAME, nameString);

        int currentQuantity = 0;
        if (!TextUtils.isEmpty(currentQuantityString)) {
            currentQuantity = Integer.parseInt(currentQuantityString);
        }
        contentValues.put(InventoryEntry.COLUMN_INVENTORY_CURRENT_QUANTITY, currentQuantity);

        // TODO: 2016-09-29 임시로 작성
        contentValues.put(InventoryEntry.COLUMN_INVENTORY_SALE_QUANTITY, 5);
        contentValues.put(InventoryEntry.COLUMN_INVENTORY_PRICE, "$100");

        // Determine if this is a new or existing inventory by checking if mCurrentInventoryUri
        // is null or not
        if (mCurrentInventoryUri == null) {
            // This is a NEW inventory, so insert a new inventory into the provider
            // returning the content URI for the new inventory.
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, contentValues);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_inventory_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_inventory_successful),
                        Toast.LENGTH_SHORT).show();
            }

        } else {
            // Otherwise this is an existing inventory, so update the inventory with content URI:
            // mCurrentInventoryUri and pass in the new ContentValues. Pass in null for the selection
            // and selection args because mCurrentInventoryUri will already identity the correct
            // row in the database that we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentInventoryUri, contentValues, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }

        }

    }
}
