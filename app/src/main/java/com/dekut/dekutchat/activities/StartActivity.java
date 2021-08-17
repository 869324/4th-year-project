package com.dekut.dekutchat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dekut.dekutchat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class StartActivity extends AppCompatActivity {

    ProgressBar progressBar;

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setProgress(0);

        if(haveNetworkConnection()) {
            firebaseAuth = FirebaseAuth.getInstance();
            user = firebaseAuth.getCurrentUser();
            progressBar.setProgress(40);
            if(user != null){
                email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                progressBar.setProgress(50);
                if (user.isEmailVerified()) {
                    Query query = FirebaseDatabase.getInstance().getReference().child("students").orderByChild("email").equalTo(email);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            progressBar.setProgress(80);
                            if (snapshot.exists()) {
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                progressBar.setProgress(90);

                            } else {
                                Intent intent = new Intent(getApplicationContext(), CreateProfile.class);
                                startActivity(intent);
                                progressBar.setProgress(90);
                            }
                            finish();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                } else {
                    progressBar.setProgress(100);
                    Intent intent = new Intent(getApplicationContext(), Login.class);
                    startActivity(intent);
                    finish();
                }
            }else{
                progressBar.setProgress(100);
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }

        }
        else {
            Toast.makeText(getApplicationContext(), "No Network Connection!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        progressBar.setProgress(30);
        return haveConnectedWifi || haveConnectedMobile;
    }
}