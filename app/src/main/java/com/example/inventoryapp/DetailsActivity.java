package com.example.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.inventoryapp.DataBase.ProductContract;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DetailsActivity extends AppCompatActivity {

    private static final int LOADER_ID = 209;
    private static final int LOAD_SALES_ID = 210;
    @BindView(R.id.img_details)
    ImageView img;
    @BindView(R.id.name_details)
    TextView title;
    @BindView(R.id.price_details)
    TextView price;
    @BindView(R.id.quantity_details)
    TextView quantity;
    @BindView(R.id.id_details)
    TextView prodId;
    @BindView(R.id.increase_quantity)
    ImageButton increaseQuantity;
    @BindView(R.id.decrease_quantity)
    ImageButton decreaseQuantity;
    @BindView(R.id.order_btn)
    Button orderBtn;
    private int prodQuantity = 0;
    @BindView(R.id.supplier_details)
    TextView supplier;

    @BindView(R.id.sold_details)
    TextView soldQuantity;
    @BindView(R.id.total_price_details)
    TextView totalPrice;

    @OnClick(R.id.increase_quantity)
    public void OnIncreaseClicked(View view) {
        ++prodQuantity;
        quantity.setText(getString(R.string.quantity_details, prodQuantity));
        updateQuantity();
    }

    @OnClick(R.id.decrease_quantity)
    public void OnDecreaseClicked(View view) {
        if (prodQuantity > 0) {
            --prodQuantity;
            quantity.setText(getString(R.string.quantity_details, prodQuantity));
            updateQuantity();

        }
    }

    @OnClick(R.id.order_btn)
    public void onOrderClicked(View view) {
        //order from supplier
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.quantity);
        LinearLayout layout = new LinearLayout(this);
        TextView tvMessage = new TextView(this);
        final EditText etInput = new EditText(this);
        tvMessage.setText(R.string.enter_quantity);
        tvMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        etInput.setSingleLine();
        etInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(tvMessage);
        layout.addView(etInput);
        layout.setPadding(50, 40, 50, 10);
        builder.setView(layout);
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.cancel();
        });
        builder.setPositiveButton(R.string.order_from_supplier, (dialog, which) -> {
            int requestedQuantity = Integer.parseInt(etInput.getText().toString());
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_EMAIL, "khaled.ali.06.10@gmail.com");
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.order_from, supplier.getText().toString()));
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.product_title, title.getText().toString())
                    .concat("\n")
                    .concat(getString(R.string.prod_id, prodId.getText().toString()))
                    .concat("\n")
                    .concat(getString(R.string.quantity_details, requestedQuantity)));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }

        });
        builder.create().show();
    }

    private void updateQuantity() {
        ContentValues values = new ContentValues();
        values.put(ProductContract.Product.COLUMN_QUANTITY, prodQuantity);
        getContentResolver().update(productUri, values, null, null);
    }

    private Uri productUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(R.string.product_info);
        }
        String uri = getIntent().getStringExtra("uri");
        if (uri != null) {
            productUri = Uri.parse(uri);
            loadData();

        } else {
            finish();
        }
    }

    private void loadData() {
        String[] projection = new String[]{
                ProductContract.Sales.COLUMN_QUANTITY,
                ProductContract.Sales.COLUMN_PRODUCT_ID,
                ProductContract.Sales.COLUMN_PRICE,
        };
        Uri uri = ContentUris.withAppendedId(ProductContract.Sales.CONTENT_URI, ContentUris.parseId(productUri));
        Cursor sales = getContentResolver().query(uri, projection, null, null, null);
        projection = new String[]{
                ProductContract.Product.COLUMN_QUANTITY,
                ProductContract.Product.COLUMN_ID,
                ProductContract.Product.COLUMN_SUPPLIER,
                ProductContract.Product.COLUMN_PRICE,
                ProductContract.Product.COLUMN_TITLE,
                ProductContract.Product.COLUMN_PICTURE
        };
        Cursor info = getContentResolver().query(productUri, projection, null, null, null);
        updateViews(info, sales);

    }

    private void updateViews(Cursor info, Cursor sales) {
        if (info.getCount() < 1) return;
        info.moveToFirst();
        prodQuantity = info.getInt(info.getColumnIndex(ProductContract.Product.COLUMN_QUANTITY));
        String s = info.getString(info.getColumnIndex(ProductContract.Product.COLUMN_SUPPLIER));
        supplier.setText(getString(R.string.supplier_details, s));
        int id = info.getInt(info.getColumnIndex(ProductContract.Product.COLUMN_ID));
        prodId.setText(getString(R.string.id_details, id));
        float p = info.getFloat(info.getColumnIndex(ProductContract.Product.COLUMN_PRICE));
        price.setText(getString(R.string.price_details, String.valueOf(p)));
        title.setText(info.getString(info.getColumnIndex(ProductContract.Product.COLUMN_TITLE)));
        quantity.setText(getString(R.string.quantity_details, prodQuantity));
        byte[] array = info.getBlob(info.getColumnIndex(ProductContract.Product.COLUMN_PICTURE));
        img.setImageBitmap(ImageUtils.getBitmap(array));
        float pri;
        int q;
        if (sales.getCount() < 1) {
            pri = 0;
            q = 0;
        } else {
            sales.moveToFirst();
            pri = sales.getFloat(sales.getColumnIndex(ProductContract.Sales.COLUMN_PRICE));
            q = sales.getInt(sales.getColumnIndex(ProductContract.Sales.COLUMN_QUANTITY));
        }
        totalPrice.setText(getString(R.string.total_price, String.valueOf(pri)));
        soldQuantity.setText(getString(R.string.sold, q));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pop_up_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_item:
                Intent intent = new Intent(DetailsActivity.this, EditorActivity.class);
                intent.putExtra("uri", productUri.toString());
                startActivity(intent);
                return true;
            case R.id.delete_item:
                int row = getContentResolver().delete(productUri, null, null);
                if (row > 0) {
                    Toast.makeText(this, R.string.product_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, R.string.failed_delete_product, Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return false;
        }
    }
}