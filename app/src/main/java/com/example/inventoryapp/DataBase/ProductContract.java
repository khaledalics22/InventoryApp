package com.example.inventoryapp.DataBase;

import android.net.Uri;
import android.provider.BaseColumns;

public class ProductContract {
    public static final String AUTHORITY = "com.example.inventoryapp";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PRODUCT_PATH = "product";
    public static final String SALES_PATH = "sales";

    public static final class Product implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PRODUCT_PATH);
        public static final String COLUMN_ID = "_id";
        public static final String TABLE_NAME = "products";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_SUPPLIER = "supplier";
        public static final String COLUMN_PICTURE = "picture";
    }

    public static final class Sales implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, SALES_PATH);
        public static final String TABLE_NAME = "sales";
        public static final String COLUMN_PRODUCT_ID = "_id";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_PRICE = "total_price";
    }

}