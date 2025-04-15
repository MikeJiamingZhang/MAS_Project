package com.example.mas_solution_2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class chatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private List<Message> chatList;
    private String currentUserId;

    public chatAdapter(List<Message> chatList) {
        this.chatList = chatList;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
    }

    @Override
    public int getItemViewType(int position) {
        Message message = chatList.get(position);

        // If the current user is the sender of the message
        if (message.getSender().equals(FirebaseAuth.getInstance().getCurrentUser().getDisplayName())) {
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = chatList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public void changedData(List<Message> messageList) {
        this.chatList.clear();
        this.chatList.addAll(messageList);
        notifyDataSetChanged();
    }

    private static class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        SentMessageHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message_text);
            timeText = itemView.findViewById(R.id.message_time);
        }

        void bind(Message message) {
            messageText.setText(message.getMessage());

            // Format the stored timestamp into a readable String
            if (message.getTime() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
                timeText.setText(dateFormat.format(message.getTime().toDate()));
            } else {
                timeText.setText("");
            }
        }
    }

    private static class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText;

        ReceivedMessageHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message_text);
            timeText = itemView.findViewById(R.id.message_time);
            nameText = itemView.findViewById(R.id.sender_name);
        }

        void bind(Message message) {
            messageText.setText(message.getMessage());
            nameText.setText(message.getSender());

            // Format the stored timestamp into a readable String
            if (message.getTime() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
                timeText.setText(dateFormat.format(message.getTime().toDate()));
            } else {
                timeText.setText("");
            }
        }
    }
}