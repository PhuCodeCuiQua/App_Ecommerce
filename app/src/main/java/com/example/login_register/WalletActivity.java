package com.example.login_register;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class WalletActivity extends AppCompatActivity implements CardAdapter.Listener {
    private ProfileDBHelper db;
    private CardAdapter adapter;
    private TextView tvBalance, tvEmpty;
    private String userKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        InsetsUtil.apply(this);
        db = new ProfileDBHelper(this);
        userKey = SharePreferance.getUserKey();

        ((TextView) findViewById(R.id.tvTitle)).setText("Thẻ và ví");
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        tvBalance = findViewById(R.id.tvBalance);
        tvEmpty = findViewById(R.id.tvEmpty);

        adapter = new CardAdapter(this);
        RecyclerView rv = findViewById(R.id.rvCards);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        findViewById(R.id.btnTopUp).setOnClickListener(v -> showTopUpDialog());
        findViewById(R.id.btnAddCard).setOnClickListener(v -> showAddCardDialog());
        refresh();
    }

    private void refresh() {
        tvBalance.setText(String.format(Locale.US, "%,.0f đ", db.getBalance(userKey)));
        ArrayList<Card> cards = db.getCards(userKey);
        adapter.submit(cards);
        tvEmpty.setVisibility(cards.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showTopUpDialog() {
        EditText ed = new EditText(this);
        ed.setInputType(InputType.TYPE_CLASS_NUMBER);
        ed.setHint("Số tiền (đ)");
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        FrameLayout box = new FrameLayout(this);
        box.setPadding(pad, pad / 2, pad, 0);
        box.addView(ed);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Nạp tiền vào ví")
                .setView(box)
                .setPositiveButton("Nạp", null)
                .setNegativeButton("Hủy", null)
                .create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String s = ed.getText().toString().trim();
            if (s.isEmpty() || s.length() > 10) { ed.setError("Số tiền không hợp lệ"); return; }
            double amount = Double.parseDouble(s);
            if (amount < 10000) { ed.setError("Tối thiểu 10.000 đ"); return; }
            db.addBalance(userKey, amount);
            refresh();
            dialog.dismiss();
        });
    }

    private void showAddCardDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_card, null);
        EditText edHolder = view.findViewById(R.id.edHolder);
        EditText edNumber = view.findViewById(R.id.edNumber);
        EditText edExpiry = view.findViewById(R.id.edExpiry);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Thêm thẻ")
                .setView(view)
                .setPositiveButton("Lưu", null)
                .setNegativeButton("Hủy", null)
                .create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String holder = edHolder.getText().toString().trim().toUpperCase(Locale.ROOT);
            String number = edNumber.getText().toString().replaceAll("\\s", "");
            String expiry = edExpiry.getText().toString().trim();
            if (holder.isEmpty()) { edHolder.setError("Nhập tên chủ thẻ"); return; }
            if (!number.matches("\\d{13,19}")) { edNumber.setError("Số thẻ phải từ 13 đến 19 chữ số"); return; }
            if (!expiry.matches("(0[1-9]|1[0-2])/\\d{2}")) { edExpiry.setError("Định dạng MM/YY"); return; }
            String last4 = number.substring(number.length() - 4);
            String brand = number.startsWith("4") ? "VISA"
                    : number.startsWith("5") ? "MASTERCARD" : "THẺ";
            db.addCard(userKey, new Card(0, holder, last4, expiry, brand));
            refresh();
            dialog.dismiss();
        });
    }

    @Override
    public void onDelete(Card card) {
        new AlertDialog.Builder(this)
                .setMessage("Xóa thẻ •••• " + card.last4 + "?")
                .setPositiveButton("Xóa", (d, w) -> {
                    db.deleteCard(userKey, card.id);
                    refresh();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}