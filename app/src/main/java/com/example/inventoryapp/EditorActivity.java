package com.example.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.example.inventoryapp.DataBase.ProductContract;

import java.io.FileNotFoundException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import butterknife.OnTouch;

public class EditorActivity extends AppCompatActivity{
    private static final int LOAD_IMAGE_CODE = 100;
    private static boolean productHasChanged = false;
    @BindView(R.id.product_name_et)
    EditText productName;
    @BindView(R.id.product_supplier_et)
    EditText productSupplier;
    @BindView(R.id.product_quantity_et)
    EditText productQuantity;
    @BindView(R.id.product_price_et)
    EditText productPrice;
    @BindView(R.id.product_id_et)
    EditText productId;
    @BindView(R.id.add_image_btn)
    ImageButton addImage;
    @BindView(R.id.product_img_et)
    EditText imagePath;
    @BindView(R.id.img_iv)
    ImageView chosenImage;
    Bitmap bitmapImage;

    @OnTextChanged(R.id.product_id_et)
    public void onIdChanged(CharSequence s, int start, int before, int count) {
        String requestedId = s.toString();
        String[] projection = new String[]{ProductContract.Product.COLUMN_TITLE};
        Cursor data =  getContentResolver().query(ContentUris.withAppendedId(ProductContract.Product.CONTENT_URI, Long.parseLong(requestedId)),
                projection, null, null, null);
        if (data!=null && data.getCount() > 0) {
            data.moveToFirst();
            String name = data.getString(data.getColumnIndex(ProductContract.Product.COLUMN_TITLE));
            Toast.makeText(this, "this Id (" + requestedId + ") exists for product " + name, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.add_image_btn)
    public void onAddImageClick(View view) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        //photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, LOAD_IMAGE_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == LOAD_IMAGE_CODE) {
            if (data != null) {
                Uri image = data.getData();
                if (image == null) {
                    Toast.makeText(this, getString(R.string.failed_to_load_image), Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    imagePath.setText(image.toString());
                    InputStream imageStream = getContentResolver().openInputStream(image);
                    bitmapImage = BitmapFactory.decodeStream(imageStream);
                    chosenImage.setImageBitmap(bitmapImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, getString(R.string.failed_to_load_image), Toast.LENGTH_SHORT).show();
                }
            }

        } else {
            Toast.makeText(this, getString(R.string.didnt_pic_picture), Toast.LENGTH_SHORT).show();
        }
    }

    @OnTouch(R.id.product_name_et)
    public boolean onNameTouched(View view) {
        productHasChanged = true;
        return false;
    }

    @OnTouch(R.id.product_supplier_et)
    public boolean onSupplierTouched(View view) {
        productHasChanged = true;
        return false;

    }

    @OnTouch(R.id.product_quantity_et)
    public boolean onQuantityTouched(View view) {
        productHasChanged = true;
        return false;

    }

    @OnTouch(R.id.product_price_et)
    public boolean onPriceTouched(View view) {
        productHasChanged = true;
        return false;
    }

    @OnTouch(R.id.product_id_et)
    public boolean onIdTouched(View view) {
        productHasChanged = true;
        return false;
    }

    public enum ActivityMode {
        MODE_EDIT,
        MODE_ADD
    }

    private ActivityMode mode;
    private Uri mProductUri;

    @Override
    public void onBackPressed() {
        if (!productHasChanged) {
            super.onBackPressed();
            return;
        }
        showAlertDialog();
    }


    private void showAlertDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(R.string.data_changes);
        alertBuilder.setMessage(R.string.are_u_sure_you_want_discard);
        alertBuilder.setPositiveButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertBuilder.setNegativeButton(getString(R.string.discard), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alertBuilder.setCancelable(false);
        alertBuilder.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        ButterKnife.bind(this);
        ActionBar actionBar = getSupportActionBar();
        productHasChanged = false;
        String uri = getIntent().getStringExtra("uri");
        if (uri != null) {
            mode = ActivityMode.MODE_EDIT;
            mProductUri = Uri.parse(uri);
            loadProduct(mProductUri);
        } else {
            mode = ActivityMode.MODE_ADD;
        }
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (mode == ActivityMode.MODE_ADD) {
                actionBar.setTitle(R.string.add_product);
            } else {
                actionBar.setTitle(R.string.edit_product);
            }
        }
    }

    private void loadProduct(Uri mProductUri) {
        String[] projection = new String[]{
                ProductContract.Product.COLUMN_ID,
                ProductContract.Product.COLUMN_TITLE,
                ProductContract.Product.COLUMN_PRICE,
                ProductContract.Product.COLUMN_SUPPLIER,
                ProductContract.Product.COLUMN_QUANTITY,
                ProductContract.Product.COLUMN_PICTURE
        };
        Cursor cursor = getContentResolver().query(mProductUri, projection, null, null, null);
        assert cursor != null;
        cursor.moveToFirst();
        int id = cursor.getInt(cursor.getColumnIndex(ProductContract.Product.COLUMN_ID));
        String title = cursor.getString(cursor.getColumnIndex(ProductContract.Product.COLUMN_TITLE));
        float price = cursor.getFloat(cursor.getColumnIndex(ProductContract.Product.COLUMN_PRICE));
        String supplier = cursor.getString(cursor.getColumnIndex(ProductContract.Product.COLUMN_SUPPLIER));
        int quan = cursor.getInt(cursor.getColumnIndex(ProductContract.Product.COLUMN_QUANTITY));
        byte[] imgArray = cursor.getBlob(cursor.getColumnIndex(ProductContract.Product.COLUMN_PICTURE));
        bitmapImage = ImageUtils.getBitmap(imgArray);
        if (bitmapImage != null) {
            chosenImage.setImageBitmap(bitmapImage);
        }
        productId.setText(String.valueOf(id));
        productName.setText(title);
        productPrice.setText(String.valueOf(price));
        productSupplier.setText(supplier);
        productQuantity.setText(String.valueOf(quan));
        cursor.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                if (mode == ActivityMode.MODE_ADD)
                    saveProduct();
                else {
                    updateProduct();
                }
                return true;
            case android.R.id.home:
                showAlertDialog();
                return true;
            default:
                return false;
        }
    }

    private ContentValues extractData(ActivityMode m) {
        ContentValues values = new ContentValues();
        String name = productName.getText().toString().trim();
        String supplier = productSupplier.getText().toString().trim();
        String quantity = productQuantity.getText().toString().trim();
        String price = productPrice.getText().toString().trim();
        String id = productId.getText().toString().trim();

        if (name.equals("") || supplier.equals("")
                || quantity.equals("")
                || price.equals("")
                || id.equals("")
                || bitmapImage == null) {
            Toast.makeText(this, "invalid input for product\nplease, check data again!", Toast.LENGTH_SHORT).show();
            return null;
        }
        values.put(ProductContract.Product.COLUMN_PICTURE, ImageUtils.getBitmapArray(bitmapImage));
        values.put(ProductContract.Product.COLUMN_ID, id);
        values.put(ProductContract.Product.COLUMN_TITLE, name);
        values.put(ProductContract.Product.COLUMN_QUANTITY, quantity);
        values.put(ProductContract.Product.COLUMN_PRICE, price);
        values.put(ProductContract.Product.COLUMN_SUPPLIER, supplier);
        return values;
    }

    private void updateProduct() {
        ContentValues values = extractData(mode);
        if (values == null) {
            Toast.makeText(this, R.string.invalied_inputs, Toast.LENGTH_SHORT).show();
            return;
        }
        int rows = getContentResolver().update(
                Uri.parse(ProductContract.Product.CONTENT_URI + "/" + values.get(ProductContract.Product.COLUMN_ID).toString()),
                values,
                null,
                null);
        if (rows > 0) {
            productHasChanged = false;
            Toast.makeText(this, R.string.product_saved, Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProduct() {
        ContentValues values = extractData(mode);
        if (values == null) {
            Toast.makeText(this, R.string.invalied_inputs, Toast.LENGTH_SHORT).show();
            return;
        }
        getContentResolver().insert(ProductContract.Product.CONTENT_URI, values);
        productHasChanged = false;
    }
}