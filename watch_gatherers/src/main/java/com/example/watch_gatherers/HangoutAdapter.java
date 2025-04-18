package com.example.watch_gatherers;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HangoutAdapter extends RecyclerView.Adapter<HangoutAdapter.HangoutViewHolder> {

    private Context context;
    private List<Hangout> hangouts;
    private boolean showGroupName;
    private Map<String, String> groupIdToName = new HashMap<>();
    private FirebaseAnalytics mFirebaseAnalytics;

    public HangoutAdapter(Context context, List<Hangout> hangouts) {
        this(context, hangouts, false);
    }

    public HangoutAdapter(Context context, List<Hangout> hangouts, boolean showGroupName) {
        this.context = context;
        this.hangouts = hangouts;
        this.showGroupName = showGroupName;
        this.mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    // Add this method to fix the error
    public void setGroupIdToName(Map<String, String> groupIdToName) {
        this.groupIdToName = groupIdToName;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HangoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_hangout, parent, false);
        return new HangoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HangoutViewHolder holder, int position) {
        Hangout hangout = hangouts.get(position);

        // Set name (with group name if showing all)
        if (showGroupName && hangout.getGroupId() != null) {
            String groupName = groupIdToName.get(hangout.getGroupId());
            if (groupName != null && !groupName.isEmpty()) {
                holder.nameTextView.setText(hangout.getName() + " (" + groupName + ")");
            } else {
                holder.nameTextView.setText(hangout.getName());
            }
        } else {
            holder.nameTextView.setText(hangout.getName());
        }

        // Format and set date
        Timestamp timestamp = hangout.getDate();
        if (timestamp != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
            String dateStr = dateFormat.format(timestamp.toDate());
            holder.dateTextView.setText(dateStr);
        } else {
            holder.dateTextView.setText("No date");
        }

        holder.locationTextView.setText(hangout.getLocation());
        
        // Set click listener to track hangout view
        holder.itemView.setOnClickListener(v -> {
            // Track hangout view event
            Bundle params = new Bundle();
            params.putString(FirebaseAnalytics.Param.ITEM_ID, hangout.getId());
            params.putString(FirebaseAnalytics.Param.ITEM_NAME, hangout.getName());
            params.putString("hangout_location", hangout.getLocation());
            if (hangout.getGroupId() != null) {
                params.putString("group_id", hangout.getGroupId());
            }
            mFirebaseAnalytics.logEvent("hangout_viewed", params);
        });
    }

    @Override
    public int getItemCount() {
        return hangouts.size();
    }

    static class HangoutViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView dateTextView;
        TextView locationTextView;

        HangoutViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.hangout_name);
            dateTextView = itemView.findViewById(R.id.hangout_date);
            locationTextView = itemView.findViewById(R.id.hangout_location);
        }
    }
}