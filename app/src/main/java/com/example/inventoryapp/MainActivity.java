package com.example.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventoryapp.DataBase.ProductContract;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, HomeAdapter.OnSellClickedInterface {
    private static final int HOME_DATA_LOADER_ID = 300;
    @BindView(R.id.home_rv)
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    HomeAdapter homeAdapter;
    @BindView(R.id.failed_loading_page)
    View failedLoadingPage;
    @BindView(R.id.fab)
    FloatingActionButton fab;

    @Override
    public void onLongClickedListener(String id) {
        int row = getContentResolver()
                .delete(ContentUris.withAppendedId(ProductContract.Product.CONTENT_URI, Long.parseLong(id)), null, null);
        if (row > 0) {
            Toast.makeText(this, R.string.product_deleted, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.failed_delete_product, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int onSellClicked(String id, int quantity, float p) {
        if (quantity > 0) {
            quantity--;
        } else {
            Toast.makeText(this, R.string.out_of_product, Toast.LENGTH_SHORT).show();
            return 0;
        }

        ContentValues values = new ContentValues();
        values.put(ProductContract.Product.COLUMN_QUANTITY, quantity);
        int rows = getContentResolver()
                .update(ProductContract.Product.CONTENT_URI,
                        values,
                        null,
                        null);
        addToSales(id, p);
        return rows;
    }

    private void addToSales(String id, double price) {
        Uri productUri = ContentUris.withAppendedId(ProductContract.Sales.CONTENT_URI,Long.parseLong(id));
        Cursor product = checkProductInSales(id, productUri);
        if (product != null && product.getCount() > 0) {
            product.moveToFirst();

            float oldPrice = product.getFloat(product.getColumnIndex(ProductContract.Sales.COLUMN_PRICE));
            int oldQuantity = product.getInt(product.getColumnIndex(ProductContract.Sales.COLUMN_QUANTITY));
            ContentValues values1 = new ContentValues();
            values1.put(ProductContract.Sales.COLUMN_PRODUCT_ID, id);
            values1.put(ProductContract.Sales.COLUMN_QUANTITY, oldQuantity + 1);
            values1.put(ProductContract.Sales.COLUMN_PRICE, oldPrice + price);
            getContentResolver().update(productUri, values1, null, null);
        } else {
            ContentValues values1 = new ContentValues();
            values1.put(ProductContract.Sales.COLUMN_PRODUCT_ID, id);
            values1.put(ProductContract.Sales.COLUMN_QUANTITY, 1);
            values1.put(ProductContract.Sales.COLUMN_PRICE, price);
            getContentResolver().insert(ProductContract.Sales.CONTENT_URI, values1);
        }
    }

    private Cursor checkProductInSales(String id, Uri productUri) {
        return getContentResolver().query(productUri,
                new String[]{ProductContract.Sales.COLUMN_PRODUCT_ID, ProductContract.Sales.COLUMN_QUANTITY, ProductContract.Sales.COLUMN_PRICE}
                , null, null, null);
    }

    @OnClick(R.id.fab)
    public void onFabClick(View view) {
        Intent intent = new Intent(MainActivity.this, EditorActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.inventory);
        }
        ButterKnife.bind(this);
        layoutManager = new LinearLayoutManager(this);
        homeAdapter = new HomeAdapter(this, null);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(homeAdapter);
        recyclerView.setHasFixedSize(true);
        getSupportLoaderManager().initLoader(HOME_DATA_LOADER_ID, null, this);

    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {
            case HOME_DATA_LOADER_ID:
                String[] projection = new String[]{
                        ProductContract.Product.COLUMN_ID,
                        ProductContract.Product.COLUMN_TITLE,
                        ProductContract.Product.COLUMN_PRICE,
                        ProductContract.Product.COLUMN_QUANTITY,
                        ProductContract.Product.COLUMN_SUPPLIER,
                        ProductContract.Product.COLUMN_PICTURE
                };
                return new CursorLoader(this, ProductContract.Product.CONTENT_URI, projection, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case HOME_DATA_LOADER_ID:
                if (data == null || data.getCount() == 0) {
                    failedLoadingPage.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);

                } else {
                    failedLoadingPage.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
                homeAdapter.changeData(data);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}