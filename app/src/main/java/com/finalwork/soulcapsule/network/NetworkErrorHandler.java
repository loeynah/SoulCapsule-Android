package com.finalwork.soulcapsule.network;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * 统一解析网络异常为用户可读提示。
 */
public final class NetworkErrorHandler {

    private NetworkErrorHandler() {
    }

    public static String getMessage(Throwable throwable) {
        if (throwable == null) {
            return "网络连接失败，请稍后重试";
        }
        Throwable root = unwrap(throwable);
        if (root instanceof SocketTimeoutException) {
            return "网络请求超时，请检查网络";
        }
        if (root instanceof UnknownHostException) {
            return "似乎断网了哦";
        }
        if (root instanceof ConnectException) {
            return "似乎断网了哦";
        }
        return "网络连接失败，请检查后端状态";
    }

    private static Throwable unwrap(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }
}
