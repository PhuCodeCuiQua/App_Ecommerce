package com.example.login_register;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.IntentCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DetailActivity extends AppCompatActivity {
    private Product product;
    private int selectedColorIndex;
    private int selectedSize;
    private int navigationInsetBottom;
    private int galleryBaseHeight;
    private int topActionsBasePaddingTop;
    private int buyNowBasePaddingBottom;
    private View main;
    private View topActions;
    private View buyNowArea;
    private View stickyBar;
    private ViewPager2 vpImages;
    private LinearLayout imageDots;
    private LinearLayout colorSelector;
    private ChipGroup sizeChipGroup;
    private RatingBar ratingBarOverall;
    private MaterialButton btnFavorite;
    private TextView tvDetailName;
    private TextView tvDetailDescription;
    private TextView tvBottomPrice;
    private TextView tvOriginalPrice;
    private TextView tvDiscount;
    private TextView tvRating;
    private Chip tvReviewCount;
    private TextView tvReviewSummary;
    private View featuredReviewCard;
    private TextView tvFeaturedAuthor;
    private TextView tvFeaturedStars;
    private TextView tvFeaturedContent;
    private final List<ProductColor> displayColors = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_detail);
        initViews();
        setupInsets();
        if (!loadProduct()) {
            return;
        }
        recordViewed();
        setupImages();
        setupColorSelector();
        setupSizeChips();
        showRating();
        loadFeaturedReview();
        showProductInfo();
        setupReviewList();
        setupReviewForm();
        setupFloatingButtons();
        setupFavorite();
        setupAddToCart();
        updateCartBadge();
    }

    private void initViews() {
        main = findViewById(R.id.main);
        topActions = findViewById(R.id.topActions);
        buyNowArea = findViewById(R.id.buyNowArea);
        stickyBar = findViewById(R.id.stickyBar);
        vpImages = findViewById(R.id.vpImages);
        imageDots = findViewById(R.id.imageDots);
        colorSelector = findViewById(R.id.layoutColorSelector);
        sizeChipGroup = findViewById(R.id.sizeChipGroup);
        ratingBarOverall = findViewById(R.id.ratingBarOverall);
        btnFavorite = findViewById(R.id.btnFavorite);
        tvDetailName = findViewById(R.id.tvDetailName);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        tvBottomPrice = findViewById(R.id.tvBottomPrice);
        tvOriginalPrice = findViewById(R.id.tvOriginalPrice);
        tvDiscount = findViewById(R.id.tvDiscount);
        tvRating = findViewById(R.id.tvRating);
        tvReviewCount = findViewById(R.id.tvReviewCount);
        tvReviewSummary = findViewById(R.id.tvReviewSummary);
        featuredReviewCard = findViewById(R.id.featuredReviewCard);
        tvFeaturedAuthor = findViewById(R.id.tvFeaturedAuthor);
        tvFeaturedStars = findViewById(R.id.tvFeaturedStars);
        tvFeaturedContent = findViewById(R.id.tvFeaturedContent);
    }

    private void setupInsets() {
        galleryBaseHeight = getResources()
                .getDimensionPixelSize(R.dimen.detail_gallery_height);
        topActionsBasePaddingTop = topActions.getPaddingTop();
        buyNowBasePaddingBottom = buyNowArea.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(main, (view, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, 0, systemBars.right, 0);
            navigationInsetBottom = systemBars.bottom;
            updateGalleryInsets(systemBars.top);
            topActions.setPadding(topActions.getPaddingLeft(),
                    topActionsBasePaddingTop + systemBars.top,
                    topActions.getPaddingRight(), topActions.getPaddingBottom());
            buyNowArea.setPadding(buyNowArea.getPaddingLeft(), buyNowArea.getPaddingTop(),
                    buyNowArea.getPaddingRight(),
                    buyNowBasePaddingBottom + navigationInsetBottom);
            updateSystemBarAppearance();
            stickyBar.post(this::updateScrollBottomPadding);
            return windowInsets;
        });
        stickyBar.addOnLayoutChangeListener((view, left, top, right, bottom,
                                             oldLeft, oldTop, oldRight, oldBottom) ->
                updateScrollBottomPadding());
    }

    private void updateGalleryInsets(int topInset) {
        LinearLayout.LayoutParams galleryParams =
                (LinearLayout.LayoutParams) findViewById(R.id.galleryContainer).getLayoutParams();
        galleryParams.height = galleryBaseHeight + topInset;
        findViewById(R.id.galleryContainer).setLayoutParams(galleryParams);

        FrameLayout.LayoutParams pagerParams =
                (FrameLayout.LayoutParams) vpImages.getLayoutParams();
        pagerParams.height = galleryBaseHeight;
        pagerParams.topMargin = topInset;
        vpImages.setLayoutParams(pagerParams);
    }

    private void updateSystemBarAppearance() {
        boolean nightMode = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        WindowInsetsControllerCompat controller =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(!nightMode);
        controller.setAppearanceLightNavigationBars(false);
    }

    private void updateScrollBottomPadding() {
        if (stickyBar == null) {
            return;
        }
        int bottomPadding = stickyBar.getHeight();
        if (bottomPadding == 0) {
            bottomPadding = getResources().getDimensionPixelSize(R.dimen.detail_sticky_padding)
                    + navigationInsetBottom;
        }
        androidx.core.widget.NestedScrollView scrollView = findViewById(R.id.detailScroll);
        scrollView.setPadding(scrollView.getPaddingLeft(), scrollView.getPaddingTop(),
                scrollView.getPaddingRight(), bottomPadding);
    }

    private boolean loadProduct() {
        product = IntentCompat.getSerializableExtra(getIntent(), "product", Product.class);
        if (product == null) {
            finish();
            return false;
        }
        selectedSize = product.getSizes().isEmpty() ? 0 : product.getSizes().get(0);
        return true;
    }
    private void recordViewed() {
        ProfileDBHelper profileDb = new ProfileDBHelper(this);
        profileDb.addViewed(SharePreferance.getUserKey(), product);
        profileDb.close();
    }

    private void setupImages() {
        displayColors.clear();
        displayColors.addAll(product.getColors());
        if (displayColors.isEmpty()) {
            displayColors.add(new ProductColor("", "", product.getImageResId()));
        }
        List<Integer> imageResources = new ArrayList<>();
        for (ProductColor color : displayColors) {
            imageResources.add(color.getImageResId());
        }
        vpImages.setAdapter(new ProductImagesAdapter(imageResources));
        vpImages.setCurrentItem(0, false);
        setupImageDots(displayColors.size());
        vpImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                selectedColorIndex = position;
                updateColorSelection();
                updateImageDots(position);
            }
        });
    }

    private void setupImageDots(int count) {
        imageDots.removeAllViews();
        for (int index = 0; index < count; index++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    getResources().getDimensionPixelSize(R.dimen.detail_dot_size),
                    getResources().getDimensionPixelSize(R.dimen.detail_dot_size));
            int margin = getResources().getDimensionPixelSize(R.dimen.space_4);
            params.setMargins(margin, 0, margin, 0);
            dot.setLayoutParams(params);
            imageDots.addView(dot);
        }
        updateImageDots(0);
    }

    private void updateImageDots(int selectedPosition) {
        for (int index = 0; index < imageDots.getChildCount(); index++) {
            imageDots.getChildAt(index).setBackgroundResource(index == selectedPosition
                    ? R.drawable.bg_detail_dot_active : R.drawable.bg_detail_dot_inactive);
        }
    }

    private void setupColorSelector() {
        colorSelector.removeAllViews();
        for (int index = 0; index < displayColors.size(); index++) {
            final int colorIndex = index;
            ProductColor color = displayColors.get(index);
            MaterialCardView thumbnail = new MaterialCardView(this);
            thumbnail.setRadius(getResources()
                    .getDimension(R.dimen.detail_color_radius));
            thumbnail.setCardBackgroundColor(
                    ContextCompat.getColor(this, R.color.product_tile));
            thumbnail.setCardElevation(0);
            thumbnail.setUseCompatPadding(false);
            thumbnail.setStrokeWidth(0);

            ImageView image = new ImageView(this);
            ProductImageUtil.apply(image, color.getImageResId());
            image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            image.setBackgroundColor(ContextCompat.getColor(this, R.color.product_tile));
            image.setPadding(getResources().getDimensionPixelSize(R.dimen.space_4),
                    getResources().getDimensionPixelSize(R.dimen.space_4),
                    getResources().getDimensionPixelSize(R.dimen.space_4),
                    getResources().getDimensionPixelSize(R.dimen.space_4));
            thumbnail.addView(image, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    getResources().getDimensionPixelSize(R.dimen.detail_color_width),
                    getResources().getDimensionPixelSize(R.dimen.detail_color_height));
            int margin = getResources().getDimensionPixelSize(R.dimen.space_4);
            params.setMargins(margin, 0, margin, 0);
            thumbnail.setLayoutParams(params);
            thumbnail.setContentDescription(color.getName());
            thumbnail.setOnClickListener(view -> vpImages.setCurrentItem(colorIndex, true));
            colorSelector.addView(thumbnail);
        }
        updateColorSelection();
    }

    private void updateColorSelection() {
        int selectedStroke = getResources().getDimensionPixelSize(R.dimen.detail_thumb_stroke);
        for (int index = 0; index < colorSelector.getChildCount(); index++) {
            MaterialCardView thumbnail = (MaterialCardView) colorSelector.getChildAt(index);
            if (index == selectedColorIndex) {
                thumbnail.setStrokeColor(ContextCompat.getColor(this, R.color.black));
                thumbnail.setStrokeWidth(selectedStroke);
            } else {
                thumbnail.setStrokeWidth(0);
            }
        }
    }

    private void setupSizeChips() {
        sizeChipGroup.setSingleSelection(true);
        sizeChipGroup.setSelectionRequired(true);
        List<Integer> sizes = product.getSizes();
        for (int index = 0; index < sizes.size(); index++) {
            int size = sizes.get(index);
            Chip chip = (Chip) LayoutInflater.from(this)
                    .inflate(R.layout.item_size_chip, sizeChipGroup, false);
            chip.setId(View.generateViewId());
            chip.setText("EU " + size);
            chip.setOnCheckedChangeListener((button, checked) -> {
                if (checked) {
                    selectedSize = size;
                }
            });
            sizeChipGroup.addView(chip);
            if (index == 0) {
                selectedSize = size;
                chip.setChecked(true);
            }
        }
    }

    private void showRating() {
        showRating(product.getRatingAverage(), product.getRatingCount());
    }

    private void showRating(double average, int count) {
        ratingBarOverall.setRating((float) Math.max(0, Math.min(5, average)));
        tvRating.setText(String.format(Locale.getDefault(), "%.1f", average));
        tvReviewCount.setText(String.valueOf(count));
        tvReviewSummary.setText(String.format(Locale.getDefault(),
                "%.1f/5 - %d đánh giá", average, count));
    }

    private void loadFeaturedReview() {
        featuredReviewCard.setVisibility(View.GONE);
        FirebaseFirestore.getInstance().collection("products")
                .document(product.getId())
                .collection("reviews")
                .orderBy("stars", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        featuredReviewCard.setVisibility(View.GONE);
                        return;
                    }
                    DocumentSnapshot review = snapshot.getDocuments().get(0);
                    String author = review.getString("author");
                    Long stars = review.getLong("stars");
                    String content = review.getString("content");
                    tvFeaturedAuthor.setText(author == null ? "" : author);
                    tvFeaturedStars.setText(formatStars(stars == null ? 0
                            : Math.max(0, Math.min(5, stars.intValue()))));
                    tvFeaturedContent.setText(content == null ? "" : content);
                    featuredReviewCard.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(exception ->
                        Log.e("DetailActivity", "Không thể tải đánh giá nổi bật", exception));
    }

    private String formatStars(int count) {
        StringBuilder stars = new StringBuilder();
        for (int index = 0; index < 5; index++) {
            stars.append(index < count ? "★" : "☆");
        }
        return stars.toString();
    }

    private void showProductInfo() {
        tvDetailName.setText(product.getName());
        tvDetailDescription.setText(product.getDescription());
        ((TextView) findViewById(R.id.tvOrigin)).setText(product.getMaterial());
        ((TextView) findViewById(R.id.tvExpiry)).setText(product.getDesigner());
        ((TextView) findViewById(R.id.tvStorage)).setText(product.getFeatures());
        ((TextView) findViewById(R.id.tvShipping)).setText(product.getCollection());
        ((TextView) findViewById(R.id.tvWeight)).setText(product.getType());

        tvBottomPrice.setText(PriceFormatter.format(product.getSalePrice()));
        if (product.hasDiscount()) {
            tvOriginalPrice.setVisibility(View.VISIBLE);
            tvOriginalPrice.setText(PriceFormatter.format(product.getOriginalPrice()));
            tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags()
                    | Paint.STRIKE_THRU_TEXT_FLAG);
            tvDiscount.setVisibility(View.VISIBLE);
            tvDiscount.setText("-" + product.getDiscountPercent() + "%");
        } else {
            tvOriginalPrice.setVisibility(View.GONE);
            tvDiscount.setVisibility(View.GONE);
        }
    }

    private void setupReviewList() {
        showReviews(product);
    }

    private void showReviews(Product product) {
        LinearLayout layoutReviews = findViewById(R.id.layoutReviews);
        FirebaseFirestore.getInstance()
                .collection("products")
                .document(product.getId())
                .collection("reviews")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    layoutReviews.removeAllViews();
                    if (querySnapshot.isEmpty()) {
                        TextView emptyView = new TextView(this);
                        emptyView.setText("Chưa có đánh giá");
                        emptyView.setTextAppearance(
                                com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
                        emptyView.setTextColor(ContextCompat.getColor(
                                this, R.color.md_onSurfaceVariant));
                        layoutReviews.addView(emptyView);
                        return;
                    }

                    SimpleDateFormat dateFormat =
                            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        View reviewView = LayoutInflater.from(this)
                                .inflate(R.layout.item_review, layoutReviews, false);
                        String author = document.getString("author");
                        Long reviewStars = document.getLong("stars");
                        String content = document.getString("content");
                        Timestamp createdAt = document.getTimestamp("createdAt");
                        int starsValue = reviewStars == null ? 0
                                : Math.max(0, Math.min(5, reviewStars.intValue()));

                        ((TextView) reviewView.findViewById(R.id.tvReviewAuthor))
                                .setText(author == null ? "" : author);
                        ((TextView) reviewView.findViewById(R.id.tvReviewAvatar))
                                .setText(author == null || author.isEmpty() ? ""
                                        : author.substring(0, 1).toUpperCase(Locale.getDefault()));
                        ((TextView) reviewView.findViewById(R.id.tvReviewStars))
                                .setText(formatStars(starsValue));
                        ((TextView) reviewView.findViewById(R.id.tvReviewContent))
                                .setText(content == null ? "" : content);
                        ((TextView) reviewView.findViewById(R.id.tvReviewDate))
                                .setText(createdAt == null ? ""
                                        : dateFormat.format(createdAt.toDate()));
                        layoutReviews.addView(reviewView);
                    }
                })
                .addOnFailureListener(exception ->
                        Log.e("DetailActivity", "Không thể tải đánh giá", exception));
    }

    private void setupReviewForm() {
        findViewById(R.id.btnSubmitReview).setOnClickListener(view -> submitReview());
    }

    private void submitReview() {
        RatingBar ratingBar = findViewById(R.id.ratingBarInput);
        TextInputLayout inputLayout = findViewById(R.id.reviewInputLayout);
        TextInputEditText reviewContent = findViewById(R.id.edtReviewContent);
        int stars = Math.round(ratingBar.getRating());
        if (stars == 0) {
            inputLayout.setError("Vui lòng chọn số sao");
            return;
        }
        inputLayout.setError(null);

        String content = reviewContent.getText() == null
                ? "" : reviewContent.getText().toString();
        DocumentReference productReference = FirebaseFirestore.getInstance()
                .collection("products")
                .document(product.getId());
        DocumentReference reviewReference = productReference
                .collection("reviews")
                .document();
        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("author", SharePreferance.username);
        reviewData.put("stars", stars);
        reviewData.put("content", content);
        reviewData.put("createdAt", Timestamp.now());

        FirebaseFirestore.getInstance().runTransaction(transaction -> {
            DocumentSnapshot productSnapshot = transaction.get(productReference);
            Long oldCountValue = productSnapshot.getLong("ratingCount");
            Double oldAverageValue = productSnapshot.getDouble("ratingAverage");
            long oldCount = oldCountValue == null ? 0 : oldCountValue;
            double oldAverage = oldAverageValue == null ? 0 : oldAverageValue;
            long newCount = oldCount + 1;
            double newAverage = ((oldAverage * oldCount) + stars) / newCount;

            transaction.set(reviewReference, reviewData);
            transaction.update(productReference, "ratingCount", newCount);
            transaction.update(productReference, "ratingAverage", newAverage);
            return new RatingStats(newAverage, newCount);
        }).addOnSuccessListener(stats -> {
            reviewContent.setText("");
            ratingBar.setRating(0);
            showRating(stats.average, (int) stats.count);
            reloadReviews();
        }).addOnFailureListener(exception ->
                Log.e("DetailActivity", "Không thể gửi đánh giá", exception));
    }

    private void reloadReviews() {
        showReviews(product);
        loadFeaturedReview();
    }

    private void setupFloatingButtons() {
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
        findViewById(R.id.btnCart).setOnClickListener(view ->
                startActivity(new Intent(this, CartActivity.class)));
        CartManager.load(this, this::updateCartBadge);
    }

    private void setupFavorite() {
        btnFavorite.setOnClickListener(view -> {
            FavoriteManager.toggle(product);
            updateFavoriteButton();
        });
        FavoriteManager.load(this, this::updateFavoriteButton);
        updateFavoriteButton();
    }

    private void updateFavoriteButton() {
        boolean favorite = FavoriteManager.isFavorite(product.getId());
        btnFavorite.setIconResource(favorite
                ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
        btnFavorite.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(this,
                favorite ? R.color.sale_red : R.color.md_onSurfaceVariant)));
    }

    private void setupAddToCart() {
        findViewById(R.id.btnAddToCart).setOnClickListener(view -> {
            List<ProductColor> colors = product.getColors();
            ProductColor selectedColor = colors.isEmpty() || selectedColorIndex >= colors.size()
                    ? null : colors.get(selectedColorIndex);
            CartManager.add(product, selectedColor, selectedSize);
            updateCartBadge();
            Snackbar.make(main, "Đã thêm vào giỏ hàng", Snackbar.LENGTH_SHORT)
                    .setAction("Xem giỏ", action ->
                            startActivity(new Intent(this, CartActivity.class)))
                    .show();
        });
    }

    private void updateCartBadge() {
        CartBadge.update(main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
        if (product != null && btnFavorite != null) {
            updateFavoriteButton();
        }
    }

    private static class ProductImagesAdapter
            extends RecyclerView.Adapter<ProductImagesAdapter.ImageViewHolder> {
        private final List<Integer> imageResources;

        private ProductImagesAdapter(List<Integer> imageResources) {
            this.imageResources = new ArrayList<>(imageResources);
        }

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_product_image, parent, false);
            return new ImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ImageViewHolder holder, int position) {
            ProductImageUtil.apply(holder.image, imageResources.get(position));
        }

        @Override
        public int getItemCount() {
            return imageResources.size();
        }

        private static class ImageViewHolder extends RecyclerView.ViewHolder {
            private final ImageView image;

            private ImageViewHolder(View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.imgPagerProduct);
            }
        }
    }

    private static class RatingStats {
        private final double average;
        private final long count;

        private RatingStats(double average, long count) {
            this.average = average;
            this.count = count;
        }
    }
}
