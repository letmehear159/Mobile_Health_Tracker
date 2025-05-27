package com.example.healthtrackerapp.model;

import java.util.List;

public class ChatResponse {
    private String id;
    private String object;
    private long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;

    public List<Choice> getChoices() {
        return choices;
    }

    public static class Choice {
        private Message message;
        private String finish_reason;
        private int index;

        public Message getMessage() {
            return message;
        }
    }

    public static class Message {
        private String role;
        private String content;

        public String getContent() {
            return content;
        }
    }

    public static class Usage {
        private int prompt_tokens;
        private int completion_tokens;
        private int total_tokens;
    }
}