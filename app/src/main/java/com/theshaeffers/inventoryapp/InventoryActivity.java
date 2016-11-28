package com.theshaeffers.inventoryapp;

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

import com.theshaeffers.inventoryapp.data.ProductContract.ProductEntry;

import static android.R.attr.excludeId;
import static android.R.attr.id;
import static android.R.attr.start;


public class InventoryActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the product data loader
     */
    private static final int PRODUCT_LOADER = 0;

    /**
     * Adapter for the ListView
     */
    ProductCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inventory_activity);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InventoryActivity.this, DetailActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the product data
        ListView productListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);

        //Setup an Adapter for the list
        mCursorAdapter = new ProductCursorAdapter(this, null);
        productListView.setAdapter(mCursorAdapter);

        //Set a click listener for the list items
        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //Create an intent
                Intent intent = new Intent(InventoryActivity.this, DetailActivity.class);

                //Create the URI with the given list item id
                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

                //Set the Uri
                intent.setData(currentProductUri);

                //Pass the item id
                intent.putExtra("id", id);

                //Start the detail activity
                startActivity(intent);
            }

        });

        //Start the loader
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    /**
     * Used to insert hardcoded product data into the database. From insert dummy data menu option.
     */
    private void insertProduct() {
        // Create a ContentValues object where column names are the keys,
        // and Toto's pet attributes are the values.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, "Google Pixel");
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, 649);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, 100);

        // Insert a new row for dummy data into the provider using the ContentResolver.
        // Receive the new content URI that will allow us to access dummy data in the future.
        Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
    }

    //Deletes all the rows from the products table
    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
        Log.v("InventoryActivity", rowsDeleted + " rows deleted from products database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/inventory_menu.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.inventory_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertProduct();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllProducts();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                ProductEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link ProductCursorAdapter} with this new cursor containing updated product data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}

