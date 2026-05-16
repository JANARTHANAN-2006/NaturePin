package com.example.naturepin;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notifications")
public class Notification {
    @PrimaryKey
    @NonNull
    public String id = "";
    public String receiverUid;
    public String senderUsername;
    public String message;
    public long timestamp;

    public Notification() {} // Needed for Firestore

    public Notification(String receiverUid, String senderUsername, String message) {
        this.id = java.util.UUID.randomUUID().toString();
        this.receiverUid = receiverUid;
        this.senderUsername = senderUsername;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
}
