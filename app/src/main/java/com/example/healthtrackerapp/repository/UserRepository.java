package com.example.healthtrackerapp.repository;

import com.example.healthtrackerapp.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.function.Consumer;

public class UserRepository {
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private DatabaseReference db = FirebaseDatabase.getInstance().getReference("users");

    public void register(String email, String password, Consumer<FirebaseUser> onSuccess, Consumer<Exception> onFailure) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    db.child(user.getUid()).setValue(new User(user.getUid(), user.getEmail()));
                    onSuccess.accept(user);
                })
                .addOnFailureListener(onFailure::accept);
    }

    public void login(String email, String password, Consumer<FirebaseUser> onSuccess, Consumer<Exception> onFailure) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> onSuccess.accept(result.getUser()))
                .addOnFailureListener(onFailure::accept);
    }
}
