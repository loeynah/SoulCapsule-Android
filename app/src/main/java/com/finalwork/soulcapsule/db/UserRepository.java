package com.finalwork.soulcapsule.db;

import android.content.Context;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {

    public interface AuthCallback {
        void onSuccess();

        void onFailure(String message);
    }

    private static volatile UserRepository instance;

    private final UserDao userDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private UserRepository(Context context) {
        userDao = AppDatabase.getInstance(context).userDao();
    }

    public static UserRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (UserRepository.class) {
                if (instance == null) {
                    instance = new UserRepository(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public void register(String username, String password, AuthCallback callback) {
        executor.execute(() -> {
            if (userDao.findByUsername(username) != null) {
                postFailure(callback, "exists");
                return;
            }
            try {
                userDao.insert(new User(username, hashPassword(password)));
                postSuccess(callback);
            } catch (Exception e) {
                postFailure(callback, "unknown");
            }
        });
    }

    public void login(String username, String password, AuthCallback callback) {
        executor.execute(() -> {
            User user = userDao.findByUsername(username);
            if (user == null || !user.getPasswordHash().equals(hashPassword(password))) {
                postFailure(callback, "invalid");
                return;
            }
            postSuccess(callback);
        });
    }

    private void postSuccess(AuthCallback callback) {
        if (callback != null) {
            callback.onSuccess();
        }
    }

    private void postFailure(AuthCallback callback, String reason) {
        if (callback != null) {
            callback.onFailure(reason);
        }
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte value : hash) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
