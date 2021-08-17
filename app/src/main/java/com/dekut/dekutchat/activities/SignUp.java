package com.dekut.dekutchat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.dekut.dekutchat.R;
import com.dekut.dekutchat.utils.Student;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class SignUp extends AppCompatActivity{
    EditText etEmail, etPassword;
    Button btnSignUp;
    TextView back, tvError;
    ProgressDialog progressDialog;
    ImageButton showPassword;

    FirebaseAuth firebaseAuth;
    boolean hidePassword = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        back = findViewById(R.id.back);
        tvError = findViewById(R.id.tvError);
        showPassword = findViewById(R.id.showPassword);

        firebaseAuth = FirebaseAuth.getInstance();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
             public void onClick(View v) {
                startActivity(new Intent(SignUp.this, Login.class));
                finish();
            }
        });

        showPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hidePassword){
                    etPassword.setTransformationMethod(null);
                    hidePassword = false;
                }
                else {
                    etPassword.setTransformationMethod(new PasswordTransformationMethod());
                    hidePassword = true;
                }

            }
        });


        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString();
                Pattern pattern = Pattern.compile("\\w+.\\w+\\d+@students.dkut.ac.ke");

                if(email.isEmpty()){
                    etEmail.setError("Email Required!");
                    etEmail.requestFocus();
                    return;
                }

                if(password.isEmpty()){
                    etPassword.setError("Password Required!");
                    etPassword.requestFocus();
                    return;
                }

                if(password.length() < 6){
                    etPassword.setError("Password should not be less than 6 characters");
                    etPassword.requestFocus();
                    return;
                }

                if(!pattern.matcher(email).matches()){
                    etEmail.setError("Enter valid Dekut student email");
                    etEmail.requestFocus();
                    return;
                }

                else{
                    progressDialog = new ProgressDialog(SignUp.this);
                    progressDialog.show();
                    progressDialog.setContentView(R.layout.progress_dialog);
                    progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    progressDialog.setCancelable(false);

                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        progressDialog.dismiss();
                                        if(task.isSuccessful()){
                                            AlertDialog.Builder builder = new AlertDialog.Builder(SignUp.this);
                                            builder.setMessage("Registered successfully, Please check your Email for verification\nbefore you login");
                                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Intent intent = new Intent(SignUp.this, Login.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            });
                                            AlertDialog alertDialog = builder.create();
                                            alertDialog.show();
                                        }
                                        else {
                                            Toast.makeText(SignUp.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                            else {
                                progressDialog.dismiss();
                                Toast.makeText(SignUp.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SignUp.this, "Failed: " +e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }
}