package com.finalwork.soulcapsule;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.finalwork.soulcapsule.network.NetworkErrorHandler;
import com.finalwork.soulcapsule.ui.CustomLoadingDialog;

/**
 * 提供全局 Loading 与网络异常提示的 Activity 基类。
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Nullable
    private CustomLoadingDialog loadingDialog;

    protected void showLoading() {
        if (!isActivityAlive()) {
            return;
        }
        if (loadingDialog == null) {
            loadingDialog = new CustomLoadingDialog(this);
        }
        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    protected void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    protected boolean isActivityAlive() {
        return !isFinishing() && !isDestroyed();
    }

    protected void showNetworkError(Throwable throwable) {
        if (!isActivityAlive()) {
            return;
        }
        ToastHelper.show(this, NetworkErrorHandler.getMessage(throwable));
    }

    @Override
    protected void onDestroy() {
        hideLoading();
        loadingDialog = null;
        super.onDestroy();
    }
}
