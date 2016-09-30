package com.example.android.inventory;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;

import java.io.ByteArrayOutputStream;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();
    private static final int EXISTING_INVENTORY_LOADER = 0;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 100;
    private static int RESULT_LOAD_IMAGE = 1;

    private Uri mCurrentInventoryUri;
    private EditText mNameEditText;
    private EditText mTotalQuantityEditText;
    private TextView mCurrentQuantityTextView;
    private EditText mSaleQuantityEditText;
    private EditText mPriceEditText;
    private ImageView mPictureImageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Log.d(LOG_TAG, "onCreate");

        Intent intent = getIntent();
        mCurrentInventoryUri = intent.getData();
        Log.d(LOG_TAG, "mCurrentInventoryUri:" + mCurrentInventoryUri);

        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mTotalQuantityEditText = (EditText) findViewById(R.id.edit_inventory_total_quantity);
        mCurrentQuantityTextView = (TextView) findViewById(R.id.edit_inventory_current_quantity);
        mSaleQuantityEditText = (EditText) findViewById(R.id.edit_inventory_sale_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_inventory_price);
        mPictureImageView = (ImageView) findViewById(R.id.imageview_picture);

        if (mCurrentInventoryUri == null) {
            setTitle(getString(R.string.detail_activity_title_new_product));

            View orderButton = findViewById(R.id.button_order);
            orderButton.setVisibility(View.INVISIBLE);
            View deleteButton = findViewById(R.id.button_delete);
            deleteButton.setVisibility(View.INVISIBLE);

        } else {
            setTitle(getString(R.string.detail_activity_title_edit_product));

            // Initialize a loader to read the inventory data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_INVENTORY_LOADER, null, this);
        }
    }

    public void onSelectImage(View view) {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                // app-defined int constant. The callback method gets the
                // result of the request.

            }
        } else {

            Intent intent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            startActivityForResult(intent, RESULT_LOAD_IMAGE);

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    startActivityForResult(intent, RESULT_LOAD_IMAGE);

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            ImageView imageView = (ImageView) findViewById(R.id.imageview_picture);
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

        }
    }

    public void onSave(View view) {

        if (TextUtils.isEmpty(mNameEditText.getText().toString().trim())) {
            Toast.makeText(DetailActivity.this, "Product name is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(mTotalQuantityEditText.getText().toString().trim())) {
            Toast.makeText(DetailActivity.this, "Total Quantity is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(mSaleQuantityEditText.getText().toString().trim())) {
            Toast.makeText(DetailActivity.this, "Sale Quantity is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(mPriceEditText.getText().toString().trim())) {
            Toast.makeText(DetailActivity.this, "Price is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save inventory to database
        saveInventory();
        // Exit activity
        finish();

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
                InventoryEntry.COLUMN_INVENTORY_PRODUCT,
                InventoryEntry.COLUMN_INVENTORY_TOTAL_QUANTITY,
                InventoryEntry.COLUMN_INVENTORY_CURRENT_QUANTITY,
                InventoryEntry.COLUMN_INVENTORY_SALE_QUANTITY,
                InventoryEntry.COLUMN_INVENTORY_PICTURE,
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
            int nameColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRODUCT);
            int totalQuantityColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_TOTAL_QUANTITY);
            int currentQuantityColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_CURRENT_QUANTITY);
            int saleQuantityColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_SALE_QUANTITY);
            int priceColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRICE);
            int pictureColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PICTURE);

            // Extract out the value from the Cursor for the given column index
            String name = data.getString(nameColumnIndex);
            int totalQuantity = data.getInt(totalQuantityColumnIndex);
            int currentQuantity = data.getInt(currentQuantityColumnIndex);
            int saleQuantity = data.getInt(saleQuantityColumnIndex);
            String price = data.getString(priceColumnIndex);
            byte[] picture = data.getBlob(pictureColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mTotalQuantityEditText.setText(Integer.toString(totalQuantity));
            mCurrentQuantityTextView.setText(Integer.toString(currentQuantity));
            mSaleQuantityEditText.setText(Integer.toString(saleQuantity));
            mPriceEditText.setText(price);

            Bitmap pictureBitmap = BitmapFactory.decodeByteArray(picture, 0, picture.length);
            mPictureImageView.setImageBitmap(pictureBitmap);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_detail, menu);
//        return true;
//    }

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
        String totalQuantityString = mTotalQuantityEditText.getText().toString().trim();
        String saleQuantityString = mSaleQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();

        // Create a ContentValues object where column names are the key,
        // and inventory attributes from the editor are the values
        ContentValues contentValues = new ContentValues();
        contentValues.put(InventoryEntry.COLUMN_INVENTORY_PRODUCT, nameString);

        int totalQuantity = 0;
        if (!TextUtils.isEmpty(totalQuantityString)) {
            totalQuantity = Integer.parseInt(totalQuantityString);
        }
        contentValues.put(InventoryEntry.COLUMN_INVENTORY_TOTAL_QUANTITY, totalQuantity);

        int saleQuantity = 0;
        if (!TextUtils.isEmpty(saleQuantityString)) {
            saleQuantity = Integer.parseInt(saleQuantityString);
        }
        contentValues.put(InventoryEntry.COLUMN_INVENTORY_SALE_QUANTITY, saleQuantity);

        int currentQuantity = totalQuantity - saleQuantity;
        contentValues.put(InventoryEntry.COLUMN_INVENTORY_CURRENT_QUANTITY, currentQuantity);

        contentValues.put(InventoryEntry.COLUMN_INVENTORY_PRICE, priceString);
        byte[] byteArray = getBitmapAsByteArray();

        contentValues.put(InventoryEntry.COLUMN_INVENTORY_PICTURE, byteArray);

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
                Toast.makeText(this, getString(R.string.editor_update_inventory_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_inventory_successful),
                        Toast.LENGTH_SHORT).show();
            }

        }

    }

    private byte[] getBitmapAsByteArray() {
        // get image and convert byte array
        mPictureImageView.setDrawingCacheEnabled(true);
        mPictureImageView.buildDrawingCache();
        Bitmap imageBitMap = mPictureImageView.getDrawingCache();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imageBitMap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

}
