package com.example.naturepin;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Post.class, Comment.class, User.class, Notification.class}, version = 6, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PostDao postDao();
    public abstract CommentDao commentDao();
    public abstract UserDao userDao();
    public abstract NotificationDao notificationDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "naturepin_database")
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
