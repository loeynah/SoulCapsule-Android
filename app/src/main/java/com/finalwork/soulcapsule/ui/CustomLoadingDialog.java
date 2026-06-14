package com.finalwork.soulcapsule.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.finalwork.soulcapsule.R;

/**
 * 全局半透明加载等待框，阻止重复操作。
 */
public class CustomLoadingDialog extends Dialog {

    public CustomLoadingDialog(@NonNull Context context) {
        super(context, R.style.CustomLoadingDialogTheme);
        setContentView(R.layout.dialog_custom_loading);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setDimAmount(0.45f);
        }
    }
}
