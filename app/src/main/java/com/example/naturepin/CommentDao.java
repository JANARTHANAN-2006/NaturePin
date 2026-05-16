package com.example.naturepin;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CommentDao {
    @Query("SELECT * FROM comments WHERE postId = :postId")
    List<Comment> getCommentsForPost(int postId);

    @Insert
    void insert(Comment comment);
}
