package com.example.agroplanner;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Register extends AppCompatActivity {

    private FirebaseAuth mAuth;
    DatabaseReference reference;

    private EditText fullname, username, email, password;
    private Button btn_register;
    private CheckBox checkBox;
    private MaterialAlertDialogBuilder materialAlertDialogBuilder;
    Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        fullname = findViewById(R.id.fullname);
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        btn_register = findViewById(R.id.btn_register);

        //CheckBox
//                getSupportActionBar().hide();
        checkBox = findViewById(R.id.check_id);
        btn_register.setEnabled(false);
        materialAlertDialogBuilder = new MaterialAlertDialogBuilder(mContext);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    materialAlertDialogBuilder.setTitle("Terms and Conditions");
                    materialAlertDialogBuilder.setMessage(Html.fromHtml("click here,<a href=\"https://drive.google.com/file/d/1W6V1P_HWZghNvGfdKlUVLvgG-2iMsQDM/view\">link</a>.</p>"));
                    materialAlertDialogBuilder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            btn_register.setEnabled(true);
                            dialogInterface.dismiss();
                        }
                    });
                    materialAlertDialogBuilder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            dialogInterface.dismiss();
                            checkBox.setChecked(false);
                        }
                    });
                    AlertDialog Alert1 = materialAlertDialogBuilder.create();
                    Alert1.show();
                    ((TextView)Alert1.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());

                }else{
                    btn_register.setEnabled(false);
                }
            }});

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String reg_fname = fullname.getText().toString().trim();
                String reg_uname = username.getText().toString().trim();
                String reg_email = email.getText().toString().trim();
                String reg_password = password.getText().toString().trim();


                if (reg_fname.isEmpty()) {
                    fullname.setError("Fullname is required");
                    fullname.requestFocus();
                    return;

                }

                if (reg_uname.isEmpty()) {
                    username.setError("Username is required");
                    username.requestFocus();
                    return;

                }

                if (reg_email.isEmpty()) {
                    email.setError("Email is required");
                    email.requestFocus();
                    return;

                }

                if (!Patterns.EMAIL_ADDRESS.matcher(reg_email).matches()) {
                    email.setError("Email is not valid");
                    email.requestFocus();
                    return;

                }

                if (reg_password.isEmpty()) {
                    password.setError("Password is required");
                    password.requestFocus();
                    return;

                }

                if (reg_password.length()<6) {
                    password.setError("Password should be at least 6 characters");
                    password.requestFocus();
                    return;

                }

                registerUser(reg_email, reg_password, reg_fname, reg_uname);
            }
        });
    }

    private void registerUser(String email, String password, String fullname, String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            //Registration is successful
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            String userid = firebaseUser.getUid();

                            reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userid);

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("id", userid);
                            hashMap.put("username", username.toLowerCase());
                            hashMap.put("fullname", fullname);
                            hashMap.put("email", email);
                            hashMap.put("imageurl", "https://firebasestorage.googleapis.com/v0/b/agroplanner-4efff.appspot.com/o/default-profile-icon-24.jpg?alt=media&token=e4a7606d-a23d-414a-9d91-1ae1710c8221");

                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(Register.this, Dashboard.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        Toast.makeText(Register.this, "Registration success", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(Register.this, "Registration failed. Try again", Toast.LENGTH_LONG).show();
                        }

                    }
                });
    }

    public void ClickReturn (View view) {
        startActivity(new Intent(Register.this, Login.class));

    }
}