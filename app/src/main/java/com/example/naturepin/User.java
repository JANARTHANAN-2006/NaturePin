package com.example.naturepin;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String uid; // Firebase UID
    public String email;
    public String username;
    public String password;
    public String bio;
    public String age;
    public String gender;
    public String profilePicUri;
    public String accountType; // "individual" or "corporate"
    public String welcomeMessage; // Personalised message for new friends
    public int friendsCount;
    public int pinsCount;

    public User() {}

    public User(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.bio = "";
        this.age = "";
        this.gender = "";
        this.profilePicUri = "";
        this.accountType = "individual";
        this.welcomeMessage = "Hi! Thanks for befriending me.";
        this.friendsCount = 0;
        this.pinsCount = 0;
    }
}
