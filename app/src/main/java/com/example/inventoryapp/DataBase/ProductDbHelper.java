package com.example.inventoryapp.DataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class ProductDbHelper extends SQLiteOpenHelper {
    private static int version = 1;
    private static final String DB_NAME = "inventoryapp.db";
    private static final String CREATE_PRODUCT_TABLE = "CREATE TABLE " + ProductContract.Product.TABLE_NAME +
            " ( " + ProductContract.Product.COLUMN_ID + " INTEGER PRIMARY KEY" +
            ", " + ProductContract.Product.COLUMN_TITLE + " TEXT NOT NULL" +
            ", " + ProductContract.Product.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0" +
            ", " + ProductContract.Product.COLUMN_PRICE + " REAL NOT NULL DEFAULT 0" +
            ", " + ProductContract.Product.COLUMN_SUPPLIER + " TEXT" +
            ", " + ProductContract.Product.COLUMN_PICTURE + " BLOB )";

    private static final String CREATE_SALES_TABLE = " CREATE TABLE " + ProductContract.Sales.TABLE_NAME +
            "( " + ProductContract.Sales.COLUMN_PRODUCT_ID + " INTEGER NOT NULL " +
            ", " + ProductContract.Sales.COLUMN_QUANTITY + " INTEGER NOT NULL" +
            ", " + ProductContract.Sales.COLUMN_PRICE + " REAL NOT NULL" +
            ", FOREIGN KEY (" + ProductContract.Sales.COLUMN_PRODUCT_ID + ") REFERENCES " +
            ProductContract.Product.TABLE_NAME + "(" + ProductContract.Product.COLUMN_ID + "))";

    private static final String DROP_SALES_TABLE = "DROP TABLE " + ProductContract.Sales.TABLE_NAME;
    private static final String DROP_SHIPMENTS_TABLE = "DROP TABLE " + ProductContract.Product.TABLE_NAME;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PRODUCT_TABLE);
        db.execSQL(CREATE_SALES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_SALES_TABLE);
        db.execSQL(DROP_SHIPMENTS_TABLE);
        version++;
        onCreate(db);
    }

    public ProductDbHelper(@Nullable Context context) {
        super(context, DB_NAME, null, version);
    }
}
