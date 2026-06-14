package com.finalwork.soulcapsule;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public final class ToastHelper {

    private ToastHelper() {
    }

    public static void show(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
