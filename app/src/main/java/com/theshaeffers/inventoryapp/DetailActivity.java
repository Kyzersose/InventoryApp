package com.theshaeffers.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.theshaeffers.inventoryapp.data.ProductContract.ProductEntry;

import java.io.ByteArrayOutputStream;

// Activity used by user to create a new product or edit an existing one

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the product data loader
     */
    private static final int EXISTING_PRODUCT_LOADER = 0;

    /**
     * Identifier for the image capture
     */
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    /**
     * Content URI for the existing product (null if it's a new product)
     */
    private Uri mCurrentProductUri;

    /**
     * ImageButton for the product image
     */
    private ImageButton mProductPicture;

    /**
     * EditText field to enter the product name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the product price
     */
    private EditText mPriceEditText;

    /**
     * TextView field for the product quantity
     */
    private EditText mQuantityEditText;

    /**
     * TextView used to Quick Order (increment quantity by 1)
     */
    private TextView mQuickOrder;

    /**
     * TextView used to make a Sale (decrement quantity by 1 )
     */
    private TextView mSell;

    /**
     * TextView used to make an intent to an email to a supplier, passing intent
     */
    private TextView mBulkOrder;

    /**
     * Used to track if a picture has been taken
     */
    private boolean mImageTaken;

    /**
     * Bitmap used to track the image across methods
     */
    Bitmap bitmap;

    /**
     * byte used to store bitmap when converting and storing in the db
     */
    byte[] bytes;

    /**
     * Boolean flag that keeps track of whether the product has been edited
     */
    private boolean mProductHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mProductHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new product or editing an existing one.
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // If the intent DOES NOT contain a product content URI, then we know that we are
        // creating a new product.
        if (mCurrentProductUri == null) {
            /**
             * New Product Detail Activity
             */
            // This is a new product, so change the app bar to say "Add a Product"
            setTitle(getString(R.string.editor_activity_title_new_product));

            //This is a new product, so the user will not need the Bulk Order, Sell, or Quick Order
            mQuickOrder = (TextView) findViewById(R.id.quick_order_view);
            mSell = (TextView) findViewById(R.id.sell_view);
            mBulkOrder = (TextView) findViewById(R.id.bulk_order_view);
            mQuickOrder.setVisibility(View.GONE);
            mSell.setVisibility(View.GONE);
            mBulkOrder.setVisibility(View.GONE);

            //Find the views the user WILL edit
            //Find the views the user can edit
            mNameEditText = (EditText) findViewById(R.id.edit_product_name);
            mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
            mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity);

            //Set the onClickListeners
            mNameEditText.setOnTouchListener(mTouchListener);
            mPriceEditText.setOnTouchListener(mTouchListener);
            mQuantityEditText.setOnTouchListener(mTouchListener);

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a product that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            /**
             * Existing Product Detail Activity
             */
            // Otherwise this is an existing product, so change app bar to say "Edit product"
            setTitle(getString(R.string.editor_activity_title_edit_product));

            //Find the views the user can edit
            mNameEditText = (EditText) findViewById(R.id.edit_product_name);
            mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
            mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity);
            mQuickOrder = (TextView) findViewById(R.id.quick_order_view);
            mSell = (TextView) findViewById(R.id.sell_view);
            mBulkOrder = (TextView) findViewById(R.id.bulk_order_view);

            // Setup OnTouchListeners on all the input fields, so we can determine if the user
            // has touched or modified them. This will let us know if there are unsaved changes
            // or not, if the user tries to leave the editor without saving.
            mNameEditText.setOnTouchListener(mTouchListener);
            mPriceEditText.setOnTouchListener(mTouchListener);
            mQuantityEditText.setOnTouchListener(mTouchListener);
            mQuickOrder.setOnTouchListener(mTouchListener);
            mSell.setOnTouchListener(mTouchListener);
            mBulkOrder.setOnTouchListener(mTouchListener);

            // Initialize a loader to read the product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);

            //ClickListener for Quick Order, increments quantity
            mQuickOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int currentQuantity = Integer.parseInt(mQuantityEditText.getText().toString());
                    currentQuantity++;
                    mQuantityEditText.setText("" + currentQuantity);
                }
            });

            //ClickListener for Sell button, decrements quantity
            mSell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int currentQuantity = Integer.parseInt(mQuantityEditText.getText().toString());
                    if (currentQuantity <= 1) {
                        Toast.makeText(DetailActivity.this, "Inventory running low, order more.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        currentQuantity--;
                        mQuantityEditText.setText("" + currentQuantity);
                    }
                }
            });

            //ClickListener for Bulk Order
            //Sends an email populated with product name
            mBulkOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent bulkIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",
                            "Sebastian@UdacityMegaSuppliers.com", null));
                    bulkIntent.putExtra(Intent.EXTRA_SUBJECT, "Bulk order");
                    bulkIntent.putExtra(Intent.EXTRA_TEXT, "Please order our standard bulk" +
                            " shipment of: \n\n" + mNameEditText.getText().toString() +
                            "\n\n Thanks,\n\nSuper Genius");
                    if (bulkIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(bulkIntent);
                    }
                }
            });


        }
        // Find the image button view
        mProductPicture = (ImageButton) findViewById(R.id.imageButton);
        mProductPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

    }

    //This is called when the camera returns to the DetailActivity
    //Sets the Bitmap recieved as the ImageButton drawable.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mProductPicture.setImageBitmap(imageBitmap);
            mImageTaken = true;
        }
    }

    //Picture taker method
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }

    }

    // convert from bitmap to byte array
    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    //Grab input from user
    private void saveProduct() {
        //Read the input from the fields and trim them
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();

        //Check if an image has been taken
        if (mImageTaken) {
            bitmap = ((BitmapDrawable) mProductPicture.getDrawable()).getBitmap();
            bytes = getBytes(bitmap);
        }

        //Check if it's really a new product. Check if any fields are null.
        if (mCurrentProductUri == null && TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(priceString) && TextUtils.isEmpty(quantityString)) {
            //No fields were changed, no need to update the name or price fields
            return;
        }

        //Create ContentValues. column names = keys, product attributes = values
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceString);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityString);
        if (mImageTaken) {
            values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, bytes);
        }

        //Determine if this is a new or existing product, check if mCurrentProduct is null
        if (mCurrentProductUri == null) {
            // This is a NEW product, so insert a new product into the provider,
            // returning the content URI for the new product.
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING product, so update the product with content URI:
            // mCurrentProductUri and pass in the new ContentValues. Pass in null for the selection
            // and selection args because mCurrentProductUri will already identify the correct
            // row in the database that we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.edit_product_menu, menu);
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save product to database
                saveProduct();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
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
     * Prompt the user to confirm that they want to delete this product.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
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
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //Define a projection that contains all the columns
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_IMAGE};

        //Loader executes on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,     // Query the content URI for the current product
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //Stop if the cursor is null or there is < 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        //Move to the first row in the cursor
        if (cursor.moveToFirst()) {
            //find the columns
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);

            //Extract the values from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            byte[] pictureByte = cursor.getBlob(imageColumnIndex);

            //Update the views with the new info
            mNameEditText.setText(name);
            mPriceEditText.setText(Integer.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));
            if (pictureByte != null) {
                mProductPicture.setImageBitmap(getImage(pictureByte));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //If loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
    }
}