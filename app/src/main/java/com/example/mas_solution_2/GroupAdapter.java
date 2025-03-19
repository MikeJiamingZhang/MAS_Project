package com.example.mas_solution_2;

import android.content.Context;
import android.content.Intent;
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

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private Context context;
    private List<Group> groupList;
    private GroupClickListener clickListener;

    public interface GroupClickListener {
        void onGroupClick(Group group);
        void onLeaveClick(Group group);
        void onChatClick(Group group); // Added a new click listener for chat
    }

    public GroupAdapter(Context context, List<Group> groupList, GroupClickListener clickListener) {
        this.context = context;
        this.groupList = groupList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groupList.get(position);

        holder.groupNameTextView.setText(group.getName());

        // Load image using Glide
        if (group.getImageUrl() != null && !group.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(group.getImageUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(holder.groupImageView);
        } else {
            holder.groupImageView.setImageResource(R.drawable.ic_launcher_foreground);
        }

        holder.itemView.setOnClickListener(view -> {
            if (clickListener != null) {
                clickListener.onGroupClick(group);
            }
        });

        holder.leaveButton.setOnClickListener(view -> {
            if (clickListener != null) {
                clickListener.onLeaveClick(group);
            }
        });

        // Add click listener for chat button
        holder.chatButton.setOnClickListener(view -> {
            if (clickListener != null) {
                clickListener.onChatClick(group);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public void updateData(List<Group> newGroupList) {
        this.groupList.clear();
        this.groupList.addAll(newGroupList);
        notifyDataSetChanged();
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        ImageView groupImageView;
        TextView groupNameTextView;
        Button leaveButton;
        Button chatButton; // Added a chat button

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            groupImageView = itemView.findViewById(R.id.group_image);
            groupNameTextView = itemView.findViewById(R.id.group_name);
            leaveButton = itemView.findViewById(R.id.leave_button);
            chatButton = itemView.findViewById(R.id.chat_button); // Get reference to chat button
        }
    }
}