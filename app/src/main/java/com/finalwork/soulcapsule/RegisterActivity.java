package com.finalwork.soulcapsule;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.finalwork.soulcapsule.db.UserRepository;

public class RegisterActivity extends AppCompatActivity {

    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MIN_PASSWORD_LENGTH = 6;

    private EditText etUsername;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button btnRegister;

    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userRepository = UserRepository.getInstance(this);

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);

        btnRegister.setOnClickListener(v -> handleRegister());
        findViewById(R.id.tv_back_login).setOnClickListener(v -> finish());
    }

    private void handleRegister() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            ToastHelper.show(this, getString(R.string.toast_empty_credentials));
            return;
        }

        if (username.length() < MIN_USERNAME_LENGTH) {
            ToastHelper.show(this, getString(R.string.toast_username_too_short));
            return;
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            ToastHelper.show(this, getString(R.string.toast_password_too_short));
            return;
        }

        if (!password.equals(confirmPassword)) {
            ToastHelper.show(this, getString(R.string.toast_password_mismatch));
            return;
        }

        btnRegister.setEnabled(false);
        userRepository.register(username, password, new UserRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    btnRegister.setEnabled(true);
                    ToastHelper.show(RegisterActivity.this, getString(R.string.toast_register_success));
                    finish();
                });
            }

            @Override
            public void onFailure(String message) {
                runOnUiThread(() -> {
                    btnRegister.setEnabled(true);
                    if ("exists".equals(message)) {
                        ToastHelper.show(RegisterActivity.this, getString(R.string.toast_username_exists));
                    } else {
                        ToastHelper.show(RegisterActivity.this, getString(R.string.toast_register_failed));
                    }
                });
            }
        });
    }
}
