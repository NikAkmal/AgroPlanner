package com.example.agroplanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Dashboard extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private CardView btn_plants, btn_feed, btn_report, btn_profile;
    private TextView profile_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        String email = user.getEmail();

        profile_name = findViewById(R.id.profile_name);
        profile_name.setText(email);

        btn_plants = findViewById(R.id.btn_plants);
        btn_feed = findViewById(R.id.btn_feed);
        btn_report = findViewById(R.id.btn_report);
        btn_profile = findViewById(R.id.btn_profile);

        btn_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Dashboard.this, Profile.class));

            }
        });
    }

    public void Plant (View view){
        Intent intent = new  Intent(getApplicationContext(), Plants.class);
        startActivity(intent);
    }
}