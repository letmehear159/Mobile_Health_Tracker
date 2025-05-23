package com.example.healthtrackerapp.view.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.healthtrackerapp.R;
import com.example.healthtrackerapp.viewmodel.EmailPasswordViewModel;

public class RegisterActivity extends AppCompatActivity {
    private EmailPasswordViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        viewModel = new ViewModelProvider(this).get(EmailPasswordViewModel.class);

        EditText email = findViewById(R.id.registerEmail);
        EditText password = findViewById(R.id.registerPassword);
        Button register = findViewById(R.id.btn_register);

        register.setOnClickListener(v -> {
            viewModel.register(email.getText().toString(), password.getText().toString());
        });

        viewModel.getUser().observe(this, user -> {
            Toast.makeText(this, "Registered: " + user.getEmail(), Toast.LENGTH_SHORT).show();
        });

        viewModel.getError().observe(this, err -> {
            Toast.makeText(this, "Error: " + err, Toast.LENGTH_SHORT).show();
        });
    }
}

