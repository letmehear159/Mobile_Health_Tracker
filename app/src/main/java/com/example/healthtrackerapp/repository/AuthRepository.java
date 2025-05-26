package com.example.healthtrackerapp.repository;

import android.content.Context;

import com.example.healthtrackerapp.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class AuthRepository {

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    public GoogleSignInClient getGoogleSignInClient(Context context) {
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("235464999384-87eaups8so0ssvjo43u61b11ofn60et9.apps.googleusercontent.com")
                .requestEmail()
                .build();

        return GoogleSignIn.getClient(context, options);
    }

    public void signInWithGoogle(Task<GoogleSignInAccount> task, OnCompleteListener<FirebaseUser> callback) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

            firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener(authTask -> {
                        if (authTask.isSuccessful()) {
                            callback.onComplete(firebaseAuth.getCurrentUser());
                        } else {
                            callback.onComplete(null);
                        }
                    });
        } catch (ApiException e) {
            callback.onComplete(null);
        }
    }

    public interface OnCompleteListener<T> {
        void onComplete(T result);
    }
}
