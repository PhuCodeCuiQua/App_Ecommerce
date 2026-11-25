package com.example.login_register;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class CartActivity extends AppCompatActivity {
    private ListView listView;
    private TextView tvEmpty;
    private TextView tvTotal;
    private MaterialButton btnPlaceOrder;
    private CartAdapter cartAdapter;
    private View topbar;
    private View bottomBar;
    private int topbarBasePadding;
    private int bottomBarBasePadding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_cart);
        initViews();
        setupInsets();
        setupTopbar();
        setupList();
        setupPlaceOrder();
        CartManager.load(this, this::refreshCart);
        refreshCart();
    }

    private void initViews() {
        topbar = findViewById(R.id.topbarSimple);
        bottomBar = findViewById(R.id.cartBottomBar);
        listView = findViewById(R.id.listViewCart);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvTotal = findViewById(R.id.tvTotal);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        topbarBasePadding = topbar.getPaddingTop();
        bottomBarBasePadding = bottomBar.getPaddingBottom();
    }

    private void setupInsets() {
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), findViewById(R.id.main));
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(false);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, 0, systemBars.right, 0);
            topbar.setPadding(topbar.getPaddingLeft(), topbarBasePadding + systemBars.top,
                    topbar.getPaddingRight(), topbar.getPaddingBottom());
            bottomBar.setPadding(bottomBar.getPaddingLeft(), bottomBar.getPaddingTop(),
                    bottomBar.getPaddingRight(), bottomBarBasePadding + systemBars.bottom);
            return insets;
        });
    }

    private void setupTopbar() {
        ((TextView) findViewById(R.id.tvTitle)).setText("Giỏ hàng");
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
    }

    private void setupList() {
        cartAdapter = new CartAdapter(this, new ArrayList<>(), this::refreshCart);
        listView.setAdapter(cartAdapter);
    }

    private void setupPlaceOrder() {
        btnPlaceOrder.setOnClickListener(view ->
                startActivity(new Intent(this, CheckoutActivity.class)));
    }

    private void refreshCart() {
        if (cartAdapter == null) {
            return;
        }
        cartAdapter.clear();
        cartAdapter.addAll(CartManager.getItems());
        boolean isEmpty = cartAdapter.isEmpty();
        listView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        bottomBar.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        btnPlaceOrder.setEnabled(!isEmpty);
        tvTotal.setText(PriceFormatter.format(CartManager.getTotal()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCart();
    }
}
