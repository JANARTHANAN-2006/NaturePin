package com.example.naturepin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private List<Comment> comments;

    public CommentAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.tvUser.setText(comment.userName);
        holder.tvText.setText(comment.text);
        
        if (comment.userProfilePic != null && !comment.userProfilePic.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(comment.userProfilePic)
                .circleCrop()
                .into(holder.ivPfp);
        } else {
            holder.ivPfp.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    @Override
    public int getItemCount() {
        return comments != null ? comments.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser, tvText;
        ImageView ivPfp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUser = itemView.findViewById(R.id.tv_comment_user);
            tvText = itemView.findViewById(R.id.tv_comment_text);
            ivPfp = itemView.findViewById(R.id.iv_comment_pfp);
        }
    }
}
