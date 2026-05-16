package com.example.naturepin;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "posts")
public class Post {
    @PrimaryKey
    @NonNull
    public String id = ""; // Supabase UUID
    
    public String user_uid; // Supabase User ID
    public String title;
    public String description;
    public String hashtags;
    public double latitude;
    public double longitude;
    public String uri1;
    public String uri2;
    public String uri3;
    public long timestamp;

    public Post() {}

    @Ignore
    public Post(String user_uid, String title, String description, String hashtags, double latitude, double longitude, String uri1, String uri2, String uri3) {
        this.user_uid = user_uid;
        this.title = title;
        this.description = description;
        this.hashtags = hashtags;
        this.latitude = latitude;
        this.longitude = longitude;
        this.uri1 = uri1;
        this.uri2 = uri2;
        this.uri3 = uri3;
        this.timestamp = System.currentTimeMillis();
    }
}
