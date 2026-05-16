package com.example.naturepin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class ProfileGridAdapter extends RecyclerView.Adapter<ProfileGridAdapter.ViewHolder> {
    private List<Post> posts;
    private OnPostClickListener listener;

    public interface OnPostClickListener {
        void onPostClick(Post post);
    }

    public ProfileGridAdapter(List<Post> posts, OnPostClickListener listener) {
        this.posts = posts;
        this.listener = listener;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        
        // Get the first available image
        String imageUrl = null;
        if (post.uri1 != null && !post.uri1.isEmpty()) imageUrl = post.uri1;
        else if (post.uri2 != null && !post.uri2.isEmpty()) imageUrl = post.uri2;
        else if (post.uri3 != null && !post.uri3.isEmpty()) imageUrl = post.uri3;

        if (imageUrl != null) {
            Glide.with(holder.imageView.getContext())
                    .load(imageUrl)
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPostClick(post);
        });
    }

    @Override
    public int getItemCount() {
        return posts != null ? posts.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_grid_post_image);
        }
    }
}
