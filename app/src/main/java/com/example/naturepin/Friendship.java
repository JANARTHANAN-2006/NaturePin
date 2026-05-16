package com.example.naturepin;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "friendships")
public class Friendship {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String userUid1;
    public String userUid2;
    public String status; // "pending", "accepted", "community_joined"

    public Friendship() {}

    public Friendship(String userUid1, String userUid2, String status) {
        this.userUid1 = userUid1;
        this.userUid2 = userUid2;
        this.status = status;
    }
}
