package com.example.mas_solution_2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// ADapter for recycler list:
// Reference: https://www.youtube.com/watch?v=__OMnFR-wZU
public class chatAdapter extends  RecyclerView.Adapter<chatAdapter.MyViewHolder>{

    private List<Message> chatList;

    public chatAdapter(List<Message> chatList){
        this.chatList = chatList;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView textView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView2);
        }
    }

    @NonNull
    @Override
    public chatAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyc_view_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull chatAdapter.MyViewHolder holder, int position) {
        String message = chatList.get(position).getSender() + ": " + chatList.get(position).getMessage();
        holder.textView.setText(message);
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public void changedData(List<Message> messageList){
        this.chatList.clear();
        this.chatList.addAll(messageList);
        notifyDataSetChanged();
    }
}
