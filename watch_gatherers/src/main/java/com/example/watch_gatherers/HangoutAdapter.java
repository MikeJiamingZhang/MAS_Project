package com.example.watch_gatherers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

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


    public HangoutAdapter(Context context, List<Hangout> hangouts) {
        this(context, hangouts, false);
    }

    public HangoutAdapter(Context context, List<Hangout> hangouts, boolean showGroupName) {
        this.context = context;
        this.hangouts = hangouts;
        this.showGroupName = showGroupName;
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
        String me = "RWmRbh838Tf1u0u8BsatsqFj1m02";
        holder.rsvpToggle.setOnCheckedChangeListener(null); // refresh it make sure it doens't stop workin
//        holder.rsvpToggle.setChecked(hangout.getParticipants().contains(me)); // check is it in there first?
//        holder.rsvpToggle.setEnabled(!hangout.isPast()); // disable if past event
        holder.rsvpToggle.setOnCheckedChangeListener(((buttonView, isChecked) -> { // do the stuff
            if(isChecked){
                FirebaseFirestore.getInstance().collection("hangouts").document(hangout.getId()).update("participants", FieldValue.arrayUnion(me)).addOnSuccessListener(unused -> {
//                    hangout.addParticipant(me);
                    Toast.makeText(context, "RSVP Added", Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                });
            }
            else{
                FirebaseFirestore.getInstance().collection("hangouts").document(hangout.getId()).update("participants", FieldValue.arrayRemove(me)).addOnSuccessListener(unused -> {
//                    hangout.removeParticipant(me);
                    Toast.makeText(context, "RSVP Removed", Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                });
            }
        }));
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
    }

    @Override
    public int getItemCount() {
        return hangouts.size();
    }

    static class HangoutViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView dateTextView;
        TextView locationTextView;

        Switch rsvpToggle;

        HangoutViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.hangout_name);
            dateTextView = itemView.findViewById(R.id.hangout_date);
            locationTextView = itemView.findViewById(R.id.hangout_location);
            rsvpToggle = itemView.findViewById(R.id.rsvpSwitch);
        }
    }
}