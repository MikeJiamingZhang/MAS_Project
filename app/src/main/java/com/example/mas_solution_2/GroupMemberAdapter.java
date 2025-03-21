package com.example.mas_solution_2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.MemberViewHolder> {

    private Context context;
    private List<String> memberNames;
    private String currentUserId;

    public GroupMemberAdapter(Context context, List<String> memberNames) {
        this.context = context;
        this.memberNames = memberNames;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        String memberName = memberNames.get(position);

        if (memberName.equals(FirebaseAuth.getInstance().getCurrentUser().getDisplayName())) {
            memberName += " (You)";
        }

        holder.textView.setText(memberName);
    }

    @Override
    public int getItemCount() {
        return memberNames.size();
    }

    public void updateMembers(List<String> memberNames) {
        this.memberNames.clear();
        this.memberNames.addAll(memberNames);
        notifyDataSetChanged();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        MemberViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}