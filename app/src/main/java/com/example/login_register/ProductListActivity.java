package com.example.login_register;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ProductListActivity extends AppCompatActivity {
    private View main;
    private View topbar;
    private TextView title;
    private RecyclerView productsView;
    private ProgressBar progress;
    private TextView empty;
    private String mode;
    private String collection;
    private String gender;
    private String type;
    private ProductCardAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        initViews();
        readArguments();
        setupInsets();
        setupTopbar();
        setupProductList();
        loadProducts();
    }

    private void initViews() {
        main = findViewById(R.id.main);
        topbar = findViewById(R.id.productListTopbar);
        title = findViewById(R.id.tvProductListTitle);
        productsView = findViewById(R.id.rvProductList);
        progress = findViewById(R.id.progressProductList);
        empty = findViewById(R.id.tvProductListEmpty);
    }

    private void readArguments() {
        Intent intent = getIntent();
        mode = intent.getStringExtra("mode");
        collection = intent.getStringExtra("collection");
        gender = intent.getStringExtra("gender");
        type = intent.getStringExtra("type");
    }

    private void setupInsets() {
        WindowCompat.enableEdgeToEdge(getWindow());
        int topPadding = topbar.getPaddingTop();
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), main);
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);
        ViewCompat.setOnApplyWindowInsetsListener(main, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, 0, bars.right, bars.bottom);
            topbar.setPadding(topbar.getPaddingLeft(), topPadding + bars.top,
                    topbar.getPaddingRight(), topbar.getPaddingBottom());
            return windowInsets;
        });
    }

    private void setupTopbar() {
        title.setText(getIntent().getStringExtra("title"));
        findViewById(R.id.btnProductListBack).setOnClickListener(view -> finish());
    }

    private void setupProductList() {
        productsView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductCardAdapter(this, new ArrayList<>(),
                ProductCardAdapter.DisplayMode.GRID, false, () ->
                Snackbar.make(productsView, "Đã thêm vào giỏ hàng",
                                Snackbar.LENGTH_SHORT)
                        .setAction("Xem giỏ", view ->
                                startActivity(new Intent(this, CartActivity.class)))
                        .show());
        productsView.setAdapter(adapter);
    }

    private void loadProducts() {
        progress.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance().collection("products").get()
                .addOnSuccessListener(snapshot -> {
                    List<Product> products = new ArrayList<>();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        Product product = ProductMapper.fromDocument(document, this);
                        if (matches(product)) {
                            products.add(product);
                        }
                    }
                    adapter.submitList(products);
                    progress.setVisibility(View.GONE);
                    boolean isEmpty = products.isEmpty();
                    productsView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                    empty.setText("Không có sản phẩm");
                    empty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(exception -> {
                    progress.setVisibility(View.GONE);
                    android.util.Log.e("ProductListActivity",
                            "Không thể tải sản phẩm", exception);
                    Snackbar.make(main, "Không thể tải sản phẩm",
                            Snackbar.LENGTH_LONG).show();
                });
    }

    private boolean matches(Product product) {
        if ("collection".equals(mode) && !equals(collection, product.getCollection())) {
            return false;
        }
        if ("gender".equals(mode) && !equals(gender, product.getGender())) {
            return false;
        }
        if ("gender_type".equals(mode)
                && (!equals(gender, product.getGender())
                || !equals(type, product.getType()))) {
            return false;
        }
        return !"sale".equals(mode) || product.getDiscountPercent() > 0;
    }

    private boolean equals(String expected, String actual) {
        return expected != null && expected.equals(actual);
    }
}
