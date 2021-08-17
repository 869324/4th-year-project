package com.dekut.dekutchat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dekut.dekutchat.R;
import com.dekut.dekutchat.adapters.GroupMembersAdapter;
import com.dekut.dekutchat.utils.Group;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewGroup extends AppCompatActivity {

    TextView tvName, tvType, tvDesc1, tvDesc2, tvMembers;
    Button btnOperation, btnEditGroup;
    ImageButton btnEditPic, btnEditType, btnEditName, btnEditDesc;
    ImageView imageView;
    RecyclerView membersRecyclerView;

    String groupId;
    LinearLayoutManager linearLayoutManager;
    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_group);

        tvName = findViewById(R.id.tvName2);
        tvType = findViewById(R.id.tvType2);
        tvDesc1 = findViewById(R.id.tvDescription1);
        tvDesc2 = findViewById(R.id.tvDescription2);
        btnOperation = findViewById(R.id.btnOperation);
        btnEditPic= findViewById(R.id.btnEditPic);
        btnEditType = findViewById(R.id.btnEditType);
        btnEditName = findViewById(R.id.btnEditName);
        btnEditDesc = findViewById(R.id.btnEditDesc);
        imageView = findViewById(R.id.imageView);
        membersRecyclerView = findViewById(R.id.membersRecyclerView);
        tvMembers = findViewById(R.id.tvMembers2);
        btnEditGroup = findViewById(R.id.btnEditGroup);

        Bundle extras = getIntent().getExtras();
        groupId = extras.getString("guid");

        Query query = firebaseDatabase.getReference().child("groups").orderByKey().equalTo(groupId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap : snapshot.getChildren()){
                    group = snap.getValue(Group.class);
                    group.isJoined(new Group.SimpleCallback<Boolean>() {
                        @Override
                        public void callback(Boolean isJoined) {
                            if(isJoined){
                                btnOperation.setText("Leave Group");
                            }
                            else {
                                btnOperation.setText("Join Group");
                            }

                            Glide.with(getApplicationContext())
                                    .load(group.getImageUrl())
                                    .into(imageView);

                            tvName.setText(group.getName());
                            tvType.setText(group.getType());
                            if (group.getDescription() != null){
                                tvDesc2.setText(group.getDescription());
                                tvDesc1.setVisibility(View.VISIBLE);
                                tvDesc2.setVisibility(View.VISIBLE);
                            }

                            group.getMembersCount(new Group.SimpleCallback<Long>() {
                                @Override
                                public void callback(Long num) {
                                    tvMembers.setText(String.valueOf(num));
                                }
                            });

                            btnOperation.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(!isJoined){
                                        if(!group.getPassword().equals("")){
                                            AlertDialog.Builder builder = new AlertDialog.Builder(ViewGroup.this);
                                            LayoutInflater inflater = getLayoutInflater();
                                            View view = inflater.inflate(R.layout.entry_dialog2, null);
                                            builder.setView(view);
                                            EditText etItem = view.findViewById(R.id.etString);
                                            etItem.setTransformationMethod(PasswordTransformationMethod.getInstance());
                                            builder.setTitle("Enter Group Password");
                                            etItem.requestFocus();
                                            Button btnCancel = view.findViewById(R.id.btnCancel);
                                            Button btnOk = view.findViewById(R.id.btnOk);
                                            AlertDialog alertDialog = builder.create();
                                            alertDialog.show();

                                            btnOk.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    String password = etItem.getText().toString();
                                                    if(password.isEmpty()){
                                                        etItem.setError("Enter Password");
                                                        etItem.requestFocus();
                                                    }
                                                    else {
                                                        alertDialog.dismiss();
                                                        DatabaseReference reference = firebaseDatabase.getReference().child("groups").child(groupId).child("members");
                                                        Map<String, Object> map = new HashMap<>();
                                                        map.put("id", email);
                                                        reference.child(email.replace(".", "_")).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()) {
                                                                    Toast.makeText(getApplicationContext(), "You have joined the group", Toast.LENGTH_LONG).show();
                                                                }
                                                                else {
                                                                    Toast.makeText(getApplicationContext(), "Failed to join group, Try again later", Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        });

                                                    }
                                                }
                                            });

                                            btnCancel.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    alertDialog.dismiss();
                                                }
                                            });
                                        }
                                        else {
                                            DatabaseReference reference = firebaseDatabase.getReference().child("groups").child(groupId).child("members");
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("id", email);
                                            reference.child(email.replace(".", "_")).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(getApplicationContext(), "You have joined the group", Toast.LENGTH_LONG).show();
                                                    }
                                                    else {
                                                        Toast.makeText(getApplicationContext(), "Failed to join group, Try again later", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                        }

                                    }
                                    else {
                                        Query query = firebaseDatabase.getReference().child("groups").child(groupId).child("members").orderByChild("id").equalTo(email);
                                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for(DataSnapshot snap : snapshot.getChildren()){
                                                    String key = snap.getKey();
                                                    DatabaseReference reference = query.getRef().child(key);
                                                    reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                Toast.makeText(getApplicationContext(), "You have left the Group", Toast.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }
                            });

                        }
                    });

                    group.isAdmin(new Group.SimpleCallback<Boolean>() {
                        @Override
                        public void callback(Boolean isAdmin) {
                            if (isAdmin){
                                btnEditGroup.setVisibility(View.VISIBLE);
                            }
                            else {
                                btnEditGroup.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                    break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}