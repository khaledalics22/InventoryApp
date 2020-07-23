package com.example.inventoryapp.DataBase;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ProductProvider extends ContentProvider {
    private ProductDbHelper dbHelper;
    private static final int PRODUCT_URI_CODE = 200;
    private static final int PRODUCT_ID_URI_CODE = 201;
    private static final int SALES_URI_CODE = 202;
    private static final int SALES_ID_URI_CODE = 203;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ProductContract.AUTHORITY, ProductContract.PRODUCT_PATH, PRODUCT_URI_CODE);
        sUriMatcher.addURI(ProductContract.AUTHORITY, ProductContract.PRODUCT_PATH + "/#", PRODUCT_ID_URI_CODE);

        sUriMatcher.addURI(ProductContract.AUTHORITY, ProductContract.SALES_PATH, SALES_URI_CODE);
        sUriMatcher.addURI(ProductContract.AUTHORITY, ProductContract.SALES_PATH + "/#", SALES_ID_URI_CODE);


    }

    @Override
    public boolean onCreate() {
        dbHelper = new ProductDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String tableName;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT_URI_CODE:
                tableName = ProductContract.Product.TABLE_NAME;
                break;
            case PRODUCT_ID_URI_CODE:
                tableName = ProductContract.Product.TABLE_NAME;
                selection = ProductContract.Product.COLUMN_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                break;
            case SALES_URI_CODE:
                tableName = ProductContract.Sales.TABLE_NAME;
                break;
            case SALES_ID_URI_CODE:
                tableName = ProductContract.Sales.TABLE_NAME;
                selection = ProductContract.Sales.COLUMN_PRODUCT_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                break;
            default:
                throw new IllegalArgumentException("Invalid Arguments for uri " + uri);
        }
        cursor = db.query(tableName, projection, selection, selectionArgs, null, null, null);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (values == null || values.size() < 1) {
            throw new IllegalArgumentException("Invalid argument for uri " + uri);
        }
        if (!checkInsertData(uri, values)) {
            throw new IllegalArgumentException("Invalid argument for uri " + uri);
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String tableName;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT_URI_CODE:
                tableName = ProductContract.Product.TABLE_NAME;
                break;
            case SALES_URI_CODE:
                tableName = ProductContract.Sales.TABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException("Invalid Arguments for uri " + uri);
        }
        long id = db.insert(tableName, null, values);
        if (id != -1) {
            Toast.makeText(getContext(), "product saved", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Failed saving product\ntry again", Toast.LENGTH_SHORT).show();
        }
        Uri u = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(uri, null);
        return u;
    }

    private boolean checkInsertData(Uri uri, ContentValues values) {
        int match = sUriMatcher.match(uri);
        if (match == PRODUCT_ID_URI_CODE || match == PRODUCT_URI_CODE) {
            if (!values.containsKey(ProductContract.Product.COLUMN_ID) || !
                    values.containsKey(ProductContract.Product.COLUMN_TITLE))
                return false;
            int id = values.getAsInteger(ProductContract.Product.COLUMN_ID);
            if (id < 0) return false;
            if (values.containsKey(ProductContract.Product.COLUMN_QUANTITY)) {
                int quantity = values.getAsInteger(ProductContract.Product.COLUMN_QUANTITY);
                return quantity >= 0;
            }
        } else {
            return values.containsKey(ProductContract.Sales.COLUMN_PRODUCT_ID)
                    && values.containsKey(ProductContract.Sales.COLUMN_PRICE) &&
                    values.containsKey(ProductContract.Sales.COLUMN_QUANTITY);
        }
        return true;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int rowsNum;
        String tableName;
        switch (match) {
            case PRODUCT_URI_CODE:
                tableName = ProductContract.Product.TABLE_NAME;
                break;
            case PRODUCT_ID_URI_CODE:
                selection = ProductContract.Product.COLUMN_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                tableName = ProductContract.Product.TABLE_NAME;
                break;
            case SALES_URI_CODE:
                tableName = ProductContract.Sales.TABLE_NAME;
                break;
            case SALES_ID_URI_CODE:
                selection = ProductContract.Sales.COLUMN_PRODUCT_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                tableName = ProductContract.Sales.TABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException("invalid uri :" + uri);

        }
        rowsNum = db.delete(tableName, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsNum;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        if (values == null || values.size() < 1) {
            throw new IllegalArgumentException("Invalid argument for uri " + uri);
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int rowsNum;
        switch (match) {
            case PRODUCT_URI_CODE:
                rowsNum = db.update(ProductContract.Product.TABLE_NAME, values, selection, selectionArgs);
                break;
            case PRODUCT_ID_URI_CODE:
                selection = ProductContract.Product.COLUMN_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsNum = db.update(ProductContract.Product.TABLE_NAME, values, selection, selectionArgs);

                break;
            case SALES_URI_CODE:
                rowsNum = db.update(ProductContract.Sales.TABLE_NAME, values, selection, selectionArgs);
                break;
            case SALES_ID_URI_CODE:
                selection = ProductContract.Sales.COLUMN_PRODUCT_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsNum = db.update(ProductContract.Sales.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("invalid uri :" + uri);

        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsNum;
    }
}
