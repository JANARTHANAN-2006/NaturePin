package com.example.naturepin;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PostDao {
    @Query("SELECT * FROM posts")
    List<Post> getAll();

    @Insert
    void insert(Post post);
}
