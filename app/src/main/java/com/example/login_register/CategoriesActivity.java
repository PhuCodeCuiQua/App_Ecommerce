package com.example.login_register;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

import java.util.Locale;
import java.util.Map;

public class CategoriesActivity extends AppCompatActivity {
    private View main;
    private View header;
    private View bottomNav;
    private LinearLayout categoriesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);
        initViews();
        setupInsets();
        setupBottomNav();
        loadCategories();
    }

    private void initViews() {
        main = findViewById(R.id.main);
        header = findViewById(R.id.categoriesHeader);
        bottomNav = findViewById(R.id.bottomNav);
        categoriesContainer = findViewById(R.id.categoriesContainer);
    }

    private void setupInsets() {
        BottomNav.applyInsets(this, main, header, bottomNav);
    }

    private void setupBottomNav() {
        BottomNav.setup(this, BottomNav.CATEGORIES);
        BottomNav.handleBack(this);
    }

    private void loadCategories() {
        addCategoryCard("Đang giảm giá", null, null, "sale", true);
        for (Map.Entry<String, String> gender : ShoeCategories.GENDERS.entrySet()) {
            addCategoryCard(gender.getValue().toUpperCase(new Locale("vi", "VN")),
                    gender.getKey(), null,
                    "gender", false);
        }
    }

    private void addCategoryCard(String title, String gender, String type,
                                 String mode, boolean sale) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = getResources()
                .getDimensionPixelSize(R.dimen.space_12);
        card.setLayoutParams(cardParams);
        card.setRadius(getResources().getDimension(R.dimen.radius_medium));
        card.setCardBackgroundColor(getColor(R.color.product_tile));
        card.setStrokeWidth(0);
        card.setCardElevation(0);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dimen(R.dimen.space_16), dimen(R.dimen.space_8),
                dimen(R.dimen.space_16), dimen(R.dimen.space_8));
        card.addView(content);

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        titleRow.setMinimumHeight(dimen(R.dimen.touch_target));
        TextView heading = createLabel(title, R.style.TextAppearance_App_TitleMedium);
        heading.setTypeface(heading.getTypeface(), android.graphics.Typeface.BOLD);
        titleRow.addView(heading, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        TextView arrow = createLabel("›", R.style.TextAppearance_App_TitleLarge);
        titleRow.addView(arrow);
        content.addView(titleRow);
        titleRow.setOnClickListener(view -> openProductList(
                sale ? "sale" : mode, title, gender, type));
        titleRow.setClickable(true);
        titleRow.setFocusable(true);

        if (!sale) {
            for (String shoeType : ShoeCategories.TYPES) {
                addTypeRow(content, gender, title, shoeType);
            }
        } else {
            card.setOnClickListener(view -> openProductList("sale", title, null, null));
            card.setClickable(true);
            card.setFocusable(true);
        }
        categoriesContainer.addView(card);
    }

    private void addTypeRow(LinearLayout content, String gender, String genderTitle,
                            String type) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(dimen(R.dimen.touch_target));
        TextView label = createLabel(type, R.style.TextAppearance_App_BodyLarge);
        row.addView(label, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(createLabel("›", R.style.TextAppearance_App_TitleMedium));
        row.setOnClickListener(view -> openProductList("gender_type",
                genderTitle + " · " + type, gender, type));
        row.setClickable(true);
        row.setFocusable(true);
        content.addView(row);
    }

    private TextView createLabel(String text, int appearance) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextAppearance(appearance);
        label.setTextColor(getColor(R.color.tile_text));
        return label;
    }

    private void openProductList(String mode, String title, String gender, String type) {
        Intent intent = new Intent(this, ProductListActivity.class);
        intent.putExtra("mode", mode);
        intent.putExtra("title", title);
        if (gender != null) {
            intent.putExtra("gender", gender);
        }
        if (type != null) {
            intent.putExtra("type", type);
        }
        startActivity(intent);
    }

    private int dimen(int resource) {
        return getResources().getDimensionPixelSize(resource);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNav.refreshBadges(this);
    }
}
