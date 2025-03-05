package com.example.mas_solution_2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


// adapter for the voting section
// Horizontally scroll to the right
// Very similar to the chatAdapter

public class voteAdapter extends  RecyclerView.Adapter<voteAdapter.MyViewHolder>{

    private List<voteItem> voteList;

    public voteAdapter(List<voteItem> voteList){
        this.voteList = voteList;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView voteView;
        private TextView locationView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            voteView = itemView.findViewById(R.id.voteCount);
            locationView = itemView.findViewById(R.id.locationName);
        }
    }

    @NonNull
    @Override
    public voteAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.vote_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        voteItem item = voteList.get(position);
        holder.locationView.setText(item.getLocation());
        holder.voteView.setText("Votes: " + item.getVote());

    }

    @Override
    public int getItemCount() {
        return voteList.size();
    }

    public void updateData(List<voteItem> list){
        this.voteList.clear();
        this.voteList.addAll(list);
        notifyDataSetChanged();
    }
}
