package com.example.login_register;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.TextView;
import android.widget.ImageView;

import java.util.List;

public class ProductAdapter extends ArrayAdapter<Product> {
    public interface FavoriteChangeListener {
        void onFavoriteChanged(Product product);
    }

    private FavoriteChangeListener favoriteChangeListener;

    public ProductAdapter(Context context, List<Product> products) {
        super(context, 0, products);
    }

    public void setFavoriteChangeListener(FavoriteChangeListener listener) {
        favoriteChangeListener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_product, parent, false);
        }

        Product product = getItem(position);

        TextView tvName = convertView.findViewById(R.id.tvName);
        TextView tvDescription = convertView.findViewById(R.id.tvDescription);
        TextView tvPrice = convertView.findViewById(R.id.tvPrice);

        tvName.setText(product.getName());
        tvDescription.setText(product.getType());
        tvPrice.setText(PriceFormatter.format(product.getSalePrice()));

        ImageView imageView = convertView.findViewById(R.id.imageView);
        ProductImageUtil.apply(imageView, product.getImageResId());

        ImageButton btnFavorite = convertView.findViewById(R.id.btnFavorite);
        btnFavorite.setImageResource(FavoriteManager.isFavorite(product.getId())
                ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
        btnFavorite.setOnClickListener(v -> {
            FavoriteManager.toggle(product);
            if (favoriteChangeListener != null) {
                favoriteChangeListener.onFavoriteChanged(product);
            } else {
                btnFavorite.setImageResource(FavoriteManager.isFavorite(product.getId())
                        ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
            }
        });

        return convertView;
    }
}

