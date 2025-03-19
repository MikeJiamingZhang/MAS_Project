package com.example.mas_solution_2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


// adapter for the voting section
// Horizontally scroll to the right
// Very similar to the chatAdapter
// Vote adapter reference: https://www.youtube.com/watch?v=vBxNDtyE_Co&t=0s
public class voteAdapter extends  RecyclerView.Adapter<voteAdapter.MyViewHolder>{

    private List<voteItem> voteList;
    private voteListener listener;

    public voteAdapter(List<voteItem> voteList, voteListener listener){
        this.voteList = voteList;
        this.listener = listener;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView voteView;
        private TextView locationView;
        private Button vote;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            voteView = itemView.findViewById(R.id.voteCount);
            locationView = itemView.findViewById(R.id.location);
            vote = itemView.findViewById(R.id.vote);
            vote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(voteList.get(getAdapterPosition()).getLocation());
                }
            });
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

    public interface voteListener{
        void onClick(String locName);
    }
}
