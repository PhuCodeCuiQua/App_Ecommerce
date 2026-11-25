package com.example.login_register;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProductCardAdapter extends RecyclerView.Adapter<ProductCardAdapter.ProductViewHolder> {
    public enum DisplayMode {
        HORIZONTAL,
        GRID
    }

    public interface OnProductAddedListener {
        void onProductAdded();
    }

    public interface OnFavoriteChangedListener {
        void onFavoriteChanged(Product product);
    }

    private final Context context;
    private final List<Product> products = new ArrayList<>();
    private final DisplayMode displayMode;
    private final boolean showFavoriteButton;
    private final OnProductAddedListener onProductAddedListener;
    private OnFavoriteChangedListener onFavoriteChangedListener;

    public ProductCardAdapter(Context context, List<Product> products, DisplayMode displayMode) {
        this(context, products, displayMode, false, null);
    }

    public ProductCardAdapter(Context context, List<Product> products, DisplayMode displayMode,
                              boolean showFavoriteButton,
                              OnProductAddedListener onProductAddedListener) {
        this.context = context;
        this.products.addAll(products);
        this.displayMode = displayMode;
        this.showFavoriteButton = showFavoriteButton;
        this.onProductAddedListener = onProductAddedListener;
    }

    public void submitList(List<Product> products) {
        this.products.clear();
        this.products.addAll(products);
        notifyDataSetChanged();
    }

    public void setFavoriteChangedListener(OnFavoriteChangedListener listener) {
        onFavoriteChangedListener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_card, parent, false);
        return new ProductViewHolder(view, (RecyclerView) parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    final class ProductViewHolder extends RecyclerView.ViewHolder {
        private final View card;
        private final View cardContent;
        private final FrameLayout imageContainer;
        private final RecyclerView recyclerView;
        private final ImageView image;
        private final TextView name;
        private final TextView type;
        private final TextView originalPrice;
        private final TextView salePrice;
        private final TextView discount;
        private final ImageButton favorite;
        private final View add;

        ProductViewHolder(@NonNull View itemView, RecyclerView recyclerView) {
            super(itemView);
            this.recyclerView = recyclerView;
            card = itemView.findViewById(R.id.productCard);
            cardContent = itemView.findViewById(R.id.productCardContent);
            imageContainer = itemView.findViewById(R.id.productImageContainer);
            image = itemView.findViewById(R.id.imgProduct);
            name = itemView.findViewById(R.id.tvProductName);
            type = itemView.findViewById(R.id.tvProductType);
            originalPrice = itemView.findViewById(R.id.tvOriginalPrice);
            salePrice = itemView.findViewById(R.id.tvSalePrice);
            discount = itemView.findViewById(R.id.tvDiscount);
            favorite = itemView.findViewById(R.id.btnFavorite);
            add = itemView.findViewById(R.id.btnAdd);
            imageContainer.addOnLayoutChangeListener((view, left, top, right, bottom,
                                                      oldLeft, oldTop, oldRight, oldBottom) -> {
                if (displayMode != DisplayMode.GRID || right <= left) {
                    return;
                }
                android.widget.LinearLayout.LayoutParams params =
                        (android.widget.LinearLayout.LayoutParams) imageContainer.getLayoutParams();
                if (params.height != right - left || params.weight != 0f) {
                    params.height = right - left;
                    params.weight = 0f;
                    imageContainer.setLayoutParams(params);
                }
            });
        }

        void bind(Product product) {
            updateLayout();
            card.post(this::updateLayout);

            List<ProductColor> colors = product.getColors();
            int imageResId = colors.isEmpty()
                    ? product.getImageResId() : colors.get(0).getImageResId();
            ProductImageUtil.apply(image, imageResId);
            name.setText(product.getName());
            type.setText(product.getType());
            salePrice.setText(PriceFormatter.format(product.getSalePrice()));
            if (product.hasDiscount()) {
                originalPrice.setVisibility(View.VISIBLE);
                originalPrice.setText(PriceFormatter.format(product.getOriginalPrice()));
                originalPrice.setPaintFlags(originalPrice.getPaintFlags()
                        | Paint.STRIKE_THRU_TEXT_FLAG);
                discount.setVisibility(View.VISIBLE);
                discount.setText("-" + product.getDiscountPercent() + "%");
                salePrice.setTextColor(ContextCompat.getColor(context, R.color.sale_red));
            } else {
                originalPrice.setVisibility(View.GONE);
                salePrice.setTextColor(ContextCompat.getColor(context, R.color.tile_text));
                discount.setVisibility(View.GONE);
            }

            favorite.setVisibility(showFavoriteButton ? View.VISIBLE : View.GONE);
            updateFavoriteIcon(favorite, product);
            add.setVisibility(showFavoriteButton ? View.GONE : View.VISIBLE);
            favorite.setOnClickListener(v -> {
                if (showFavoriteButton) {
                    FavoriteManager.remove(product.getId());
                    if (onFavoriteChangedListener != null) {
                        onFavoriteChangedListener.onFavoriteChanged(product);
                    }
                } else {
                    FavoriteManager.toggle(product);
                    updateFavoriteIcon(favorite, product);
                }
            });

            card.setOnClickListener(v -> {
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("product", product);
                context.startActivity(intent);
            });
            add.setOnClickListener(v -> {
                ProductColor color = colors.isEmpty() ? null : colors.get(0);
                List<Integer> sizes = product.getSizes();
                CartManager.add(product, color, sizes.isEmpty() ? 0 : sizes.get(0));
                if (onProductAddedListener != null) {
                    onProductAddedListener.onProductAdded();
                }
            });
        }

        private void updateLayout() {
            ViewGroup.LayoutParams cardParams = card.getLayoutParams();
            ViewGroup.LayoutParams contentParams = cardContent.getLayoutParams();
            ViewGroup.LayoutParams imageParams = imageContainer.getLayoutParams();
            if (displayMode == DisplayMode.HORIZONTAL) {
                int parentWidth = recyclerView.getWidth();
                cardParams.width = parentWidth > 0
                        ? Math.round(parentWidth * 0.56f)
                        : context.getResources().getDimensionPixelSize(R.dimen.product_card_width);
                cardParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                contentParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                imageParams.height = 0;
                ((android.widget.LinearLayout.LayoutParams) imageParams).weight = 1f;
            } else {
                cardParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                cardParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                contentParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                imageParams.height = 0;
                ((android.widget.LinearLayout.LayoutParams) imageParams).weight = 0f;
            }
            card.setLayoutParams(cardParams);
            cardContent.setLayoutParams(contentParams);
            imageContainer.setLayoutParams(imageParams);
        }

        private void updateFavoriteIcon(ImageButton button, Product product) {
            boolean isFavorite = FavoriteManager.isFavorite(product.getId());
            button.setImageResource(isFavorite
                    ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
            int tint = ContextCompat.getColor(context,
                    isFavorite ? R.color.sale_red : R.color.black);
            button.setImageTintList(ColorStateList.valueOf(tint));
        }
    }
}
