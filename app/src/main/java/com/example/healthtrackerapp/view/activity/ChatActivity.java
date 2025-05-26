package com.example.healthtrackerapp.view.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthtrackerapp.R;
import com.example.healthtrackerapp.adapter.ChatAdapter;
import com.example.healthtrackerapp.model.ChatMessage;
import com.example.healthtrackerapp.model.ChatRequest;
import com.example.healthtrackerapp.repository.ChatRepository;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private ChatAdapter chatAdapter;
    private ChatRepository chatRepository;
    private List<ChatRequest.Message> conversationHistory;
    private Toolbar toolbarChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize views
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        toolbarChat = findViewById(R.id.toolbarChat);

        // Setup Toolbar
        setSupportActionBar(toolbarChat);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Health Assistant");
        }

        // Setup RecyclerView
        chatAdapter = new ChatAdapter();
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Initialize repository and conversation history
        chatRepository = new ChatRepository();
        conversationHistory = new ArrayList<>();

        // Add welcome message
        chatAdapter.addMessage(new ChatMessage(
            "Hello! I'm your health assistant. How can I help you today?",
            ChatMessage.TYPE_BOT
        ));

        // Setup send button click listener
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String message = messageInput.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            return;
        }

        // Clear input
        messageInput.setText("");

        // Add user message to chat
        chatAdapter.addMessage(new ChatMessage(message, ChatMessage.TYPE_USER));

        // Add to conversation history
        conversationHistory.add(new ChatRequest.Message("user", message));

        // Show loading message
        chatAdapter.showLoadingMessage();

        // Send to API
        chatRepository.sendMessage(message, conversationHistory, new ChatRepository.ChatCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    // Remove loading message
                    chatAdapter.removeLoadingMessage();

                    // Add bot response
                    chatAdapter.addMessage(new ChatMessage(response, ChatMessage.TYPE_BOT));
                    conversationHistory.add(new ChatRequest.Message("assistant", response));
                    
                    // Scroll to bottom
                    chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    // Remove loading message
                    chatAdapter.removeLoadingMessage();

                    // Show error message
                    Toast.makeText(ChatActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Handle back button click
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 