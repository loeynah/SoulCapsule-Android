package com.finalwork.soulcapsule.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {User.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract UserDao userDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "soul_capsule_db"
                    ).allowMainThreadQueries().build();
                    seedDemoAccount(instance);
                }
            }
        }
        return instance;
    }

    private static void seedDemoAccount(AppDatabase database) {
        UserDao userDao = database.userDao();
        if (userDao.count() == 0) {
            userDao.insert(new User("admin", UserRepository.hashPassword("123456")));
        }
    }
}
