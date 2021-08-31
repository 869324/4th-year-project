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
import com.dekut.dekutchat.adapters.ChatAdapter;
import com.dekut.dekutchat.adapters.GroupMembersAdapter;
import com.dekut.dekutchat.adapters.SearchUserAdapter;
import com.dekut.dekutchat.utils.Group;
import com.dekut.dekutchat.utils.Student;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
    List<Student> members = new ArrayList<>();
    List<String> keys = new ArrayList<>();
    Group group;
    SearchUserAdapter userAdapter;

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
                                membersRecyclerView.setVisibility(View.VISIBLE);

                                linearLayoutManager = new LinearLayoutManager(ViewGroup.this);
                                //linearLayoutManager.setReverseLayout(true);
                                //linearLayoutManager.setStackFromEnd(true);
                                membersRecyclerView.setLayoutManager(linearLayoutManager);
                                userAdapter = new SearchUserAdapter(members, ViewGroup.this);
                                userAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
                                membersRecyclerView.setAdapter(userAdapter);

                                fetchMembers();
                            }
                            else {
                                btnOperation.setText("Join Group");
                                btnEditGroup.setVisibility(View.INVISIBLE);
                                membersRecyclerView.setVisibility(View.INVISIBLE);
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
                                        group.isAdmin(new Group.SimpleCallback<Boolean>() {
                                            @Override
                                            public void callback(Boolean isAdmin) {
                                                group.getAdminCount(new Group.SimpleCallback<Long>() {
                                                    @Override
                                                    public void callback(Long adminCount) {
                                                        if (isAdmin && adminCount < 2){
                                                            Toast.makeText(ViewGroup.this, "You cannot leave the Group because you are the only admin!", Toast.LENGTH_LONG).show();
                                                        }
                                                        else {
                                                            leaveGroup();
                                                        }
                                                    }
                                                });

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

    public void fetchMembers(){
        Query query = firebaseDatabase.getReference().child("groups").child(groupId).child("members");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()){
                    String id = snap.child("id").getValue().toString();
                    Query query1 = firebaseDatabase.getReference().child("students").orderByChild("email").equalTo(id);
                    query1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            for (DataSnapshot snap : snapshot.getChildren()){
                                Student student = snap.getValue(Student.class);
                                if (!keys.contains(student.getEmail()) && !student.getEmail().equals(email)){
                                    members.add(student);
                                    keys.add(student.getEmail());
                                    userAdapter.notifyItemInserted(members.size() - 1);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    public void leaveGroup (){
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
                                group.isAdmin(new Group.SimpleCallback<Boolean>() {
                                    @Override
                                    public void callback(Boolean isAdmin) {
                                        if (isAdmin){
                                            Query query1 = firebaseDatabase.getReference().child("groups").child(groupId).child("admins").orderByChild("id").equalTo(email);
                                            query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    for(DataSnapshot snap : snapshot.getChildren()){
                                                        String key = snap.getKey();
                                                        DatabaseReference reference = query1.getRef().child(key);
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
                                        else {
                                            Toast.makeText(getApplicationContext(), "You have left the Group", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });

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