package com.example.vjezba4;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users"); // Referenca na "users" čvor

        Button registerButton = findViewById(R.id.registerButton);
        final EditText emailEditText = findViewById(R.id.emailEditText);
        final EditText passwordEditText = findViewById(R.id.passwordEditText);
        final EditText usernameEditText = findViewById(R.id.usernameEditText);  // Polje za username

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String username = usernameEditText.getText().toString().trim();  // Dohvati username

                if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Sva polja su obavezna.", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Dohvati trenutnog korisnika
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        String userId = user.getUid();

                                        // Spremi korisnikove podatke u Realtime Database
                                        User newUser = new User(email, username);  // Dodaj username u objekt
                                        databaseReference.child(userId).setValue(newUser)
                                                .addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful()) {
                                                        Toast.makeText(RegisterActivity.this, "Registracija uspješna!", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        Toast.makeText(RegisterActivity.this, "Greška pri spremanju podataka: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                } else {
                                    // Greška pri registraciji
                                    Toast.makeText(RegisterActivity.this, "Registracija nije uspjela: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    // Klasa za model korisnika (s emailom i usernameom)
    public static class User {
        public String email;
        public String username;

        public User() {
            // Prazan konstruktor potreban za Firebase
        }

        public User(String email, String username) {
            this.email = email;
            this.username = username;
        }
    }
}

