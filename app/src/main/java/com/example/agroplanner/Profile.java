package com.example.agroplanner;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Profile extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference reference;
    private String userID;

    private Button btn_logout, btn_editProfile, terms;
    private TextView P_Name, P_username, P_email;
    private ImageView image_profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        btn_logout = findViewById(R.id.btn_logout);
        terms = findViewById(R.id.terms);
        btn_editProfile = findViewById(R.id.editprofile);
        P_Name = findViewById(R.id.fullname_profile);
        P_username = findViewById(R.id.username_profile);
        P_email = findViewById(R.id.email_profile);
        image_profile= findViewById(R.id.image_profileED);

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(Profile.this, Login.class));

            }
        });

        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://drive.google.com/file/d/1W6V1P_HWZghNvGfdKlUVLvgG-2iMsQDM/view";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        btn_editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Profile.this, EditProfile.class));
            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Users");
        userID = user.getUid();

        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userProfile = snapshot.getValue(User.class);
                if (userProfile !=null){
                    String fullname = userProfile.fullname;
                    String username = userProfile.username;
                    String email = userProfile.email;
                    String imageurl = userProfile.imageurl;

                    image_profile.setImageURI(Uri.parse(imageurl));
                    Glide.with(Profile.this).load(userProfile.getImageurl()).into(image_profile);
                    P_Name.setText(fullname);
                    P_username.setText(username);
                    P_email.setText(email);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profile.this, "Something wrong happen", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void ClickReturn (View view) {
//        startActivity(new Intent(Profile.this, Dashboard.class));
        onBackPressed();
    }
}