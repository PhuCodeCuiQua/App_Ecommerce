package com.example.login_register;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.graphics.Typeface;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.content.res.ColorStateList;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ListItemActivity extends AppCompatActivity {
    private static final String[] COLLECTION_ORDER = {
            "Bán chạy nhất", "Bộ sưu tập mùa hè", "Hàng mới về"
    };

    private final ArrayList<Product> productList = new ArrayList<>();
    private final ArrayList<Product> displayedProducts = new ArrayList<>();
    private final List<HomeSectionAdapter.Section> sections = new ArrayList<>();
    private View main;
    private View homeContent;
    private View topbar;
    private View searchPanel;
    private View searchPanelContent;
    private View searchScrim;
    private com.google.android.material.card.MaterialCardView homeCard;
    private RecyclerView rvSections;
    private RecyclerView rvResults;
    private TextView tvNoResults;
    private ProgressBar progressLoading;
    private TextView tvPanelUsername;
    private TextInputEditText edtSearch;
    private LinearLayout categoryContainer;
    private Chip chipFilter;
    private HomeSectionAdapter sectionAdapter;
    private ProductCardAdapter resultsAdapter;
    private OnBackPressedCallback panelBackCallback;
    private AnimatorSet panelAnimator;
    private boolean panelOpen;
    private String expandedGender;
    private String activeQuery;
    private String activeGender;
    private String activeType;
    private int topbarBaseHeight;
    private int homeContentBaseBottom;
    private int searchPanelBaseTop;
    private int searchPanelBaseBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_list_item);
        initViews();
        setupInsets();
        setupRecyclerViews();
        setupFilterChip();
        setupTopbar();
        setupSearchPanel();
        setupBackHandling();
        setupDataLoading();
    }

    private void initViews() {
        main = findViewById(R.id.main);
        homeContent = findViewById(R.id.homeContent);
        topbar = findViewById(R.id.topbarHome);
        homeCard = findViewById(R.id.homeCard);
        searchPanel = findViewById(R.id.searchPanel);
        searchPanelContent = findViewById(R.id.searchPanelContent);
        searchScrim = findViewById(R.id.searchScrim);
        rvSections = findViewById(R.id.rvSections);
        rvResults = findViewById(R.id.rvResults);
        tvNoResults = findViewById(R.id.tvNoResults);
        progressLoading = findViewById(R.id.progressLoading);
        tvPanelUsername = findViewById(R.id.tvPanelUsername);
        edtSearch = findViewById(R.id.edtSearch);
        categoryContainer = findViewById(R.id.categoryContainer);
        chipFilter = findViewById(R.id.chipFilter);
    }

    private void setupInsets() {
        topbarBaseHeight = topbar.getLayoutParams().height;
        homeContentBaseBottom = homeContent.getPaddingBottom();
        searchPanelBaseTop = searchPanelContent.getPaddingTop();
        searchPanelBaseBottom = searchPanelContent.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(main, (view, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, 0, systemBars.right, 0);

            ViewGroup.LayoutParams topbarParams = topbar.getLayoutParams();
            topbarParams.height = topbarBaseHeight + systemBars.top;
            topbar.setLayoutParams(topbarParams);
            homeContent.setPadding(homeContent.getPaddingLeft(),
                    homeContent.getPaddingTop(), homeContent.getPaddingRight(),
                    homeContentBaseBottom + systemBars.bottom);
            searchPanelContent.setPadding(searchPanelContent.getPaddingLeft(),
                    searchPanelBaseTop + systemBars.top, searchPanelContent.getPaddingRight(),
                    searchPanelBaseBottom + systemBars.bottom);
            return windowInsets;
        });
    }

    private void setupRecyclerViews() {
        rvSections.setLayoutManager(new LinearLayoutManager(this));
        sectionAdapter = new HomeSectionAdapter(this, this::showAddedSnackbar);
        rvSections.setAdapter(sectionAdapter);
        setupSectionSizing();

        rvResults.setLayoutManager(new GridLayoutManager(this, 2));
        resultsAdapter = new ProductCardAdapter(this, displayedProducts,
                ProductCardAdapter.DisplayMode.GRID, false, this::showAddedSnackbar);
        rvResults.setAdapter(resultsAdapter);
    }

    private void setupSectionSizing() {
        rvSections.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int height = rvSections.getHeight() - rvSections.getPaddingTop()
                                - rvSections.getPaddingBottom();
                        if (height <= 0) {
                            return;
                        }
                        sectionAdapter.setSectionHeight(height / 2);
                    }
                });
    }

    private void setupFilterChip() {
        chipFilter.setCloseIconResource(R.drawable.ic_close);
        chipFilter.setCloseIconVisible(true);
        chipFilter.setOnCloseIconClickListener(view -> clearFilter());
    }

    private void setupTopbar() {
        findViewById(R.id.btnMenu).setOnClickListener(view ->
                startActivity(new Intent(this, MenuActivity.class)));
        findViewById(R.id.btnSearch).setOnClickListener(view -> openSearchPanel());
        findViewById(R.id.btnCart).setOnClickListener(view ->
                startActivity(new Intent(this, CartActivity.class)));
    }

    private void setupSearchPanel() {
        String username = SharePreferance.username;
        tvPanelUsername.setText(username == null ? "" : username);
        searchScrim.setOnClickListener(view -> closeSearchPanel());
        edtSearch.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        edtSearch.setOnEditorActionListener((textView, actionId, event) -> {
            boolean searchAction = actionId == EditorInfo.IME_ACTION_SEARCH
                    || event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN;
            if (!searchAction) {
                return false;
            }
            String query = edtSearch.getText() == null
                    ? "" : edtSearch.getText().toString();
            applyFilter(query, null, null);
            hideKeyboard();
            closeSearchPanel();
            return true;
        });
    }

    private void setupBackHandling() {
        panelBackCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                closeSearchPanel();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, panelBackCallback);
    }

    private void setupDataLoading() {
        progressLoading.setVisibility(View.VISIBLE);
        CartManager.load(this, this::updateCartBadge);
        FavoriteManager.load(this, null);
        DataSeeder.seedIfNeeded(this::loadProducts);
    }

    private void loadProducts() {
        FirebaseFirestore.getInstance().collection("products").get()
                .addOnSuccessListener(snapshot -> {
                    productList.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot document
                            : snapshot.getDocuments()) {
                        Product product = ProductMapper.fromDocument(document, this);
                        if (ShoeCategories.GENDERS.containsKey(product.getGender())
                                && ShoeCategories.TYPES.contains(product.getType())) {
                            productList.add(product);
                        }
                    }
                    progressLoading.setVisibility(View.GONE);
                    updateDisplayedProducts();
                })
                .addOnFailureListener(exception -> {
                    progressLoading.setVisibility(View.GONE);
                    android.util.Log.e("ListItemActivity", "Không thể tải sản phẩm", exception);
                    Snackbar.make(homeCard, "Không thể tải sản phẩm", Snackbar.LENGTH_LONG).show();
                });
    }

    private void applyFilter(String query, String gender, String type) {
        String trimmedQuery = query == null ? "" : query.trim();
        activeQuery = trimmedQuery.isEmpty() ? null : trimmedQuery;
        activeGender = gender;
        activeType = type;
        updateDisplayedProducts();
        updateFilterChip();
    }

    private void clearFilter() {
        activeQuery = null;
        activeGender = null;
        activeType = null;
        edtSearch.setText("");
        updateDisplayedProducts();
        updateFilterChip();
    }

    private void updateDisplayedProducts() {
        displayedProducts.clear();
        String normalizedQuery = activeQuery == null
                ? null : activeQuery.toLowerCase(Locale.ROOT);
        for (Product product : productList) {
            if (normalizedQuery != null
                    && !product.getName().toLowerCase(Locale.ROOT).contains(normalizedQuery)) {
                continue;
            }
            if (activeGender != null && !activeGender.equals(product.getGender())) {
                continue;
            }
            if (activeType != null && !activeType.equals(product.getType())) {
                continue;
            }
            displayedProducts.add(product);
        }

        if (hasActiveFilter()) {
            showResults();
        } else {
            showSections();
        }
    }

    private boolean hasActiveFilter() {
        return activeQuery != null || activeGender != null || activeType != null;
    }

    private void updateFilterChip() {
        if (!hasActiveFilter()) {
            chipFilter.setVisibility(View.GONE);
            return;
        }
        if (activeQuery != null) {
            chipFilter.setText("Tìm: " + activeQuery);
        } else {
            String genderLabel = ShoeCategories.GENDERS.get(activeGender);
            chipFilter.setText(genderLabel + " · " + activeType);
        }
        chipFilter.setVisibility(View.VISIBLE);
    }

    private void showSections() {
        Map<String, List<Product>> productsByCollection = new LinkedHashMap<>();
        for (String collection : COLLECTION_ORDER) {
            productsByCollection.put(collection, new ArrayList<>());
        }
        for (Product product : productList) {
            List<Product> products = productsByCollection.get(product.getCollection());
            if (products != null) {
                products.add(product);
            }
        }

        sections.clear();
        for (String collection : COLLECTION_ORDER) {
            List<Product> products = productsByCollection.get(collection);
            if (!products.isEmpty()) {
                sections.add(new HomeSectionAdapter.Section(collection, products));
            }
        }
        sectionAdapter.submitList(sections);
        rvSections.setVisibility(View.VISIBLE);
        rvResults.setVisibility(View.GONE);
        tvNoResults.setVisibility(View.GONE);
    }

    private void showResults() {
        resultsAdapter.submitList(displayedProducts);
        boolean hasResults = !displayedProducts.isEmpty();
        rvSections.setVisibility(View.GONE);
        rvResults.setVisibility(hasResults ? View.VISIBLE : View.GONE);
        tvNoResults.setVisibility(hasResults ? View.GONE : View.VISIBLE);
    }

    private void openSearchPanel() {
        if (panelOpen) {
            return;
        }
        panelOpen = true;
        expandedGender = "men";
        edtSearch.setText(activeQuery == null ? "" : activeQuery);
        buildCategoryList();
        searchPanel.setVisibility(View.VISIBLE);
        searchScrim.setVisibility(View.VISIBLE);
        panelBackCallback.setEnabled(true);
        setSystemBarAppearance(true);
        animateHomeCard(true, null);
    }

    private void closeSearchPanel() {
        if (!panelOpen) {
            return;
        }
        panelOpen = false;
        panelBackCallback.setEnabled(false);
        hideKeyboard();
        animateHomeCard(false, () -> {
            searchPanel.setVisibility(View.GONE);
            searchScrim.setVisibility(View.GONE);
            setSystemBarAppearance(false);
        });
    }

    private void animateHomeCard(boolean opening, Runnable onEnd) {
        AnimatorSet previousAnimator = panelAnimator;
        panelAnimator = null;
        if (previousAnimator != null) {
            previousAnimator.cancel();
        }

        float targetTranslation = opening
                ? getResources().getDisplayMetrics().widthPixels * 0.5f : 0;
        float targetScale = opening ? 0.86f : 1f;
        float targetRadius = opening
                ? getResources().getDimension(R.dimen.home_card_radius_max) : 0;
        ValueAnimator radiusAnimator = ValueAnimator.ofFloat(homeCard.getRadius(), targetRadius);
        radiusAnimator.addUpdateListener(valueAnimator ->
                homeCard.setRadius((Float) valueAnimator.getAnimatedValue()));
        AnimatorSet animator = new AnimatorSet();
        animator.playTogether(
                ObjectAnimator.ofFloat(homeCard, View.TRANSLATION_X, targetTranslation),
                ObjectAnimator.ofFloat(homeCard, View.SCALE_X, targetScale),
                ObjectAnimator.ofFloat(homeCard, View.SCALE_Y, targetScale),
                radiusAnimator);
        animator.setDuration(320);
        animator.setInterpolator(new android.view.animation.DecelerateInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator endedAnimator) {
                if (panelAnimator != animator) {
                    return;
                }
                panelAnimator = null;
                if (onEnd != null) {
                    onEnd.run();
                }
            }
        });
        panelAnimator = animator;
        animator.start();
    }

    private void setSystemBarAppearance(boolean panelVisible) {
        boolean isNightMode = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        WindowInsetsControllerCompat controller =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        boolean lightBars = !panelVisible && !isNightMode;
        controller.setAppearanceLightStatusBars(lightBars);
        controller.setAppearanceLightNavigationBars(lightBars);
    }

    private void buildCategoryList() {
        categoryContainer.removeAllViews();
        for (Map.Entry<String, String> gender : ShoeCategories.GENDERS.entrySet()) {
            String genderKey = gender.getKey();
            boolean expanded = genderKey.equals(expandedGender);
            LinearLayout header = new LinearLayout(this);
            header.setGravity(Gravity.CENTER_VERTICAL);
            header.setOrientation(LinearLayout.HORIZONTAL);
            header.setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.menu_item_height));
            header.setPadding(getResources().getDimensionPixelSize(R.dimen.space_8), 0,
                    getResources().getDimensionPixelSize(R.dimen.space_8), 0);
            header.setBackgroundResource(android.R.drawable.list_selector_background);

            TextView label = new TextView(this);
            label.setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            label.setText(gender.getValue().toUpperCase(Locale.forLanguageTag("vi-VN")));
            label.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleSmall);
            label.setTextColor(ContextCompat.getColor(this, R.color.white));
            label.setTypeface(null, Typeface.BOLD);
            header.addView(label);

            ImageView arrow = new ImageView(this);
            arrow.setLayoutParams(new LinearLayout.LayoutParams(
                    getResources().getDimensionPixelSize(R.dimen.icon_size),
                    getResources().getDimensionPixelSize(R.dimen.icon_size)));
            arrow.setImageResource(R.drawable.ic_chevron_right);
            androidx.core.widget.ImageViewCompat.setImageTintList(arrow,
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
            arrow.setRotation(expanded ? 90 : 0);
            header.addView(arrow);
            header.setOnClickListener(view -> {
                expandedGender = expanded ? null : genderKey;
                buildCategoryList();
            });
            categoryContainer.addView(header);

            LinearLayout typeList = new LinearLayout(this);
            typeList.setOrientation(LinearLayout.VERTICAL);
            typeList.setVisibility(expanded ? View.VISIBLE : View.GONE);
            for (String type : ShoeCategories.TYPES) {
                TextView typeItem = new TextView(this);
                typeItem.setGravity(Gravity.CENTER_VERTICAL);
                typeItem.setMinimumHeight(getResources()
                        .getDimensionPixelSize(R.dimen.touch_target));
                typeItem.setPadding(getResources()
                                .getDimensionPixelSize(R.dimen.space_24), 0,
                        getResources().getDimensionPixelSize(R.dimen.space_8), 0);
                typeItem.setText(type);
                typeItem.setTextAppearance(
                        com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
                typeItem.setTextColor(ContextCompat.getColor(this, R.color.white));
                boolean selected = genderKey.equals(activeGender) && type.equals(activeType);
                typeItem.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
                typeItem.setBackgroundResource(android.R.drawable.list_selector_background);
                typeItem.setOnClickListener(view -> {
                    applyFilter(null, genderKey, type);
                    closeSearchPanel();
                });
                typeList.addView(typeItem);
            }
            categoryContainer.addView(typeList);
        }
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);
        }
        edtSearch.clearFocus();
    }

    private void showAddedSnackbar() {
        updateCartBadge();
        Snackbar.make(homeCard, "Đã thêm vào giỏ hàng", Snackbar.LENGTH_SHORT)
                .setAction("Xem giỏ", view ->
                        startActivity(new Intent(this, CartActivity.class)))
                .show();
    }

    private void updateCartBadge() {
        CartBadge.update(main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
    }
}
