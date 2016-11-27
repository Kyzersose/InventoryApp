package com.theshaeffers.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.theshaeffers.inventoryapp.data.ProductContract.ProductEntry;

/**
 * Created by shaefferm on 11/26/2016.
 *
 * Adapter for the list that uses a Cursor of product data as its source.
 */

public class ProductCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new ProductCursorAdapter
     * @param context
     * @param c
     */
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Creates a new list with NO DATA
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
    public void bindView(View view, Context context, Cursor cursor) {

        //Find the TextViews
        TextView productNameView = (TextView) view.findViewById(R.id.list_item_name);
        TextView productInStockView = (TextView) view.findViewById(R.id.list_item_quantity);
        TextView productItemPriceView = (TextView) view.findViewById(R.id.list_item_price);

        //Find the columns from the products tables
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);

        //Pull attributes from the products db
        String productName = cursor.getString(nameColumnIndex);
        int productQuantity = cursor.getInt(quantityColumnIndex);
        int productPrice = cursor.getInt(priceColumnIndex);

        //Set the attributes to their textviews
        productNameView.setText(productName);
        productInStockView.setText(R.string.list_in_stock + productQuantity);
        productItemPriceView.setText(productPrice);

    }
}
