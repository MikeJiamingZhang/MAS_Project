package com.example.mas_solution_2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private Context context;
    private List<String> photoUrls;
    private PhotoClickListener clickListener;

    public interface PhotoClickListener {
        void onPhotoClick(String photoUrl, int position);
    }

    public PhotoAdapter(Context context, List<String> photoUrls, PhotoClickListener clickListener) {
        this.context = context;
        this.photoUrls = photoUrls;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        String photoUrl = photoUrls.get(position);

        // Load image using Glide
        if (photoUrl != null && !photoUrl.isEmpty()) {
            if (photoUrl.equals("add")) {
                // Special case for the "Add Photo" button
                holder.imageView.setImageResource(R.drawable.ic_launcher_foreground);
                holder.imageView.setBackgroundResource(android.R.color.darker_gray);
            } else {
                Glide.with(context)
                        .load(photoUrl)
                        .apply(new RequestOptions().centerCrop())
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(holder.imageView);
            }
        } else {
            holder.imageView.setImageResource(R.drawable.ic_launcher_background);
        }

        final int pos = position;
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onPhotoClick(photoUrl, pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return photoUrls.size();
    }

    public void updateData(List<String> newPhotoUrls) {
        this.photoUrls.clear();
        this.photoUrls.addAll(newPhotoUrls);
        notifyDataSetChanged();
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.photo_image);
        }
    }
}