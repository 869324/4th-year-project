package com.dekut.dekutchat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dekut.dekutchat.R;
import com.dekut.dekutchat.adapters.ProfilePagerAdapter;
import com.dekut.dekutchat.fragments.Comments1;
import com.dekut.dekutchat.fragments.Comments2;
import com.dekut.dekutchat.fragments.Groups1;
import com.dekut.dekutchat.fragments.Home1;
import com.dekut.dekutchat.fragments.Likes;
import com.dekut.dekutchat.fragments.Likes1;
import com.dekut.dekutchat.fragments.Politics1;
import com.dekut.dekutchat.utils.Student;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ViewProfile extends AppCompatActivity {
    ImageView profilePic;
    TextView tvUsername, tvEmail;
    ProgressBar progressBar;
    Button editProfile;
    TabLayout tabLayout;
    ViewPager viewPager;

    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    String profileEmail;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    Query query;
    Student student;
    String key, previousUrl;
    ProfilePagerAdapter profilePagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_profile);

        profilePic = findViewById(R.id.profilePic);
        tvUsername = findViewById(R.id.tvUserName);
        tvEmail = findViewById(R.id.tvEmail);
        progressBar = findViewById(R.id.progressBar);
        editProfile = findViewById((R.id.editProfile));
        tabLayout = findViewById((R.id.tabLayout));
        viewPager = findViewById((R.id.viewPager));

        Bundle extras = getIntent().getExtras();
        profileEmail = extras.getString("profileEmail");

        fetchData();

        profilePagerAdapter = new ProfilePagerAdapter(getSupportFragmentManager());
        profilePagerAdapter.addFragment(new Home1(profileEmail), "Home");
        profilePagerAdapter.addFragment(new Politics1(profileEmail), "Politics");
        profilePagerAdapter.addFragment(new Likes(profileEmail), "Home Likes");
        profilePagerAdapter.addFragment(new Likes1(profileEmail), "Politics Likes");
        profilePagerAdapter.addFragment(new Comments1(profileEmail), "Home Comments");
        profilePagerAdapter.addFragment(new Comments2(profileEmail), "Politics Comments");
        profilePagerAdapter.addFragment(new Groups1(profileEmail), "Groups");
        viewPager.setAdapter(profilePagerAdapter);

        tabLayout.setupWithViewPager(viewPager);

        if(profileEmail.equals(email)){
            editProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), EditProfile.class);
                    startActivity(intent);
                }
            });
        }
        else {
            editProfile.setVisibility(View.INVISIBLE);
        }

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewProfile.this, ViewImage.class);
                intent.putExtra("url", student.getProfileUrl());
                startActivity(intent);
            }
        });


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void fetchData(){
        query = firebaseDatabase.getReference().child("students").orderByChild("email").equalTo(profileEmail);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    student = snap.getValue(Student.class);
                    progressBar.setVisibility(View.GONE);
                    Glide.with(getApplicationContext())
                            .load(student.getProfileUrl())
                            .into(profilePic);
                    tvUsername.setText(student.getUserName());
                    tvEmail.setText(profileEmail);
                    previousUrl = student.getProfileUrl();
                    key = snap.getKey();
                    break;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}