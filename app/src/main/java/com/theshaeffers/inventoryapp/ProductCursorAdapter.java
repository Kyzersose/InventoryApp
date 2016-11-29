package com.theshaeffers.inventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.theshaeffers.inventoryapp.data.ProductContract.ProductEntry;

/**
 * Created by shaefferm on 11/26/2016.
 * <p>
 * Adapter for the list that uses a Cursor of product data as its source.
 */

public class ProductCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new ProductCursorAdapter
     *
     * @param context
     * @param c
     */

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    /**
     * Creates a new list with NO DATA
     *
     * @param context
     * @param cursor
     * @param parent
     * @return
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        //Find the Views
        TextView productNameView = (TextView) view.findViewById(R.id.list_item_name);
        final TextView productInStockView = (TextView) view.findViewById(R.id.list_item_quantity);
        TextView productItemPriceView = (TextView) view.findViewById(R.id.list_item_price);
        ImageButton salesListButton = (ImageButton) view.findViewById(R.id.salesListButton);

        //Find the columns from the products tables
        int idColumnIndex = cursor.getColumnIndex(ProductEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);

        //Pull attributes from the products db
        int id = cursor.getInt(idColumnIndex);
        String productName = cursor.getString(nameColumnIndex);
        int productQuantity = cursor.getInt(quantityColumnIndex);
        int productPrice = cursor.getInt(priceColumnIndex);
        String productQuantityString = String.valueOf(productQuantity);
        String productPriceString = "Price: $" + productPrice;


        //Set the attributes to their textviews
        productNameView.setText(productName);
        productInStockView.setText(productQuantityString);
        productItemPriceView.setText(productPriceString);

        salesListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Read the value, pulled from the db and inserted into productInStockView
                int listQuantity = Integer.valueOf(productInStockView.getText().toString());
                //If there's more than 0 items, decrement listQuantity
                if (listQuantity > 0) {
                    listQuantity--;
                    //Insert the new value into the productInStockView
                    productInStockView.setText(String.valueOf(listQuantity));

                    //Update the ContentProvider
                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, listQuantity);
                    context.getContentResolver().update(ProductEntry.CONTENT_URI,
                            values, "_id == " +
                                    Integer.toString(cursor.getInt(cursor.getColumnIndex(ProductEntry._ID))), null);
                } else {
                    Toast.makeText(context, R.string.out_of_stock, Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
}
