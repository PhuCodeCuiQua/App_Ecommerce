package com.example.login_register;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FavoriteActivity extends AppCompatActivity {
    private RecyclerView rvFavorites;
    private TextView tvEmpty;
    private ProductCardAdapter productAdapter;
    private View topbar;
    private int topbarBasePadding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_favorite);
        initViews();
        setupInsets();
        setupTopbar();
        setupFavoritesList();
        FavoriteManager.load(this, this::refreshFavorites);
        refreshFavorites();
    }

    private void initViews() {
        topbar = findViewById(R.id.topbarSimple);
        topbarBasePadding = topbar.getPaddingTop();
        rvFavorites = findViewById(R.id.rvFavorites);
        tvEmpty = findViewById(R.id.tvEmpty);
    }

    private void setupInsets() {
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), findViewById(R.id.main));
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            topbar.setPadding(topbar.getPaddingLeft(), topbarBasePadding + systemBars.top,
                    topbar.getPaddingRight(), topbar.getPaddingBottom());
            return insets;
        });
    }

    private void setupTopbar() {
        ((TextView) findViewById(R.id.tvTitle)).setText("Yêu thích");
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
    }

    private void setupFavoritesList() {
        rvFavorites.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new ProductCardAdapter(this, new ArrayList<>(),
                ProductCardAdapter.DisplayMode.GRID, true, null);
        productAdapter.setFavoriteChangedListener(product -> refreshFavorites());
        rvFavorites.setAdapter(productAdapter);
        rvFavorites.setItemAnimator(null);
    }

    private void refreshFavorites() {
        if (productAdapter == null) {
            return;
        }
        ArrayList<Product> favorites = new ArrayList<>(FavoriteManager.getFavorites());
        productAdapter.submitList(favorites);
        boolean isEmpty = favorites.isEmpty();
        rvFavorites.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshFavorites();
    }
}
