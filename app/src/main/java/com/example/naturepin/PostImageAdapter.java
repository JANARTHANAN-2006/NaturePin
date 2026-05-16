package com.example.naturepin;

import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class PostImageAdapter extends RecyclerView.Adapter<PostImageAdapter.ViewHolder> {
    private List<String> imageUrls;
    private OnDoubleTapListener doubleTapListener;

    public interface OnDoubleTapListener {
        void onDoubleTap();
    }

    public PostImageAdapter(List<String> imageUrls, OnDoubleTapListener doubleTapListener) {
        this.imageUrls = imageUrls;
        this.doubleTapListener = doubleTapListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String url = imageUrls.get(position);
        if (url != null && !url.isEmpty()) {
            Glide.with(holder.imageView.getContext())
                    .load(url)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .centerCrop()
                    .into(holder.imageView);
        }

        GestureDetector gestureDetector = new GestureDetector(holder.itemView.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (doubleTapListener != null) {
                    doubleTapListener.onDoubleTap();
                    animateHeart(holder.ivHeart);
                }
                return true;
            }
        });

        holder.itemView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });
    }

    private void animateHeart(ImageView ivHeart) {
        ivHeart.setAlpha(1.0f);
        ivHeart.setScaleX(0f);
        ivHeart.setScaleY(0f);

        ivHeart.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(300)
                .setInterpolator(new AnticipateOvershootInterpolator())
                .withEndAction(() -> {
                    ivHeart.animate()
                            .scaleX(0f)
                            .scaleY(0f)
                            .alpha(0f)
                            .setDuration(300)
                            .setStartDelay(200)
                            .start();
                }).start();
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, ivHeart;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_post_image_content);
            ivHeart = itemView.findViewById(R.id.iv_heart_overlay);
        }
    }
}
