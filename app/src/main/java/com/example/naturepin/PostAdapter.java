package com.example.naturepin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private List<Post> posts;
    private Context context;
    private int lastPosition = -1;
    private String currentUserUid;
    private Set<String> friendUids = new HashSet<>();

    public interface OnProfileClickListener {
        void onProfileClick(String uid);
    }

    private OnProfileClickListener profileClickListener;

    public void setOnProfileClickListener(OnProfileClickListener listener) {
        this.profileClickListener = listener;
    }

    public PostAdapter(List<Post> posts, Context context) {
        this.posts = posts;
        this.context = context;
        SharedPreferences pref = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        this.currentUserUid = pref.getString("userUid", null);
    }

    public void setFriendUids(Set<String> friendUids) {
        this.friendUids = (friendUids != null) ? friendUids : new HashSet<>();
        notifyDataSetChanged();
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        setAnimation(holder.itemView, position);
        
        holder.tvTitle.setText(post.title);
        holder.tvDescription.setText(post.description);
        holder.tvHashtags.setText(post.hashtags);

        if (post.user_uid != null && friendUids.contains(post.user_uid)) {
            holder.tvFriendBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvFriendBadge.setVisibility(View.GONE);
        }
        
        if (post.latitude != 0 || post.longitude != 0) {
            holder.tvLocation.setText("Location: pinned");
            holder.tvLocation.setVisibility(View.VISIBLE);
        } else {
            holder.tvLocation.setVisibility(View.GONE);
        }

        if (post.user_uid != null) {
            SupabaseHelper.INSTANCE.getUserProfile(post.user_uid, profile -> {
                if (profile != null) {
                    holder.tvUsername.setText(profile.getUsername());
                    String pfp = profile.getProfile_pic_url();
                    if (pfp != null && !pfp.isEmpty()) {
                        Glide.with(context).load(pfp).circleCrop().into(holder.ivAvatar);
                    } else {
                        holder.ivAvatar.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                } else {
                    holder.tvUsername.setText("User");
                    holder.ivAvatar.setImageResource(android.R.drawable.ic_menu_gallery);
                }
                return null;
            });
            
            View.OnClickListener toProfile = v -> {
                if (profileClickListener != null) profileClickListener.onProfileClick(post.user_uid);
            };
            holder.ivAvatar.setOnClickListener(toProfile);
            holder.tvUsername.setOnClickListener(toProfile);
        }

        holder.llCommentsPreview.removeAllViews();

        List<String> images = new ArrayList<>();
        if (post.uri1 != null && !post.uri1.isEmpty()) images.add(post.uri1);
        if (post.uri2 != null && !post.uri2.isEmpty()) images.add(post.uri2);
        if (post.uri3 != null && !post.uri3.isEmpty()) images.add(post.uri3);

        if (images.isEmpty()) {
            holder.viewPager.setVisibility(View.GONE);
        } else {
            holder.viewPager.setVisibility(View.VISIBLE);
            holder.viewPager.setAdapter(new PostImageAdapter(images, () -> handleLike(post, holder.btnLike)));
        }

        holder.btnLike.setOnClickListener(v -> handleLike(post, holder.btnLike));
        holder.btnComment.setOnClickListener(v -> showCommentsSheet(post));
        holder.tvViewComments.setOnClickListener(v -> showCommentsSheet(post));
        
        holder.btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareBody = "Check out this pin on NaturePin: " + post.title + "\n" + post.description;
            if (post.latitude != 0 || post.longitude != 0) {
                shareBody += "\nLocation: https://www.google.com/maps/search/?api=1&query=" + post.latitude + "," + post.longitude;
            }
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, post.title);
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            context.startActivity(Intent.createChooser(shareIntent, "Share post via"));
        });

        holder.btnOptions.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, v);
            if (post.user_uid != null && post.user_uid.equals(currentUserUid)) {
                popup.getMenu().add("Delete Pin");
            }
            popup.getMenu().add("Report");
            popup.getMenu().add("Copy Link");
            
            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Delete Pin")) {
                    Toast.makeText(context, "Delete from feed coming soon", Toast.LENGTH_SHORT).show();
                } else if (item.getTitle().equals("Copy Link")) {
                    Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show();
                }
                return true;
            });
            popup.show();
        });
    }

    private void handleLike(Post post, ImageButton btnLike) {
        Toast.makeText(context, "Likes coming soon", Toast.LENGTH_SHORT).show();
    }

    private void sendLikeNotification(Post post) {
        // TODO: migrate notifications to Supabase
    }

    private void loadRecentComments(Post post, LinearLayout container) {
        container.removeAllViews();
    }

    private void showCommentsSheet(Post post) {
        if (currentUserUid == null || currentUserUid.isEmpty()) {
            Toast.makeText(context, "Please login to comment", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(context, "Comments coming soon", Toast.LENGTH_SHORT).show();
        return;
    }

    private void sendCommentNotification(Post post) {
        // TODO: migrate notifications to Supabase
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            viewToAnimate.startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_in));
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() { return posts != null ? posts.size() : 0; }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvLocation, tvTitle, tvDescription, tvHashtags, tvViewComments;
        TextView tvFriendBadge;
        ImageView ivAvatar;
        ViewPager2 viewPager;
        ImageButton btnLike, btnComment, btnSave, btnShare, btnOptions;
        LinearLayout llCommentsPreview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvFriendBadge = itemView.findViewById(R.id.tv_friend_badge);
            tvLocation = itemView.findViewById(R.id.tv_post_location);
            tvTitle = itemView.findViewById(R.id.tv_post_title);
            tvDescription = itemView.findViewById(R.id.tv_post_description);
            tvHashtags = itemView.findViewById(R.id.tv_post_hashtags);
            tvViewComments = itemView.findViewById(R.id.tv_view_comments);
            ivAvatar = itemView.findViewById(R.id.iv_user_avatar);
            viewPager = itemView.findViewById(R.id.vp_images);
            btnLike = itemView.findViewById(R.id.btn_like);
            btnComment = itemView.findViewById(R.id.btn_comment);
            btnSave = itemView.findViewById(R.id.btn_save);
            btnShare = itemView.findViewById(R.id.btn_share);
            btnOptions = itemView.findViewById(R.id.btn_post_options);
            llCommentsPreview = itemView.findViewById(R.id.ll_comments_preview);
            if (tvViewComments != null) tvViewComments.setVisibility(View.VISIBLE);
        }
    }
}
