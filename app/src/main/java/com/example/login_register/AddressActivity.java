package com.example.login_register;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AddressActivity extends AppCompatActivity implements AddressAdapter.Listener {
    private ProfileDBHelper db;
    private AddressAdapter adapter;
    private TextView tvEmpty;
    private String userKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);
        InsetsUtil.apply(this);
        db = new ProfileDBHelper(this);
        userKey = SharePreferance.getUserKey();

        ((TextView) findViewById(R.id.tvTitle)).setText("Địa chỉ");
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        tvEmpty = findViewById(R.id.tvEmpty);

        adapter = new AddressAdapter(this);
        RecyclerView rv = findViewById(R.id.rvAddresses);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        findViewById(R.id.btnAddAddress).setOnClickListener(v -> showDialog(null));
        refresh();
    }

    private void refresh() {
        java.util.ArrayList<Address> data = db.getAddresses(userKey);
        adapter.submit(data);
        tvEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showDialog(Address existing) {
        View view = getLayoutInflater().inflate(R.layout.dialog_address, null);
        EditText edName = view.findViewById(R.id.edName);
        EditText edPhone = view.findViewById(R.id.edPhone);
        EditText edDetail = view.findViewById(R.id.edDetail);
        CheckBox cbDefault = view.findViewById(R.id.cbDefault);
        if (existing != null) {
            edName.setText(existing.name);
            edPhone.setText(existing.phone);
            edDetail.setText(existing.detail);
            cbDefault.setChecked(existing.isDefault);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(existing == null ? "Thêm địa chỉ" : "Sửa địa chỉ")
                .setView(view)
                .setPositiveButton("Lưu", null)
                .setNegativeButton("Hủy", null)
                .create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = edName.getText().toString().trim();
            String phone = edPhone.getText().toString().trim();
            String detail = edDetail.getText().toString().trim();
            if (name.isEmpty()) { edName.setError("Nhập tên người nhận"); return; }
            if (!phone.matches("0\\d{9,10}")) { edPhone.setError("Số điện thoại không hợp lệ"); return; }
            if (detail.isEmpty()) { edDetail.setError("Nhập địa chỉ chi tiết"); return; }
            long id = existing == null ? 0 : existing.id;
            db.saveAddress(userKey, new Address(id, name, phone, detail, cbDefault.isChecked()));
            refresh();
            dialog.dismiss();
        });
    }

    @Override
    public void onEdit(Address a) {
        showDialog(a);
    }

    @Override
    public void onDelete(Address a) {
        new AlertDialog.Builder(this)
                .setMessage("Xóa địa chỉ này?")
                .setPositiveButton("Xóa", (d, w) -> {
                    db.deleteAddress(userKey, a);
                    refresh();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onSetDefault(Address a) {
        db.setDefaultAddress(userKey, a.id);
        refresh();
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}