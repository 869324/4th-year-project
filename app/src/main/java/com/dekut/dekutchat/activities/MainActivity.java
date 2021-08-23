package com.dekut.dekutchat.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dekut.dekutchat.R;
import com.dekut.dekutchat.fragments.Chats;
import com.dekut.dekutchat.fragments.Groups;
import com.dekut.dekutchat.fragments.Home;
import com.dekut.dekutchat.fragments.Politics;
import com.dekut.dekutchat.utils.Student;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawer;
    NavigationView navView;
    BottomNavigationView navBar;
    View headerView;
    ImageView headerProfilePic;
    TextView tvHeaderUsername, tvHeaderEmail;

    String email;
    Student student;
    Fragment home, chats, groups, politics;
    FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        home = new Home(email);
        chats = new Chats(email);
        groups = new Groups(email);
        politics = new Politics(email);

        navBar = findViewById(R.id.bottomNavBar);
        navBar.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        if (savedInstanceState == null) {
            fragmentManager.beginTransaction().replace(R.id.nav_host_fragment, home).commit();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        drawer = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        headerView = navView.getHeaderView(0);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();

        headerProfilePic = headerView.findViewById(R.id.headerProfilePic);
        tvHeaderUsername = headerView.findViewById(R.id.tvHeaderUsername);
        tvHeaderEmail = headerView.findViewById(R.id.tvHeaderEmail);

        Query query = FirebaseDatabase.getInstance().getReference().child("students").orderByChild("email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    student = snap.getValue(Student.class);
                    tvHeaderUsername.setText(student.getUserName());
                    tvHeaderEmail.setText(student.getEmail());
                    Glide.with(getApplicationContext())
                            .load(student.getProfileUrl())
                            .into(headerProfilePic);
                    break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == R.id.nav_profile) {
                    Intent intent = new Intent(MainActivity.this, ViewProfile.class);
                    intent.putExtra("profileEmail", email);
                    startActivity(intent);
                } else if (id == R.id.nav_logout) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(MainActivity.this, Login.class));
                    finish();
                } else if (id == R.id.nav_exit) {
                    finishAffinity();
                    System.exit(0);
                }
                drawer.closeDrawer(GravityCompat.START);
                menuItem.setChecked(false);
                return true;
            }
        });
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @SuppressLint("NonConstantResourceId")
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment fragment = null;
                    int itemId = item.getItemId();
                    String tag = null;

                    switch (itemId) {
                        case R.id.home_menu:
                            Fragment fragment1 = fragmentManager.findFragmentByTag("home");
                            if (fragment1 != null && fragment1.isVisible()){
                                Home home1 = (Home) fragment1;
                                home1.scrollUp();
                            }

                            else {
                                fragment = home;
                                tag = "home";
                            }
                            break;

                        case R.id.chats_menu:
                            fragment = chats;
                            tag = "chats";
                            break;

                        case R.id.groups_menu:
                            fragment = groups;
                            tag = "groups";
                            break;

                        case R.id.politics_menu:
                            Fragment fragment2 = fragmentManager.findFragmentByTag("politics");
                            if (fragment2 != null && fragment2.isVisible()){
                                Politics politics1 = (Politics) fragment2;
                                politics1.scrollUp();
                            }

                            else {
                                fragment = politics;
                                tag = "politics";
                            }
                    }

                    if (fragment != null) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment, tag).commit();
                    }
                    return true;
                }
            };

}

