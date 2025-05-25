package com.example.healthtrackerapp.model;

public class User {
    public String uid;
    public String email;

    public String fullName;


    public User() {
    } // Needed for Firebase

    public User(String uid, String email, String fullName) {

        this.uid = uid;
        this.email = email;
        this.fullName = fullName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
