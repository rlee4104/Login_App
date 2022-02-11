package com.example.gettouni.ui.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gettouni.R;
import com.example.gettouni.data.model.LoggedInUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {
    EditText firstName, lastName, userEmail, userPassword;
    Button btnRegister;
    TextView textLogin;
    FirebaseAuth mFirebaseAuth;
    FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        firstName = findViewById(R.id.userFirstName);
        lastName = findViewById(R.id.userLastName);
        userEmail = findViewById(R.id.username);
        userPassword = findViewById(R.id.password);
        btnRegister = findViewById(R.id.register);
        textLogin = findViewById(R.id.Login);
        mFirebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        // Register button function
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = userEmail.getText().toString();
                final String fName = firstName.getText().toString();
                final String lName = lastName.getText().toString();
                final String pwd = userPassword.getText().toString();

                if (email.isEmpty()) {
                    userEmail.setError("Please enter your email address.");
                    userEmail.requestFocus();
                } else if (fName.isEmpty()) {
                    firstName.setError("Please enter your first name.");
                    firstName.requestFocus();
                } else if (lName.isEmpty()) {
                    lastName.setError("Please enter your last name.");
                    lastName.requestFocus();
                } else if (pwd.isEmpty()) {
                    userPassword.setError("Please enter a password.");
                    userPassword.requestFocus();
                } else if (!(email.isEmpty() && pwd.isEmpty())) {
                    mFirebaseAuth.createUserWithEmailAndPassword(email, pwd)
                            .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(SignupActivity.this, "Registration Error. " + task.getException(), Toast.LENGTH_LONG).show();
                                    } else {
                                        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                            Toast.makeText(SignupActivity.this, "Invalid email address.", Toast.LENGTH_LONG).show();
                                        }
                                        else {
                                            // Store user information in the database
                                            LoggedInUser userDetail = new LoggedInUser(fName, lName, email);
                                            String uid = task.getResult().getUser().getUid();
                                            firebaseDatabase.getReference(uid).setValue(userDetail);

                                            // Send verification link to the user's email
                                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(intent);
                                                        Toast.makeText(SignupActivity.this, "A verification link has been sent to your email.", Toast.LENGTH_LONG).show();
                                                    } else {
                                                        Toast.makeText(SignupActivity.this, "A problem has occurred when sending the verification link. " + task.getException(), Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                        }
                                    }

                                }
                            });
                } else {
                    Toast.makeText(SignupActivity.this, "Registration Error. Please try again.", Toast.LENGTH_LONG).show();
                }
            }
        });

        // When login text is clicked, redirect to login up page
        textLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SignupActivity.this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        });
    }
}