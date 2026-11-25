package com.example.login_register;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {
    private EditText edtEmail;
    private EditText edtPassword;
    private EditText edtConfirm;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_register);
        setupInsets();
        initViews();
        setupListeners();
    }

    // Thiết lập vùng hiển thị an toàn theo system bars.
    private void setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Lấy các view nhập liệu và nút đăng ký.
    private void initViews() {
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirm = findViewById(R.id.edtConfirm);
        btnRegister = findViewById(R.id.btnRegister);
    }

    // Gắn sự kiện xử lý đăng ký.
    private void setupListeners() {
        btnRegister.setOnClickListener(v -> {
            handleRegister();
        });
    }

    // Kiểm tra các trường nhập theo thứ tự hiện tại.
    private boolean validateInput() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirm = edtConfirm.getText().toString().trim();

        if (email.isEmpty()) {
            edtEmail.setError("Vui lòng nhập email");
            return false;
        }
        if (password.isEmpty()) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            return false;
        }
        if (confirm.isEmpty()) {
            edtConfirm.setError("Vui lòng xác nhận mật khẩu");
            return false;
        }
        if (!password.equals(confirm)) {
            edtConfirm.setError("Mật khẩu không khớp");
            return false;
        }
        return true;
    }

    // Hiển thị kết quả đăng ký hợp lệ và đóng trang.
    private void handleRegister() {
        if (!validateInput()) {
            return;
        }
        Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
        finish();
    }
}