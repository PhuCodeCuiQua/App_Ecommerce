package com.example.login_register;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class CartAdapter extends ArrayAdapter<CartItem> {
    public interface CartActionListener {
        void onCartChanged();
    }

    private final CartActionListener listener;

    public CartAdapter(Context context, List<CartItem> items, CartActionListener listener) {
        super(context, 0, items);
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_cart, parent, false);
        }

        CartItem item = getItem(position);
        Product product = item.getProduct();
        ImageView image = convertView.findViewById(R.id.imageViewCart);
        TextView name = convertView.findViewById(R.id.tvCartName);
        TextView price = convertView.findViewById(R.id.tvCartPrice);
        TextView variant = convertView.findViewById(R.id.tvCartVariant);
        TextView quantity = convertView.findViewById(R.id.tvQuantity);
        ImageButton decrease = convertView.findViewById(R.id.btnDecrease);
        ImageButton increase = convertView.findViewById(R.id.btnIncrease);
        ImageButton delete = convertView.findViewById(R.id.btnDelete);

        ProductImageUtil.apply(image, item.getColorImageResId());
        name.setText(product.getName());
        price.setText(PriceFormatter.format(product.getSalePrice()));
        variant.setText("Size: EU " + item.getSize() + " · Màu: " + item.getColorName());
        quantity.setText(String.valueOf(item.getQuantity()));

        decrease.setOnClickListener(v -> {
            CartManager.decrease(item.getKey());
            listener.onCartChanged();
        });
        increase.setOnClickListener(v -> {
            CartManager.increase(item.getKey());
            listener.onCartChanged();
        });
        delete.setOnClickListener(v -> {
            CartManager.remove(item.getKey());
            listener.onCartChanged();
        });
        return convertView;
    }
}
