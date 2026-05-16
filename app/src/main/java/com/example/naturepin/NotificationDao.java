package com.example.naturepin;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE receiverUid = :userId ORDER BY timestamp DESC")
    List<Notification> getNotificationsForUser(String userId);

    @Insert
    void insert(Notification notification);
}
