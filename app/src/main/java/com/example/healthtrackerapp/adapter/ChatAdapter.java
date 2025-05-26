package com.example.healthtrackerapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthtrackerapp.R;
import com.example.healthtrackerapp.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
    private List<ChatMessage> messages = new ArrayList<>();
    private ChatMessage loadingMessage = null;

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.messageText.setText(message.getMessage());
        
        // Set alignment based on message type
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.messageText.getLayoutParams();
        if (message.getType() == ChatMessage.TYPE_USER) {
            params.gravity = android.view.Gravity.END;
            holder.messageText.setBackgroundResource(R.drawable.chat_message_background_user);
        } else {
            params.gravity = android.view.Gravity.START;
            holder.messageText.setBackgroundResource(R.drawable.chat_message_background_bot);
        }
        holder.messageText.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void showLoadingMessage() {
        if (loadingMessage == null) {
            loadingMessage = new ChatMessage("Thinking...", ChatMessage.TYPE_BOT);
            addMessage(loadingMessage);
        }
    }

    public void removeLoadingMessage() {
        if (loadingMessage != null) {
            int position = messages.indexOf(loadingMessage);
            if (position != -1) {
                messages.remove(position);
                notifyItemRemoved(position);
            }
            loadingMessage = null;
        }
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        MessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
        }
    }
}