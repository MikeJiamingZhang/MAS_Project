package com.example.mas_solution_2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HangoutAdapter extends RecyclerView.Adapter<HangoutAdapter.HangoutViewHolder> {

    private Context context;
    private List<Hangout> hangoutList;
    private HangoutClickListener clickListener;
    private boolean isUpcoming;

    public interface HangoutClickListener {
        void onHangoutClick(Hangout hangout);
        void onCancelClick(Hangout hangout);
        void onVoteClick(Hangout hangout);
    }

    public HangoutAdapter(Context context, List<Hangout> hangoutList, HangoutClickListener clickListener, boolean isUpcoming) {
        this.context = context;
        this.hangoutList = hangoutList;
        this.clickListener = clickListener;
        this.isUpcoming = isUpcoming;
    }

    @NonNull
    @Override
    public HangoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hangout, parent, false);
        return new HangoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HangoutViewHolder holder, int position) {
        Hangout hangout = hangoutList.get(position);

        holder.nameTextView.setText(hangout.getName());

        // Format and display date
        Timestamp timestamp = hangout.getDate();
        if (timestamp != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy, hh:mm a", Locale.getDefault());
            String dateString = dateFormat.format(timestamp.toDate());
            holder.dateTextView.setText(dateString);
        }

        // Set location
        holder.locationTextView.setText(hangout.getLocation());

        // Load image using Glide
        if (hangout.getImageUrl() != null && !hangout.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(hangout.getImageUrl())
                    .apply(new RequestOptions().centerCrop())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_launcher_background);
        }

        // Set button visibility based on whether it's an upcoming or past event
        if (isUpcoming) {
            holder.actionButton.setVisibility(View.VISIBLE);

            // Determine if this is a voting event or a confirmed event
            if (hangout.getLocation().contains("vote") || hangout.getName().contains("vote")) {
                holder.actionButton.setText("Vote");
                holder.actionButton.setBackgroundResource(android.R.color.holo_green_light);
                holder.actionButton.setOnClickListener(v -> {
                    if (clickListener != null) {
                        clickListener.onVoteClick(hangout);
                    }
                });
            } else {
                holder.actionButton.setText("Cancel");
                holder.actionButton.setBackgroundResource(android.R.color.holo_red_light);
                holder.actionButton.setOnClickListener(v -> {
                    if (clickListener != null) {
                        clickListener.onCancelClick(hangout);
                    }
                });
            }
        } else {
            holder.actionButton.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onHangoutClick(hangout);
            }
        });
    }

    @Override
    public int getItemCount() {
        return hangoutList.size();
    }

    public void updateData(List<Hangout> newHangoutList) {
        this.hangoutList.clear();
        this.hangoutList.addAll(newHangoutList);
        notifyDataSetChanged();
    }

    public static class HangoutViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView;
        TextView dateTextView;
        TextView locationTextView;
        Button actionButton;

        public HangoutViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.hangout_image);
            nameTextView = itemView.findViewById(R.id.hangout_name);
            dateTextView = itemView.findViewById(R.id.hangout_date);
            locationTextView = itemView.findViewById(R.id.hangout_location);
            actionButton = itemView.findViewById(R.id.action_button);
        }
    }
}