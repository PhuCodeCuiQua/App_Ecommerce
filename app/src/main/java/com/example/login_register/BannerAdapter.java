package com.example.login_register;

import android.content.Context;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {
    public interface OnBannerClickListener {
        void onBannerClick(Product product);
    }

    private final Context context;
    private final List<Banner> banners = new ArrayList<>();
    private final Map<String, Product> productsById;
    private final OnBannerClickListener listener;

    public BannerAdapter(Context context, List<Banner> banners,
                         Map<String, Product> productsById,
                         OnBannerClickListener listener) {
        this.context = context;
        this.banners.addAll(banners);
        this.productsById = productsById;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        Banner banner = banners.get(position);
        Product product = productsById.get(banner.getProductId());
        holder.tagline.setText(banner.getTagline());
        holder.title.setText(titleWithAccent(banner.getTitle(), banner.getAccentWord()));
        holder.subtitle.setText(banner.getSubtitle());
        holder.cta.setText(banner.getButtonText() + "  ›");
        List<ProductColor> colors = product == null ? new ArrayList<>() : product.getColors();
        if (product == null || colors.isEmpty()) {
            holder.shoe.setVisibility(View.GONE);
        } else {
            ProductImageUtil.apply(holder.shoe, colors.get(0).getImageResId());
            holder.shoe.setVisibility(View.VISIBLE);
        }
        View.OnClickListener click = view -> {
            if (product != null && listener != null) {
                listener.onBannerClick(product);
            }
        };
        holder.itemView.setOnClickListener(click);
        holder.cta.setOnClickListener(click);
    }

    @Override
    public int getItemCount() {
        return banners.size();
    }

    private CharSequence titleWithAccent(String title, String accent) {
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(accent)) {
            return title;
        }
        int start = title.indexOf(accent);
        if (start < 0) {
            return title;
        }
        SpannableString styledTitle = new SpannableString(title);
        styledTitle.setSpan(new ForegroundColorSpan(
                        ContextCompat.getColor(context, R.color.sale_red)),
                start, start + accent.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        return styledTitle;
    }

    static final class BannerViewHolder extends RecyclerView.ViewHolder {
        private final TextView tagline;
        private final TextView title;
        private final TextView subtitle;
        private final ImageView shoe;
        private final MaterialButton cta;

        BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            tagline = itemView.findViewById(R.id.tvBannerTagline);
            title = itemView.findViewById(R.id.tvBannerTitle);
            subtitle = itemView.findViewById(R.id.tvBannerSubtitle);
            shoe = itemView.findViewById(R.id.imgBannerShoe);
            cta = itemView.findViewById(R.id.btnBannerCta);
        }
    }
}
