package com.example.login_register;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ViewHistoryActivity extends AppCompatActivity {
    private ProfileDBHelper db;
    private String userKey;
    private TextView tvEmpty;
    private final ArrayList<ViewedProduct> items = new ArrayList<>();
    private RecyclerView.Adapter<VH> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_history);
        InsetsUtil.apply(this);
        db = new ProfileDBHelper(this);
        userKey = SharePreferance.getUserKey();

        ((TextView) findViewById(R.id.tvTitle)).setText("Lịch sử xem");
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        tvEmpty = findViewById(R.id.tvEmpty);

        adapter = new RecyclerView.Adapter<VH>() {
            private final SimpleDateFormat fmt =
                    new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

            @NonNull
            @Override
            public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new VH(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_viewed, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull VH h, int position) {
                ViewedProduct p = items.get(position);
                h.tvName.setText(p.name);
                h.tvPrice.setText(PriceFormatter.format(p.price));
                h.tvTime.setText("Đã xem lúc " + fmt.format(new Date(p.viewedAt)));
                h.itemView.setOnClickListener(v -> openProduct(p));
            }

            @Override
            public int getItemCount() {
                return items.size();
            }
        };
        RecyclerView rv = findViewById(R.id.rvViewed);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        findViewById(R.id.btnClear).setOnClickListener(v -> new AlertDialog.Builder(this)
                .setMessage("Xóa toàn bộ lịch sử xem?")
                .setPositiveButton("Xóa", (d, w) -> {
                    db.clearViewed(userKey);
                    refresh();
                })
                .setNegativeButton("Hủy", null)
                .show());
        refresh();
    }

    private void refresh() {
        items.clear();
        items.addAll(db.getViewed(userKey));
        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void openProduct(ViewedProduct p) {
        if (p.product == null) {
            Toast.makeText(this, "Không thể mở sản phẩm này", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("product", p.product);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvTime;

        VH(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tvViewedName);
            tvPrice = v.findViewById(R.id.tvViewedPrice);
            tvTime = v.findViewById(R.id.tvViewedTime);
        }
    }
}