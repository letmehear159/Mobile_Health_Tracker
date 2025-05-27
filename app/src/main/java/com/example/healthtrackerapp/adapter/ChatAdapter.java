package com.example.healthtrackerapp.adapter;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

        LinearLayout.LayoutParams textParams = (LinearLayout.LayoutParams) holder.messageText.getLayoutParams();
        LinearLayout.LayoutParams iconParams = (LinearLayout.LayoutParams) holder.messageIcon.getLayoutParams();

        // Set alignment and background based on message type
        if (message.getType() == ChatMessage.TYPE_USER) {
            // User message (align right, show user icon)
            ((LinearLayout) holder.itemView).setGravity(Gravity.END);
            holder.messageText.setBackgroundResource(R.drawable.chat_message_background_user);
            holder.messageIcon.setImageResource(R.drawable.user_icon); // Replace with your user icon drawable
            iconParams.leftMargin = 0; // No margin on the left
            iconParams.rightMargin = 32; // Increased margin on the right
            holder.messageIcon.setVisibility(View.VISIBLE);

        } else {
            // Bot message (align left, show bot icon)
            ((LinearLayout) holder.itemView).setGravity(Gravity.START);
            holder.messageText.setBackgroundResource(R.drawable.chat_message_background_bot);
            holder.messageIcon.setImageResource(R.drawable.bot_icon); // Replace with your bot icon drawable
            iconParams.leftMargin = 32; // Increased margin on the left
            iconParams.rightMargin = 32; // No margin on the right
            holder.messageIcon.setVisibility(View.VISIBLE);
        }

        holder.messageText.setLayoutParams(textParams);
        holder.messageIcon.setLayoutParams(iconParams);

        // Optional: Hide icon for loading message if you don't have a loading icon
        if (message == loadingMessage) {
             holder.messageIcon.setVisibility(View.GONE);
        }
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
        ImageView messageIcon;

        MessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            messageIcon = itemView.findViewById(R.id.messageIcon);
        }
    }
}