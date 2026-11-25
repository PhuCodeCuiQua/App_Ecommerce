package com.example.login_register;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HomeSectionAdapter extends RecyclerView.Adapter<HomeSectionAdapter.SectionViewHolder> {
    public static final class Section {
        private final String title;
        private final List<Product> products;

        public Section(String title, List<Product> products) {
            this.title = title;
            this.products = new ArrayList<>(products);
        }
    }

    private final Context context;
    private final ProductCardAdapter.OnProductAddedListener onProductAddedListener;
    private final List<Section> sections = new ArrayList<>();
    private int sectionHeight;

    public HomeSectionAdapter(Context context,
                              ProductCardAdapter.OnProductAddedListener listener) {
        this.context = context;
        onProductAddedListener = listener;
    }

    public void submitList(List<Section> sections) {
        this.sections.clear();
        this.sections.addAll(sections);
        notifyDataSetChanged();
    }

    public void setSectionHeight(int sectionHeight) {
        if (sectionHeight <= 0 || this.sectionHeight == sectionHeight) {
            return;
        }
        this.sectionHeight = sectionHeight;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_section, parent, false);
        return new SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder holder, int position) {
        Section section = sections.get(position);
        ViewGroup.LayoutParams sectionParams = holder.itemView.getLayoutParams();
        sectionParams.height = sectionHeight > 0
                ? sectionHeight : ViewGroup.LayoutParams.WRAP_CONTENT;
        holder.itemView.setLayoutParams(sectionParams);
        holder.title.setText(section.title);
        ViewGroup.LayoutParams productsParams = holder.products.getLayoutParams();
        productsParams.height = sectionHeight > 0
                ? Math.max(0, sectionHeight - holder.itemView.getPaddingTop()
                - context.getResources().getDimensionPixelSize(R.dimen.home_section_title_height))
                : ViewGroup.LayoutParams.WRAP_CONTENT;
        holder.products.setLayoutParams(productsParams);
        holder.products.setLayoutManager(new LinearLayoutManager(
                context, RecyclerView.HORIZONTAL, false));
        holder.products.setAdapter(new ProductCardAdapter(context, section.products,
                ProductCardAdapter.DisplayMode.HORIZONTAL, false, onProductAddedListener));
    }

    @Override
    public int getItemCount() {
        return sections.size();
    }

    static final class SectionViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final RecyclerView products;

        SectionViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvSectionTitle);
            products = itemView.findViewById(R.id.rvSectionProducts);
            products.setNestedScrollingEnabled(false);
        }
    }
}
