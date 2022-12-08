package com.example.agroplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private EditText email;
    private Button btn_reset;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        email = findViewById(R.id.email);
        btn_reset = findViewById(R.id.btn_reset);

        auth = FirebaseAuth.getInstance();

        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPassword();
            }
        });
    }

    private void resetPassword() {

        String res_email = email.getText().toString().trim();

        if (res_email.isEmpty()) {
            email.setError("Email is required");
            email.requestFocus();
            return;

        }

        if (!Patterns.EMAIL_ADDRESS.matcher(res_email).matches()) {
            email.setError("Email is not valid");
            email.requestFocus();
            return;

        }

        auth.sendPasswordResetEmail(res_email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    Toast.makeText(ForgotPassword.this, "Please check your email to reset your password", Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(ForgotPassword.this, "Try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void ClickReturn (View view) {
        startActivity(new Intent(ForgotPassword.this, Login.class));

    }
}