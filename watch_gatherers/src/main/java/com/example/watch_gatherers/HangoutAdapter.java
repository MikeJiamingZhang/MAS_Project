package com.example.watch_gatherers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HangoutAdapter extends RecyclerView.Adapter<HangoutAdapter.HangoutViewHolder> {

    private Context context;
    private List<Hangout> hangouts;

    public HangoutAdapter(Context context, List<Hangout> hangouts) {
        this.context = context;
        this.hangouts = hangouts;
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

        holder.nameTextView.setText(hangout.getName());

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