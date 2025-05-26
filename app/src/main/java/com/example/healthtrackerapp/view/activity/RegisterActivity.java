package com.example.healthtrackerapp.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
        viewModel.getUser().observe(this, user -> {
            Toast.makeText(this, "Registered: " + user.getEmail(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        viewModel.getError().observe(this, err -> {
            Toast.makeText(this, "Error: " + err, Toast.LENGTH_SHORT).show();
        });

        EditText email = findViewById(R.id.etEmail);
        EditText fullName = findViewById(R.id.etFullName);
        EditText password = findViewById(R.id.etPassword);
        Button register = findViewById(R.id.btnSignUp);
        TextView tvLogin = findViewById(R.id.tvLogin);
        register.setOnClickListener(v -> {
            viewModel.register(email.getText().toString(), password.getText().toString(), fullName.getText().toString());

        });

        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });


    }
}

