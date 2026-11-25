package com.example.login_register;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
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
        private final RecyclerView recyclerView;
        private final ImageView image;
        private final TextView name;
        private final TextView type;
        private final TextView originalPrice;
        private final TextView salePrice;
        private final TextView discount;
        private final View discountRow;
        private final TextView tag;
        private final ImageButton favorite;
        private final View add;

        ProductViewHolder(@NonNull View itemView, RecyclerView recyclerView) {
            super(itemView);
            this.recyclerView = recyclerView;
            card = itemView.findViewById(R.id.productCard);
            cardContent = itemView.findViewById(R.id.productCardContent);
            image = itemView.findViewById(R.id.imgProduct);
            name = itemView.findViewById(R.id.tvProductName);
            type = itemView.findViewById(R.id.tvProductType);
            originalPrice = itemView.findViewById(R.id.tvOriginalPrice);
            salePrice = itemView.findViewById(R.id.tvSalePrice);
            discount = itemView.findViewById(R.id.tvDiscount);
            discountRow = itemView.findViewById(R.id.productDiscountRow);
            tag = itemView.findViewById(R.id.tvTag);
            favorite = itemView.findViewById(R.id.btnFavorite);
            add = itemView.findViewById(R.id.btnAdd);
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
                originalPrice.setText(PriceFormatter.format(product.getOriginalPrice()));
                originalPrice.setPaintFlags(originalPrice.getPaintFlags()
                        | Paint.STRIKE_THRU_TEXT_FLAG);
                discount.setText("-" + product.getDiscountPercent() + "%");
                discountRow.setVisibility(View.VISIBLE);
                salePrice.setTextColor(ContextCompat.getColor(context, R.color.sale_red));
            } else {
                salePrice.setTextColor(ContextCompat.getColor(context, R.color.tile_text));
                discountRow.setVisibility(View.GONE);
            }

            bindTag(product.getPrimaryTag());
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
                    if (onFavoriteChangedListener != null) {
                        onFavoriteChangedListener.onFavoriteChanged(product);
                    }
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
            if (displayMode == DisplayMode.HORIZONTAL) {
                int parentWidth = recyclerView.getWidth();
                cardParams.width = parentWidth > 0
                        ? Math.round(parentWidth * 0.56f)
                        : context.getResources().getDimensionPixelSize(R.dimen.product_card_width);
                cardParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                contentParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            } else {
                cardParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                cardParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                contentParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
            card.setLayoutParams(cardParams);
            cardContent.setLayoutParams(contentParams);
        }

        private void updateFavoriteIcon(ImageButton button, Product product) {
            boolean isFavorite = FavoriteManager.isFavorite(product.getId());
            button.setImageResource(isFavorite
                    ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
            int tint = ContextCompat.getColor(context,
                    isFavorite ? R.color.sale_red : R.color.tile_text_secondary);
            button.setImageTintList(ColorStateList.valueOf(tint));
        }

        private void bindTag(String primaryTag) {
            if (primaryTag == null) {
                tag.setVisibility(View.GONE);
                return;
            }
            int icon;
            int background;
            int foreground;
            String label;
            switch (primaryTag) {
                case "flashsale":
                    label = "Flash Sale";
                    icon = R.drawable.ic_tag_flashsale;
                    background = R.drawable.bg_product_tag_flashsale;
                    foreground = R.color.tag_flashsale_foreground;
                    break;
                case "bestseller":
                    label = "Bán chạy";
                    icon = R.drawable.ic_tag_bestseller;
                    background = R.drawable.bg_product_tag_bestseller;
                    foreground = R.color.tag_bestseller_foreground;
                    break;
                case "new":
                    label = "Mới";
                    icon = R.drawable.ic_tag_new;
                    background = R.drawable.bg_product_tag_new;
                    foreground = R.color.tag_new_foreground;
                    break;
                default:
                    tag.setVisibility(View.GONE);
                    return;
            }
            tag.setText(label);
            tag.setBackgroundResource(background);
            Drawable drawable = DrawableCompat.wrap(
                    ContextCompat.getDrawable(context, icon)).mutate();
            DrawableCompat.setTint(drawable, ContextCompat.getColor(context, foreground));
            int iconSize = context.getResources()
                    .getDimensionPixelSize(R.dimen.product_tag_icon_size);
            drawable.setBounds(0, 0, iconSize, iconSize);
            tag.setCompoundDrawables(drawable, null, null, null);
            tag.setTextColor(ContextCompat.getColor(context, foreground));
            tag.setVisibility(View.VISIBLE);
        }
    }
}
