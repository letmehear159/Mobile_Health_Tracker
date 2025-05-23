package com.example.healthtrackerapp.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.healthtrackerapp.R;
import com.example.healthtrackerapp.viewmodel.EmailPasswordViewModel;
import com.example.healthtrackerapp.viewmodel.GoogleAuthViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends AppCompatActivity {

    private EmailPasswordViewModel viewModel;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        viewModel = new ViewModelProvider(this).get(EmailPasswordViewModel.class);

        EditText email = findViewById(R.id.loginEmail);
        EditText password = findViewById(R.id.loginPassword);
        Button login = findViewById(R.id.btn_login);
        login.setOnClickListener(v -> {
            viewModel.login(email.getText().toString(), password.getText().toString());
        });

        viewModel.getUser().observe(this, user -> {
            Toast.makeText(this, "Logged in: " + user.getEmail(), Toast.LENGTH_SHORT).show();
        });

        viewModel.getError().observe(this, err -> {
            Toast.makeText(this, "Error: " + err, Toast.LENGTH_SHORT).show();
        });
//        // Đăng ký launcher để nhận kết quả đăng nhập
//        googleSignInLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    Intent data = result.getData();
//                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//                    viewModel.signInWithGoogle(task);
//                }
//        );

//        btnSignIn.setOnClickListener(v -> {
//            GoogleSignInClient client = viewModel.getGoogleSignInClient(this);
//            Intent signInIntent = client.getSignInIntent();
//            googleSignInLauncher.launch(signInIntent);
//        });

//        viewModel.getUser().observe(this, firebaseUser -> {
//            if (firebaseUser != null) {
//                Toast.makeText(this, "Đăng nhập thành công: " + firebaseUser.getEmail(), Toast.LENGTH_SHORT).show();
//                // TODO: Chuyển sang màn hình chính
//            }
//        });
    }
}

