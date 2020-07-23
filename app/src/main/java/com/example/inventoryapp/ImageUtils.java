package com.example.inventoryapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class ImageUtils {
    public static  byte[] getBitmapArray(Bitmap bitmap) {
        ByteArrayOutputStream bais = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, bais);
        return bais.toByteArray();
    }
    public static  Bitmap getBitmap(byte[] array) {
        if(array == null ||array.length==0){return null;}
        return BitmapFactory.decodeByteArray(array,0,array.length);
    }
}
