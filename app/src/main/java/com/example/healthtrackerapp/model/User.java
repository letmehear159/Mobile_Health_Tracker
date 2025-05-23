package com.example.healthtrackerapp.model;

public class User {
    public String uid;
    public String email;

    public User() {} // Needed for Firebase

    public User(String uid, String email) {
        this.uid = uid;
        this.email = email;
    }
}
