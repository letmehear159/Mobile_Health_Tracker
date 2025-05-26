package com.example.healthtrackerapp.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.healthtrackerapp.repository.AuthRepository;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;

public class GoogleAuthViewModel extends ViewModel {
    private final AuthRepository repository = new AuthRepository();
    private final MutableLiveData<FirebaseUser> user = new MutableLiveData<>();

    public LiveData<FirebaseUser> getUser() {
        return user;
    }

    public GoogleSignInClient getGoogleSignInClient(Context context) {
        return repository.getGoogleSignInClient(context);
    }

    public void signInWithGoogle(Task<GoogleSignInAccount> task) {
        repository.signInWithGoogle(task, user::postValue);
    }
}
