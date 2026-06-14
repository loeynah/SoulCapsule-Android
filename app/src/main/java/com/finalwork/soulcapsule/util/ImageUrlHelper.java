package com.finalwork.soulcapsule.util;

import android.text.TextUtils;

import com.finalwork.soulcapsule.config.AppConfig;

/**
 * 将后端返回的图片路径规范化为可访问的完整 URL。
 */
public final class ImageUrlHelper {

    private ImageUrlHelper() {
    }

    public static String resolve(String imageUrl) {
        if (TextUtils.isEmpty(imageUrl)) {
            return null;
        }
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return normalizeHostForClient(imageUrl);
        }
        String base = AppConfig.BASE_URL;
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (imageUrl.startsWith("/")) {
            return normalizeHostForClient(base + imageUrl);
        }
        return normalizeHostForClient(base + "/" + imageUrl);
    }

    /**
     * 后端可能返回 localhost，模拟器需替换为 10.0.2.2。
     */
    private static String normalizeHostForClient(String url) {
        if (url.contains("://localhost:")) {
            return url.replace("://localhost:", "://10.0.2.2:");
        }
        if (url.contains("://127.0.0.1:")) {
            return url.replace("://127.0.0.1:", "://10.0.2.2:");
        }
        return url;
    }
}
