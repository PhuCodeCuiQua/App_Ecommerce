package com.example.login_register;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FavoriteActivity extends AppCompatActivity {
    private RecyclerView rvFavorites;
    private TextView tvEmpty;
    private ProductCardAdapter productAdapter;
    private View topbar;

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
        rvFavorites = findViewById(R.id.rvFavorites);
        tvEmpty = findViewById(R.id.tvEmpty);
    }

    private void setupInsets() {
        BottomNav.applyInsets(this, findViewById(R.id.main), topbar, null);
    }

    private void setupTopbar() {
        ((TextView) findViewById(R.id.tvTitle)).setText("Yêu thích");
        View backButton = findViewById(R.id.btnBack);
        backButton.setVisibility(View.VISIBLE);
        backButton.setOnClickListener(view -> finish());
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
