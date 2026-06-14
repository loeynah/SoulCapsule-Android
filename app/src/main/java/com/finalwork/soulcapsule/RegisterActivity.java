package com.finalwork.soulcapsule;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.finalwork.soulcapsule.dto.ApiResponse;
import com.finalwork.soulcapsule.dto.UserRequest;
import com.finalwork.soulcapsule.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends BaseActivity {

    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MIN_PASSWORD_LENGTH = 6;

    private EditText etUsername;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button btnRegister;

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

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            ToastHelper.show(this, getString(R.string.toast_empty_credentials));
            return;
        }

        if (!password.equals(confirmPassword)) {
            ToastHelper.show(this, getString(R.string.toast_password_mismatch));
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

        btnRegister.setEnabled(false);
        showLoading();

        UserRequest request = new UserRequest(username, password);
        RetrofitClient.getInstance().getApiService()
                .register(request)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call,
                                           Response<ApiResponse<Void>> response) {
                        hideLoading();
                        if (!isActivityAlive()) {
                            return;
                        }
                        btnRegister.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getCode() == 200) {
                            ToastHelper.show(RegisterActivity.this, "注册成功！");
                            finish();
                        } else {
                            String message = response.body() != null
                                    && response.body().getMessage() != null
                                    ? response.body().getMessage()
                                    : "网络异常，注册失败";
                            ToastHelper.show(RegisterActivity.this, message);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        hideLoading();
                        if (!isActivityAlive()) {
                            return;
                        }
                        btnRegister.setEnabled(true);
                        showNetworkError(t);
                    }
                });
    }
}
