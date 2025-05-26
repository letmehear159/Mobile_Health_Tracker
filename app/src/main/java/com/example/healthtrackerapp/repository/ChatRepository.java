package com.example.healthtrackerapp.repository;

import com.example.healthtrackerapp.api.OpenAIService;
import com.example.healthtrackerapp.model.ChatRequest;
import com.example.healthtrackerapp.model.ChatResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatRepository {
    private static final String BASE_URL = "https://api.openai.com/";
    private static final String API_KEY = "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\n"; // Replace with your actual API key
    private final OpenAIService openAIService;

    public ChatRepository() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        openAIService = retrofit.create(OpenAIService.class);
    }

    public interface ChatCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    public void sendMessage(String userMessage, List<ChatRequest.Message> conversationHistory, ChatCallback callback) {
        List<ChatRequest.Message> messages = new ArrayList<>(conversationHistory);
        messages.add(new ChatRequest.Message("user", userMessage));

        ChatRequest request = new ChatRequest(
            "gpt-3.5-turbo",
            messages,
            0.7
        );

        openAIService.createChatCompletion("Bearer " + API_KEY, request)
                .enqueue(new Callback<ChatResponse>() {
                    @Override
                    public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String botResponse = response.body().getChoices().get(0).getMessage().getContent();
                            callback.onSuccess(botResponse);
                        } else {
                            callback.onError("Error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ChatResponse> call, Throwable t) {
                        callback.onError("Network error: " + t.getMessage());
                    }
                });
    }
} 