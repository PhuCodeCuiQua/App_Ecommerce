package com.example.login_register;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.viewpager2.widget.ViewPager2;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ListItemActivity extends AppCompatActivity {
    private static final String[] COLLECTION_ORDER = {
            "Bán chạy nhất", "Bộ sưu tập mùa hè", "Hàng mới về"
    };

    private final ArrayList<Product> productList = new ArrayList<>();
    private final ArrayList<Product> displayedProducts = new ArrayList<>();
    private View main;
    private View topbar;
    private View searchPanel;
    private View searchPanelContent;
    private View searchScrim;
    private View homeScroll;
    private MaterialCardView homeCard;
    private View bottomNav;
    private View bannerSection;
    private ViewPager2 vpBanner;
    private LinearLayout bannerDots;
    private LinearLayout chipRow;
    private LinearLayout sectionsContainer;
    private RecyclerView rvResults;
    private TextView tvNoResults;
    private ProgressBar progressLoading;
    private TextView tvPanelUsername;
    private TextInputEditText edtSearch;
    private LinearLayout categoryContainer;
    private Chip chipFilter;
    private ProductCardAdapter resultsAdapter;
    private OnBackPressedCallback panelBackCallback;
    private AnimatorSet panelAnimator;
    private final Handler bannerHandler = new Handler(Looper.getMainLooper());
    private final Runnable bannerAdvance = this::advanceBanner;
    private final List<ProductCardAdapter> sectionProductAdapters = new ArrayList<>();
    private final List<Banner> banners = new ArrayList<>();
    private final Map<String, Product> productsById = new HashMap<>();
    private final HomeFilterState filterState = new HomeFilterState();
    private final List<String> chipTitles = Arrays.asList("Tất cả", "Giày chạy bộ",
            "Giày thể thao", "Giày thường ngày", "Dép / Sandal", "Sale");
    private ViewPager2.OnPageChangeCallback bannerPageCallback;
    private BannerAdapter bannerAdapter;
    private boolean panelOpen;
    private String expandedGender;
    private int selectedChipIndex;
    private boolean activityResumed;
    private int topbarBaseHeight;
    private int topbarBasePaddingTop;
    private int bottomNavBaseHeight;
    private int homeScrollBasePaddingBottom;
    private int searchPanelBaseTop;
    private int searchPanelBaseBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_list_item);
        initViews();
        setupInsets();
        setupResultsList();
        setupFilterChip();
        setupTopbar();
        setupSearchPanel();
        setupBanner();
        setupChips();
        setupBackHandling();
        setupBottomNav();
        setupDataLoading();
    }

    private void initViews() {
        main = findViewById(R.id.main);
        topbar = findViewById(R.id.topbarHome);
        homeCard = findViewById(R.id.homeCard);
        searchPanel = findViewById(R.id.searchPanel);
        searchPanelContent = findViewById(R.id.searchPanelContent);
        searchScrim = findViewById(R.id.searchScrim);
        homeScroll = findViewById(R.id.homeScroll);
        bottomNav = findViewById(R.id.bottomNav);
        bannerSection = findViewById(R.id.bannerSection);
        vpBanner = findViewById(R.id.vpBanner);
        bannerDots = findViewById(R.id.bannerDots);
        chipRow = findViewById(R.id.chipRow);
        sectionsContainer = findViewById(R.id.sectionsContainer);
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
        topbarBasePaddingTop = topbar.getPaddingTop();
        bottomNavBaseHeight = bottomNav.getLayoutParams().height;
        homeScrollBasePaddingBottom = homeScroll.getPaddingBottom();
        searchPanelBaseTop = searchPanelContent.getPaddingTop();
        searchPanelBaseBottom = searchPanelContent.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(main, (view, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, 0, systemBars.right, 0);

            ViewGroup.LayoutParams topbarParams = topbar.getLayoutParams();
            topbarParams.height = topbarBaseHeight + systemBars.top;
            topbar.setLayoutParams(topbarParams);
            topbar.setPadding(topbar.getPaddingLeft(),
                    topbarBasePaddingTop + systemBars.top, topbar.getPaddingRight(),
                    topbar.getPaddingBottom());
            ViewGroup.LayoutParams scrollParams = homeScroll.getLayoutParams();
            if (scrollParams instanceof android.widget.FrameLayout.LayoutParams) {
                android.widget.FrameLayout.LayoutParams frameParams =
                        (android.widget.FrameLayout.LayoutParams) scrollParams;
                frameParams.topMargin = topbarBaseHeight + systemBars.top;
                homeScroll.setLayoutParams(frameParams);
            }
            homeScroll.setPadding(homeScroll.getPaddingLeft(), homeScroll.getPaddingTop(),
                    homeScroll.getPaddingRight(),
                    homeScrollBasePaddingBottom + systemBars.bottom);
            ViewGroup.LayoutParams navParams = bottomNav.getLayoutParams();
            navParams.height = bottomNavBaseHeight + systemBars.bottom;
            bottomNav.setLayoutParams(navParams);
            bottomNav.setPadding(bottomNav.getPaddingLeft(), bottomNav.getPaddingTop(),
                    bottomNav.getPaddingRight(),
                    systemBars.bottom);
            searchPanelContent.setPadding(searchPanelContent.getPaddingLeft(),
                    searchPanelBaseTop + systemBars.top, searchPanelContent.getPaddingRight(),
                    searchPanelBaseBottom + systemBars.bottom);
            return windowInsets;
        });
    }

    private void setupResultsList() {
        rvResults.setLayoutManager(new GridLayoutManager(this, 2));
        rvResults.setNestedScrollingEnabled(false);
        resultsAdapter = new ProductCardAdapter(this, displayedProducts,
                ProductCardAdapter.DisplayMode.GRID, false, this::showAddedSnackbar);
        resultsAdapter.setFavoriteChangedListener(product -> onFavoriteChanged());
        rvResults.setAdapter(resultsAdapter);
    }

    private void setupBanner() {
        bannerPageCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateBannerDots(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    bannerHandler.removeCallbacks(bannerAdvance);
                } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    scheduleBannerAdvance();
                }
            }
        };
        vpBanner.registerOnPageChangeCallback(bannerPageCallback);
        bannerSection.setVisibility(View.GONE);
    }

    private void setupChips() {
        chipRow.removeAllViews();
        int[] icons = {
                R.drawable.ic_category_grid, R.drawable.ic_category_running,
                R.drawable.ic_category_sports, R.drawable.ic_category_casual,
                R.drawable.ic_category_sandal, R.drawable.ic_category_sale
        };
        for (int index = 0; index < chipTitles.size(); index++) {
            final int chipIndex = index;
            View chip = getLayoutInflater().inflate(
                    R.layout.item_category_chip, chipRow, false);
            MaterialCardView card = chip.findViewById(R.id.categoryChipCard);
            ImageView icon = chip.findViewById(R.id.imgCategoryIcon);
            TextView label = chip.findViewById(R.id.tvCategoryLabel);
            label.setText(chipTitles.get(index));
            icon.setImageResource(icons[index]);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    getResources().getDimensionPixelSize(R.dimen.category_chip_width),
                    getResources().getDimensionPixelSize(R.dimen.category_chip_height));
            if (index > 0) {
                params.setMarginStart(getResources()
                        .getDimensionPixelSize(R.dimen.space_8));
            }
            card.setLayoutParams(params);
            card.setOnClickListener(view -> selectChip(chipIndex));
            chipRow.addView(chip);
        }
        updateChipSelection();
    }

    private void selectChip(int index) {
        selectedChipIndex = index;
        filterState.setTypes(Collections.emptySet());
        filterState.setSaleOnly(false);
        if (index == 1) {
            filterState.setTypes(Collections.singleton("Giày chạy bộ"));
        } else if (index == 2) {
            filterState.setTypes(Collections.singleton("Giày thể thao"));
        } else if (index == 3) {
            filterState.setTypes(Collections.singleton("Giày thường ngày"));
        } else if (index == 4) {
            filterState.setTypes(Arrays.asList(
                    "Dép xỏ ngón", "Dép quai hậu", "Sandal"));
        } else if (index == 5) {
            filterState.setSaleOnly(true);
        }
        applyFilter();
    }

    private void updateChipSelection() {
        for (int index = 0; index < chipRow.getChildCount(); index++) {
            View child = chipRow.getChildAt(index);
            MaterialCardView card = child.findViewById(R.id.categoryChipCard);
            ImageView icon = child.findViewById(R.id.imgCategoryIcon);
            TextView label = child.findViewById(R.id.tvCategoryLabel);
            boolean selected = index == selectedChipIndex;
            card.setCardBackgroundColor(ContextCompat.getColor(this,
                    selected ? R.color.bottom_bar_black : R.color.chip_bg));
            int tint = ContextCompat.getColor(this,
                    selected ? R.color.white : R.color.md_onSurface);
            androidx.core.widget.ImageViewCompat.setImageTintList(
                    icon, ColorStateList.valueOf(tint));
            label.setTextColor(tint);
            label.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    private void setupBottomNav() {
        BottomNav.setup(this, BottomNav.HOME);
    }

    private void setupFilterChip() {
        chipFilter.setCloseIconResource(R.drawable.ic_close);
        chipFilter.setCloseIconVisible(true);
        chipFilter.setOnCloseIconClickListener(view -> clearFilter());
    }

    private void clearFilter() {
        filterState.clear();
        selectedChipIndex = 0;
        edtSearch.setText("");
        applyFilter();
    }

    private void buildBannerDots() {
        bannerDots.removeAllViews();
        for (int index = 0; index < banners.size(); index++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    getResources().getDimensionPixelSize(index == vpBanner.getCurrentItem()
                            ? R.dimen.banner_dot_active_width : R.dimen.banner_dot_size),
                    getResources().getDimensionPixelSize(R.dimen.banner_dot_active_height));
            if (index > 0) {
                params.setMarginStart(getResources()
                        .getDimensionPixelSize(R.dimen.banner_dot_spacing));
            }
            dot.setLayoutParams(params);
            dot.setBackgroundResource(index == vpBanner.getCurrentItem()
                    ? R.drawable.bg_banner_dot_active : R.drawable.bg_banner_dot);
            bannerDots.addView(dot);
        }
    }

    private void updateBannerDots(int selectedPosition) {
        for (int index = 0; index < bannerDots.getChildCount(); index++) {
            View dot = bannerDots.getChildAt(index);
            boolean selected = index == selectedPosition;
            ViewGroup.LayoutParams current = dot.getLayoutParams();
            int width = getResources().getDimensionPixelSize(selected
                    ? R.dimen.banner_dot_active_width : R.dimen.banner_dot_size);
            if (current.width != width) {
                current.width = width;
                dot.setLayoutParams(current);
            }
            dot.setBackgroundResource(selected
                    ? R.drawable.bg_banner_dot_active : R.drawable.bg_banner_dot);
        }
    }

    private void scheduleBannerAdvance() {
        bannerHandler.removeCallbacks(bannerAdvance);
        if (activityResumed && bannerAdapter != null && bannerAdapter.getItemCount() > 1) {
            bannerHandler.postDelayed(bannerAdvance, 4000L);
        }
    }

    private void advanceBanner() {
        if (!activityResumed || bannerAdapter == null || bannerAdapter.getItemCount() <= 1) {
            return;
        }
        int next = (vpBanner.getCurrentItem() + 1) % bannerAdapter.getItemCount();
        vpBanner.setCurrentItem(next, true);
        scheduleBannerAdvance();
    }

    private void openProductDetail(Product product) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("product", product);
        startActivity(intent);
    }

    private void setupTopbar() {
        findViewById(R.id.btnSearch).setOnClickListener(view -> openSearchPanel());
        findViewById(R.id.btnCart).setOnClickListener(view ->
                startActivity(new Intent(this, CartActivity.class)));
    }

    private void setupSearchPanel() {
        String username = SharePreferance.username;
        tvPanelUsername.setText(username == null ? "" : username);
        searchScrim.setOnClickListener(view -> closeSearchPanel());
        edtSearch.setText(filterState.getQuery() == null ? "" : filterState.getQuery());
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
            filterState.setQuery(query);
            applyFilter();
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
        FavoriteManager.load(this, this::refreshProductCards);
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
                            productsById.put(product.getId(), product);
                        }
                    }
                    loadBanners();
                })
                .addOnFailureListener(exception -> {
                    progressLoading.setVisibility(View.GONE);
                    android.util.Log.e("ListItemActivity", "Không thể tải sản phẩm", exception);
                    Snackbar.make(homeCard, "Không thể tải sản phẩm", Snackbar.LENGTH_LONG).show();
                });
    }

    private void loadBanners() {
        BannerRepository.loadBanners(new BannerRepository.LoadCallback() {
            @Override
            public void onSuccess(List<Banner> loadedBanners) {
                banners.clear();
                banners.addAll(loadedBanners);
                bannerAdapter = new BannerAdapter(ListItemActivity.this, banners,
                        productsById, ListItemActivity.this::openProductDetail);
                vpBanner.setAdapter(bannerAdapter);
                bannerSection.setVisibility(banners.isEmpty() ? View.GONE : View.VISIBLE);
                buildBannerDots();
                finishInitialLoading();
            }

            @Override
            public void onFailure(Exception exception) {
                bannerSection.setVisibility(View.GONE);
                bannerDots.removeAllViews();
                finishInitialLoading();
            }
        });
    }

    private void finishInitialLoading() {
        progressLoading.setVisibility(View.GONE);
        applyFilter();
    }

    private void applyFilter() {
        displayedProducts.clear();
        String query = filterState.getQuery();
        String normalizedQuery = query == null ? null : query.toLowerCase(Locale.ROOT);
        Set<String> types = filterState.getTypes();
        for (Product product : productList) {
            if (normalizedQuery != null
                    && !product.getName().toLowerCase(Locale.ROOT).contains(normalizedQuery)) {
                continue;
            }
            if (filterState.getGender() != null
                    && !filterState.getGender().equals(product.getGender())) {
                continue;
            }
            if (!types.isEmpty() && !types.contains(product.getType())) {
                continue;
            }
            if (filterState.isSaleOnly() && product.getDiscountPercent() <= 0) {
                continue;
            }
            displayedProducts.add(product);
        }

        updateFilterChip();
        updateChipSelection();
        if (filterState.hasFilters()) {
            showResults();
        } else {
            showSections();
        }
    }

    private void updateFilterChip() {
        if (!filterState.hasFilters()) {
            chipFilter.setVisibility(View.GONE);
            return;
        }
        List<String> labels = new ArrayList<>();
        if (filterState.getQuery() != null) {
            labels.add("Tìm: " + filterState.getQuery());
        }
        if (filterState.getGender() != null) {
            String genderLabel = ShoeCategories.GENDERS.get(filterState.getGender());
            if (genderLabel != null) {
                labels.add(genderLabel);
            }
        }
        if (!filterState.getTypes().isEmpty()) {
            labels.add(joinValues(filterState.getTypes()));
        }
        if (filterState.isSaleOnly()) {
            labels.add("Đang giảm giá");
        }
        chipFilter.setText(joinValues(labels));
        chipFilter.setVisibility(View.VISIBLE);
    }

    private void showSections() {
        Map<String, List<Product>> productsByCollection = new HashMap<>();
        for (String collection : COLLECTION_ORDER) {
            productsByCollection.put(collection, new ArrayList<>());
        }
        for (Product product : productList) {
            List<Product> products = productsByCollection.get(product.getCollection());
            if (products != null) {
                products.add(product);
            }
        }

        sectionsContainer.removeAllViews();
        sectionProductAdapters.clear();
        for (String collection : COLLECTION_ORDER) {
            List<Product> products = productsByCollection.get(collection);
            if (!products.isEmpty()) {
                addCollectionSection(collection, products);
            }
        }
        bannerSection.setVisibility(banners.isEmpty() ? View.GONE : View.VISIBLE);
        sectionsContainer.setVisibility(View.VISIBLE);
        rvResults.setVisibility(View.GONE);
        tvNoResults.setVisibility(View.GONE);
    }

    private void showResults() {
        bannerSection.setVisibility(View.GONE);
        sectionsContainer.setVisibility(View.GONE);
        resultsAdapter.submitList(displayedProducts);
        boolean hasResults = !displayedProducts.isEmpty();
        rvResults.setVisibility(hasResults ? View.VISIBLE : View.GONE);
        tvNoResults.setVisibility(hasResults ? View.GONE : View.VISIBLE);
    }

    private void addCollectionSection(String collection, List<Product> products) {
        View section = getLayoutInflater().inflate(
                R.layout.item_section, sectionsContainer, false);
        TextView title = section.findViewById(R.id.tvSectionTitle);
        com.google.android.material.button.MaterialButton seeAll =
                section.findViewById(R.id.btnSeeAll);
        RecyclerView productGrid = section.findViewById(R.id.rvSectionProducts);
        title.setText(collection);
        productGrid.setLayoutManager(new GridLayoutManager(this, 2));
        productGrid.setNestedScrollingEnabled(false);

        List<Product> firstProducts = new ArrayList<>(products.subList(
                0, Math.min(4, products.size())));
        ProductCardAdapter adapter = new ProductCardAdapter(this, firstProducts,
                ProductCardAdapter.DisplayMode.GRID, false, this::showAddedSnackbar);
        adapter.setFavoriteChangedListener(product -> onFavoriteChanged());
        productGrid.setAdapter(adapter);
        sectionProductAdapters.add(adapter);

        seeAll.setOnClickListener(view -> {
            Intent intent = new Intent(this, ProductListActivity.class);
            intent.putExtra("mode", "collection");
            intent.putExtra("title", collection);
            intent.putExtra("collection", collection);
            startActivity(intent);
        });
        sectionsContainer.addView(section);
    }

    private void onFavoriteChanged() {
        refreshProductCards();
    }

    private void refreshProductCards() {
        resultsAdapter.notifyDataSetChanged();
        for (ProductCardAdapter adapter : sectionProductAdapters) {
            adapter.notifyDataSetChanged();
        }
    }

    private String joinValues(Iterable<String> values) {
        StringBuilder joined = new StringBuilder();
        for (String value : values) {
            if (joined.length() > 0) {
                joined.append(" · ");
            }
            joined.append(value);
        }
        return joined.toString();
    }

    private void openSearchPanel() {
        if (panelOpen) {
            return;
        }
        panelOpen = true;
        expandedGender = filterState.getGender() == null
                ? "men" : filterState.getGender();
        edtSearch.setText(filterState.getQuery() == null ? "" : filterState.getQuery());
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
                boolean selected = genderKey.equals(filterState.getGender())
                        && filterState.getTypes().contains(type);
                typeItem.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
                typeItem.setBackgroundResource(android.R.drawable.list_selector_background);
                typeItem.setOnClickListener(view -> {
                    filterState.setGender(genderKey);
                    filterState.setTypes(Collections.singleton(type));
                    filterState.setSaleOnly(false);
                    selectedChipIndex = 0;
                    applyFilter();
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
        activityResumed = true;
        updateCartBadge();
        BottomNav.refreshBadges(this);
        if (resultsAdapter != null) {
            resultsAdapter.notifyDataSetChanged();
        }
        for (ProductCardAdapter adapter : sectionProductAdapters) {
            adapter.notifyDataSetChanged();
        }
        scheduleBannerAdvance();
    }

    @Override
    protected void onPause() {
        activityResumed = false;
        bannerHandler.removeCallbacks(bannerAdvance);
        super.onPause();
    }
}
