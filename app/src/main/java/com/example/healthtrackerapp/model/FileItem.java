package com.example.healthtrackerapp.model;

import com.google.firebase.Timestamp;

public class FileItem {
    private String url;
    private String name;
    private Timestamp timestamp;

    public FileItem() {} // Cần cho Firestore

    public FileItem(String url, String name, Timestamp timestamp) {
        this.url = url;
        this.name = name;
        this.timestamp = timestamp;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public long getTimestampMillis() {
        return timestamp != null ? timestamp.toDate().getTime() : 0;
    }
}
