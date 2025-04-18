package com.example.watch_gatherers;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private Context context;
    private List<Group> groups;
    private GroupClickListener listener;
    private FirebaseAnalytics mFirebaseAnalytics;

    public interface GroupClickListener {
        void onGroupClick(Group group);
    }

    public GroupAdapter(Context context, List<Group> groups, GroupClickListener listener) {
        this.context = context;
        this.groups = groups;
        this.listener = listener;
        this.mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groups.get(position);
        holder.nameTextView.setText(group.getName());

        holder.itemView.setOnClickListener(v -> {
            // Track group click event
            Bundle params = new Bundle();
            params.putString(FirebaseAnalytics.Param.ITEM_ID, group.getId());
            params.putString(FirebaseAnalytics.Param.ITEM_NAME, group.getName());
            mFirebaseAnalytics.logEvent("group_clicked", params);
            
            if (listener != null) {
                listener.onGroupClick(group);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;

        GroupViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.group_name);
        }
    }
}