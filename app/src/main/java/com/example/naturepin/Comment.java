package com.example.naturepin;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "comments")
public class Comment {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int postId;
    public String text;
    public String userName;
    public String userProfilePic; // Added to show PFP in comments

    public Comment(int postId, String text, String userName, String userProfilePic) {
        this.postId = postId;
        this.text = text;
        this.userName = userName;
        this.userProfilePic = userProfilePic;
    }
}
