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

/**
 * Allows user to create a new inventory or edit an existing one.
 */
public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    // Identifier for the inventory data loader
    private static final int EXISTING_INVENTORY_LOADER = 0;

    // Identifier for the permission to the picture
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 100;

    // Identifier for the result of loading image
    private static int RESULT_LOAD_IMAGE = 1;

    // Content URI for the existing inventory (null if it's a new inventory)
    private Uri mCurrentInventoryUri;

    // EditText field to enter the inventory's name
    private EditText mNameEditText;

    // EditText field to enter the inventory's total quantity
    private EditText mTotalQuantityEditText;

    // TextView field to enter the inventory's current quantity
    private TextView mCurrentQuantityTextView;

    // EditText field to enter the inventory's sale quantity
    private EditText mSaleQuantityEditText;

    // EditText field to enter the inventory's price
    private EditText mPriceEditText;

    // EditText field to enter the inventory's supplier email address
    private EditText mSupplierEmailEditText;

    // ImageView field to enter the inventory's Picture
    private ImageView mPictureImageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Log.d(LOG_TAG, "onCreate");

        Intent intent = getIntent();
        mCurrentInventoryUri = intent.getData();
        Log.d(LOG_TAG, "mCurrentInventoryUri:" + mCurrentInventoryUri);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mTotalQuantityEditText = (EditText) findViewById(R.id.edit_inventory_total_quantity);
        mCurrentQuantityTextView = (TextView) findViewById(R.id.edit_inventory_current_quantity);
        mSaleQuantityEditText = (EditText) findViewById(R.id.edit_inventory_sale_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_inventory_price);
        mSupplierEmailEditText = (EditText) findViewById(R.id.edit_inventory_supplier_email);
        mPictureImageView = (ImageView) findViewById(R.id.imageview_picture);

        // If the intent DOES NOT contain a inventory content URI, then we know that we are
        // creating a new inventory
        if (mCurrentInventoryUri == null) {
            // This is a new inventory, so change the app bar to say "Add a Product"
            setTitle(getString(R.string.detail_activity_title_new_product));

            // Hide the unnecessary button.
            // It doesn't make sense to delete an inventory or order by email that hasn't been
            // created yet.
            View orderButton = findViewById(R.id.button_order);
            orderButton.setVisibility(View.INVISIBLE);
            View deleteButton = findViewById(R.id.button_delete);
            deleteButton.setVisibility(View.INVISIBLE);

        } else {
            // Otherwise this is an existing inventory, so change app bar to say "Edit Product".
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

            // After permission is granted once, we can select image.
            showGallery();

        }

    }

    public void onSave(View view) {

        // User Input is validated
        if (TextUtils.isEmpty(mNameEditText.getText().toString().trim())) {
            Toast.makeText(DetailActivity.this, "Product name is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String totalQuantity = mTotalQuantityEditText.getText().toString().trim();
        if (TextUtils.isEmpty(totalQuantity)) {
            Toast.makeText(DetailActivity.this, "Total Quantity is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!TextUtils.isDigitsOnly(totalQuantity)) {
            Toast.makeText(DetailActivity.this, "Total Quantity must be number", Toast.LENGTH_SHORT).show();
            return;
        }

        String saleQuantity = mSaleQuantityEditText.getText().toString().trim();
        if (TextUtils.isEmpty(saleQuantity)) {
            Toast.makeText(DetailActivity.this, "Sale Quantity is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!TextUtils.isDigitsOnly(saleQuantity)) {
            Toast.makeText(DetailActivity.this, "Sale Quantity must be number", Toast.LENGTH_SHORT).show();
            return;
        }

        String price = mPriceEditText.getText().toString().trim();
        if (TextUtils.isEmpty(price)) {
            Toast.makeText(DetailActivity.this, "Price is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!TextUtils.isDigitsOnly(price)) {
            Toast.makeText(DetailActivity.this, "Price must be number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Integer.parseInt(saleQuantity) > Integer.parseInt(totalQuantity)) {
            Toast.makeText(DetailActivity.this, "Sale quantity must be equal or less than total quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save inventory to database
        saveInventory();

        // Exit activity
        finish();

    }

    public void onOrder(View view) {

        String supplierEmail = mSupplierEmailEditText.getText().toString();
        String productName = mNameEditText.getText().toString();

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{supplierEmail});
        intent.putExtra(Intent.EXTRA_SUBJECT, "order " + productName);
        intent.putExtra(Intent.EXTRA_TEXT, "");
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
                InventoryEntry.COLUMN_INVENTORY_SUPPLIER_EMAIL,
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
            int supplierEmailColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_EMAIL);
            int pictureColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PICTURE);

            // Extract out the value from the Cursor for the given column index
            String name = data.getString(nameColumnIndex);
            int totalQuantity = data.getInt(totalQuantityColumnIndex);
            int currentQuantity = data.getInt(currentQuantityColumnIndex);
            int saleQuantity = data.getInt(saleQuantityColumnIndex);
            String price = data.getString(priceColumnIndex);
            String supplierEmail = data.getString(supplierEmailColumnIndex);
            byte[] picture = data.getBlob(pictureColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mTotalQuantityEditText.setText(Integer.toString(totalQuantity));
            mCurrentQuantityTextView.setText(Integer.toString(currentQuantity));
            mSaleQuantityEditText.setText(Integer.toString(saleQuantity));
            mPriceEditText.setText(price);
            mSupplierEmailEditText.setText(supplierEmail);

            Bitmap pictureBitmap = BitmapFactory.decodeByteArray(picture, 0, picture.length);
            mPictureImageView.setImageBitmap(pictureBitmap);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

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
        String totalQuantityString = mTotalQuantityEditText.getText().toString().trim();
        String saleQuantityString = mSaleQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String supplierEmailString = mSupplierEmailEditText.getText().toString().trim();

        // Create a ContentValues object where column names are the key,
        // and inventory attributes from the detail are the values
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
        contentValues.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_EMAIL, supplierEmailString);

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

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, Do the
                    // contacts-related task you need to do.

                    showGallery();

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

    /**
     * Called when an activity to select image exits.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            // get Content Uri for the image from the gallery
            Uri selectedImage = data.getData();
            Log.d(LOG_TAG, "Uri image: " + selectedImage);

            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            Log.d(LOG_TAG, "picturePath: " + picturePath);

            ImageView imageView = (ImageView) findViewById(R.id.imageview_picture);
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

        }
    }

    private void showGallery() {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(intent, RESULT_LOAD_IMAGE);
    }

}
