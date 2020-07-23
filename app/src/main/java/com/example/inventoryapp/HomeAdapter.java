package com.example.inventoryapp;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventoryapp.DataBase.ProductContract;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.Holder> {
    private Context mContext;
    private Cursor cursor;

    private OnSellClickedInterface onSellClickedListener;

    public interface OnSellClickedInterface {
        int onSellClicked(String id, int q, float p);

        void onLongClickedListener(String id);
    }

    public HomeAdapter(Context mContext, Cursor cursor) {
        this.mContext = mContext;
        onSellClickedListener = (OnSellClickedInterface) mContext;
        this.cursor = cursor;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_view, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        if (cursor == null) return 0;
        return cursor.getCount();
    }

    public void changeData(Cursor data) {
        cursor = data;
        notifyDataSetChanged();
    }

    public class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.title_item)
        TextView title;
        @BindView(R.id.price_item)
        TextView price;
        @BindView(R.id.supplier_item)
        TextView supplier;
        @BindView(R.id.img_item)
        ImageView img;
        @BindView(R.id.quantity_item)
        TextView quantity;
        @BindView(R.id.sell_btn)
        Button sellBtn;

        @OnClick(R.id.sell_btn)
        public void onSellClick(View v) {
            cursor.moveToPosition(getAdapterPosition());
            int q = cursor.getInt(cursor.getColumnIndex(ProductContract.Product.COLUMN_QUANTITY));
            float p = cursor.getFloat(cursor.getColumnIndex(ProductContract.Product.COLUMN_PRICE));
            int rows = onSellClickedListener.onSellClicked(cursor.getString(cursor.getColumnIndex(ProductContract.Product.COLUMN_ID)),
                   q,p);
            if (rows > 0) {
                quantity.setText(mContext.getString(R.string.quantity_details,q - 1));
                Toast.makeText(mContext, R.string.procut_sold, Toast.LENGTH_SHORT).show();
            }
        }

        public Holder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(mContext, v);
                    popupMenu.inflate(R.menu.pop_up_menu);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        popupMenu.setGravity(Gravity.CENTER);
                    }
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.delete_item:
                                    cursor.moveToPosition(getAdapterPosition());
                                    onSellClickedListener
                                            .onLongClickedListener(cursor.getString(cursor.getColumnIndex(ProductContract.Product.COLUMN_ID)));
                                    return true;
                                case R.id.edit_item:
                                    Intent intent = new Intent(mContext, EditorActivity.class);
                                    String id = cursor.getString(cursor.getColumnIndex(ProductContract.Product.COLUMN_ID));
                                    intent.putExtra("uri",
                                           ContentUris.withAppendedId(ProductContract.Product.CONTENT_URI, Long.parseLong(id)).toString());
                                    mContext.startActivity(intent);
                            }
                            return false;
                        }
                    });
                    popupMenu.show();

                    return true;
                }
            });
        }

        @Override
        public void onClick(View v) {
            cursor.moveToPosition(getAdapterPosition());
            Intent intent = new Intent(mContext, DetailsActivity.class);
            String id = cursor.getString(cursor.getColumnIndex(ProductContract.Product.COLUMN_ID));
            intent.putExtra("uri", ProductContract.Product.CONTENT_URI + "/" + id);
            mContext.startActivity(intent);
        }

        public void bind(int position) {
            cursor.moveToPosition(position);
            String t = cursor.getString(cursor.getColumnIndex(ProductContract.Product.COLUMN_TITLE));
            String p = cursor.getString(cursor.getColumnIndex(ProductContract.Product.COLUMN_PRICE));
            String s = cursor.getString(cursor.getColumnIndex(ProductContract.Product.COLUMN_SUPPLIER));
            String q = cursor.getString(cursor.getColumnIndex(ProductContract.Product.COLUMN_QUANTITY));
            //get image
            byte[] image = cursor.getBlob(cursor.getColumnIndex(ProductContract.Product.COLUMN_PICTURE));
            if (image != null && image.length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                img.setImageBitmap(bitmap);
            }
            title.setText(t);
            price.setText(p.concat(" $"));
            supplier.setText(mContext.getString(R.string.supplier_details,s));
            quantity.setText(mContext.getString(R.string.quantity_details,Long.parseLong(q)));
        }
    }
}
