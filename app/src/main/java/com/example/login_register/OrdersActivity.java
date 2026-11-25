package com.example.login_register;

import android.content.res.Configuration;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.Collections;
import java.util.List;

public class OrdersActivity extends AppCompatActivity {
    private View main;
    private View header;
    private RecyclerView rvOrders;
    private ProgressBar progressOrders;
    private TextView tvEmpty;
    private OrdersAdapter ordersAdapter;
    private int headerPaddingTop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_orders);
        initViews();
        setupInsets();
        setupHeader();
        setupList();
    }

    private void initViews() {
        main = findViewById(R.id.main);
        header = findViewById(R.id.ordersHeader);
        rvOrders = findViewById(R.id.rvOrders);
        progressOrders = findViewById(R.id.progressOrders);
        tvEmpty = findViewById(R.id.tvEmpty);
        headerPaddingTop = header.getPaddingTop();
    }

    private void setupInsets() {
        boolean nightMode = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), main);
        controller.setAppearanceLightStatusBars(!nightMode);
        controller.setAppearanceLightNavigationBars(!nightMode);
        ViewCompat.setOnApplyWindowInsetsListener(main, (view, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            header.setPadding(header.getPaddingLeft(),
                    headerPaddingTop + systemBars.top, header.getPaddingRight(),
                    header.getPaddingBottom());
            return windowInsets;
        });
    }

    private void setupHeader() {
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
    }

    private void setupList() {
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        ordersAdapter = new OrdersAdapter(this);
        rvOrders.setAdapter(ordersAdapter);
    }

    private void loadOrders() {
        progressOrders.setVisibility(View.VISIBLE);
        rvOrders.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);
        OrderRepository.loadOrders(this, new OrderRepository.LoadCallback() {
            @Override
            public void onSuccess(List<Order> orders) {
                if (isFinishing()) {
                    return;
                }
                progressOrders.setVisibility(View.GONE);
                if (orders.isEmpty()) {
                    showEmpty();
                } else {
                    showOrders(orders);
                }
            }

            @Override
            public void onFailure(Exception exception) {
                if (isFinishing()) {
                    return;
                }
                progressOrders.setVisibility(View.GONE);
                Snackbar.make(main, "Không thể tải đơn hàng", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void showOrders(List<Order> orders) {
        ordersAdapter.submitList(orders);
        rvOrders.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
    }

    private void showEmpty() {
        ordersAdapter.submitList(Collections.emptyList());
        rvOrders.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }
}
