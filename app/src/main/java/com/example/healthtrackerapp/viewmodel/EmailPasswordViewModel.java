package com.example.healthtrackerapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.healthtrackerapp.repository.UserRepository;
import com.google.firebase.auth.FirebaseUser;

public class EmailPasswordViewModel extends ViewModel {

    private final UserRepository repo = new UserRepository();
    private final MutableLiveData<FirebaseUser> user = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<FirebaseUser> getUser() {
        return user;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void register(String email, String password) {
        repo.register(email, password, user::postValue, e -> error.postValue(e.getMessage()));
    }

    public void login(String email, String password) {
        repo.login(email, password, user::postValue, e -> error.postValue(e.getMessage()));
    }
}
