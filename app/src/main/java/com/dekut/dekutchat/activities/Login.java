package com.dekut.dekutchat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.dekut.dekutchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private TextView tvForgotPassword, tvSignUp;
    private Button btnLogin;
    ProgressDialog progressDialog;
    ImageButton showPassword;

    boolean hide = true;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSignUp = findViewById(R.id.tvSignUp);
        showPassword = findViewById(R.id.showPassword);

        firebaseAuth = FirebaseAuth.getInstance();

        showPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hide){
                    etPassword.setTransformationMethod(null);
                    hide = false;
                }
                else {
                    etPassword.setTransformationMethod(new PasswordTransformationMethod());
                    hide = true;
                }

            }
        });

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, SignUp.class);
                startActivity(intent);
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();

                if(email.isEmpty()){
                    etEmail.setError("Enter your Student Email");
                    etEmail.requestFocus();
                    return;
                }
                else {
                    firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(Login.this, "Check your email for the link to reset your password", Toast.LENGTH_LONG).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Login.this, "Reset password email not sent: " +e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString();

                if(email.isEmpty()){
                    etEmail.setError("Student email is required");
                    etEmail.requestFocus();
                    return;
                }

                if(password.isEmpty()){
                    etPassword.setError("Enter Password");
                    etPassword.requestFocus();
                    return;
                }

                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    etEmail.setError("Please enter a valid email");
                    etEmail.requestFocus();
                    return;
                }

                else {
                    progressDialog = new ProgressDialog(Login.this);
                    progressDialog.show();
                    progressDialog.setContentView(R.layout.progress_dialog);
                    progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    progressDialog.setCancelable(false);

                    firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                if(firebaseAuth.getCurrentUser().isEmailVerified()){
                                    Query query = FirebaseDatabase.getInstance().getReference().child("students").orderByChild("email").equalTo(email);
                                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists()){
                                                Intent intent = new Intent(Login.this, MainActivity.class);
                                                intent.putExtra("email", email);
                                                startActivity(intent);
                                                finish();
                                            }else {
                                                Intent intent = new Intent(Login.this, CreateProfile.class);
                                                intent.putExtra("email", email);
                                                intent.putExtra("operation", "create");
                                                startActivity(intent);
                                                finish();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                                else {
                                    progressDialog.dismiss();
                                    AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                                    builder.setMessage("You have not yet verified your Email\nWould you like us to resend the verification Email?");
                                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(getApplicationContext(), "Verification Email has been sent", Toast.LENGTH_LONG).show();
                                                    }
                                                    else {
                                                        Toast.makeText(getApplicationContext(), "Sending Verification Email Failed", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                        }
                                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    });
                                    AlertDialog alertDialog = builder.create();
                                    alertDialog.show();
                                }
                            }
                            else {
                                progressDialog.dismiss();
                                Toast.makeText(Login.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
