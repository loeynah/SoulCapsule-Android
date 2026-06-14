package com.finalwork.soulcapsule;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.finalwork.soulcapsule.dto.ApiResponse;
import com.finalwork.soulcapsule.dto.LoginResponse;
import com.finalwork.soulcapsule.dto.UserRequest;
import com.finalwork.soulcapsule.network.RetrofitClient;
import com.finalwork.soulcapsule.util.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SessionManager.isLoggedIn(this)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(v -> handleLogin());
        findViewById(R.id.tv_register).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            ToastHelper.show(this, getString(R.string.toast_empty_credentials));
            return;
        }

        btnLogin.setEnabled(false);
        showLoading();

        UserRequest request = new UserRequest(username, password);
        RetrofitClient.getInstance().getApiService()
                .login(request)
                .enqueue(new Callback<ApiResponse<LoginResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<LoginResponse>> call,
                                           Response<ApiResponse<LoginResponse>> response) {
                        hideLoading();
                        if (!isActivityAlive()) {
                            return;
                        }
                        btnLogin.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getCode() == 200
                                && response.body().getData() != null) {
                            LoginResponse loginData = response.body().getData();
                            Long userId = loginData.getUserId();
                            if (userId == null) {
                                ToastHelper.show(LoginActivity.this, "登录失败，请稍后重试");
                                return;
                            }
                            SessionManager.saveLogin(
                                    LoginActivity.this,
                                    userId,
                                    loginData.getUsername()
                            );
                            ToastHelper.show(LoginActivity.this, "欢迎回来！");
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            String message = response.body() != null
                                    && response.body().getMessage() != null
                                    ? response.body().getMessage()
                                    : getString(R.string.toast_login_failed);
                            ToastHelper.show(LoginActivity.this, message);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                        hideLoading();
                        if (!isActivityAlive()) {
                            return;
                        }
                        btnLogin.setEnabled(true);
                        showNetworkError(t);
                    }
                });
    }
}
