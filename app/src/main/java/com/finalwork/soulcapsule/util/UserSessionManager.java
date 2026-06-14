package com.finalwork.soulcapsule.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 使用 SharedPreferences 持久化登录用户状态。
 */
public final class UserSessionManager {

    private static final String PREF_NAME = "user_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";

    private UserSessionManager() {
    }

    public static void saveSession(Context context, long userId, String username) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putLong(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .apply();
    }

    public static long getUserId(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getLong(KEY_USER_ID, -1L);
    }

    public static String getUsername(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_USERNAME, "");
    }

    public static boolean isLoggedIn(Context context) {
        return getUserId(context) > 0 && !getUsername(context).isEmpty();
    }

    public static void clearSession(Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }
}
