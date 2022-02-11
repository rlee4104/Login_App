package com.example.gettouni.ui.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gettouni.R;
import com.example.gettouni.data.model.LoggedInUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    EditText usernameID, passwordID;
    Button btnLogin;
    TextView textRegister;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        usernameID = findViewById(R.id.username);
        passwordID = findViewById(R.id.password);
        btnLogin = findViewById(R.id.login);
        textRegister = findViewById(R.id.textViewRegister);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        // User authentication via Firebase
        FirebaseAuth.AuthStateListener mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser theFirebaseUser = firebaseAuth.getCurrentUser();
                if (theFirebaseUser != null) {
                    goToLoggedInUserView(theFirebaseUser);
                } else {
                    Toast.makeText(LoginActivity.this, "Please Login.", Toast.LENGTH_SHORT).show();
                }
            }
        };

        // The function of the login button
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = usernameID.getText().toString();
                String pwd = passwordID.getText().toString();

                if (email.isEmpty()) {
                    usernameID.setError("Please enter your email address.");
                    usernameID.requestFocus();
                }
                else if (pwd.isEmpty()) {
                    passwordID.setError("Please enter your password.");
                    passwordID.requestFocus();
                }
                else if (email.isEmpty() && pwd.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter your email and password.", Toast.LENGTH_SHORT).show();
                    usernameID.requestFocus();
                    passwordID.requestFocus();
                }
                else if (!(email.isEmpty() && pwd.isEmpty())) {
                    firebaseAuth.signInWithEmailAndPassword(email, pwd)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                        @Override

                                        // Process to check whether user is verified or not and the next steps in the corresponding process
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (!task.isSuccessful()) {
                                                Toast.makeText(LoginActivity.this, "Login error. Please make sure your credentials are correct.", Toast.LENGTH_LONG).show();
                                            }
                                            else {
                                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                if (!user.isEmailVerified()) {
                                                    Toast.makeText(LoginActivity.this, "Please verify your account with the link sent to your email.", Toast.LENGTH_LONG).show();
                                                }
                                                else {
                                                    goToLoggedInUserView(task.getResult().getUser());
                                                }
                                            }
                                        }
                                    });
                }
                else {
                    Toast.makeText(LoginActivity.this, "Login Error. Please try again.", Toast.LENGTH_LONG).show();
                }
            }
        });

        // When register text is clicked, redirect user to sign up page
        textRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intSignUp = new Intent(LoginActivity.this, SignupActivity.class);
                intSignUp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intSignUp);
            }
        });
    }

    // Function to go to the main page of the application
    private void goToLoggedInUserView(FirebaseUser theFirebaseUser) {
        firebaseDatabase.getReference().child(theFirebaseUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        LoggedInUser userDetail = snapshot.getValue(LoggedInUser.class);
                        String name = userDetail.getFirstName() + " " + userDetail.getLastName();
                        Intent i = new Intent(getApplicationContext(), LoggedInUserView.class);
                        Toast.makeText(LoginActivity.this, "Login Successful.", Toast.LENGTH_LONG).show();
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        i.putExtra("name", name);
                        startActivity(i);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }
}