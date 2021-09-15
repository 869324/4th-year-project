package com.dekut.dekutchat.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.dekut.dekutchat.R;
import com.dekut.dekutchat.adapters.GroupChatAdapter;
import com.dekut.dekutchat.adapters.LeaderAdapter;
import com.dekut.dekutchat.adapters.SearchUserAdapter;
import com.dekut.dekutchat.utils.Student;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StudentLeaders extends AppCompatActivity {
    RecyclerView recyclerView;
    FloatingActionButton fabAdd, fabRemove;

    ProgressDialog progressDialog;
    PopupWindow popupWindow;
    AlertDialog alertDialog;
    String email;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    LinearLayoutManager linearLayoutManager;
    LeaderAdapter leaderAdapter;
    List<Student> leaders = new ArrayList<>();
    List<String> keys = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_leaders);

        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);
        fabRemove = findViewById(R.id.fabRemove);

        Bundle extras = getIntent().getExtras();
        email = extras.getString("profileEmail");

        linearLayoutManager = new LinearLayoutManager(StudentLeaders.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        leaderAdapter = new LeaderAdapter(leaders, StudentLeaders.this);
        leaderAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        recyclerView.setAdapter(leaderAdapter);

        Query query = firebaseDatabase.getReference().child("students").orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Student student = snap.getValue(Student.class);

                    if (student.getType().equals("admin")) {
                        fabAdd.setVisibility(View.VISIBLE);
                        fabRemove.setVisibility(View.VISIBLE);
                    } else {
                        fabAdd.setVisibility(View.GONE);
                        fabRemove.setVisibility(View.GONE);
                    }
                    break;
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        fetchLeaders();

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view);
            }
        });

        fabRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEntryDialog("Student Email");
            }
        });
    }

    public void fetchLeaders() {
        Query query1 = firebaseDatabase.getReference().child("students").orderByChild("type").equalTo("leader");

        query1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                Student student = snapshot.getValue(Student.class);
                if (!keys.contains(student.getId())) {
                    leaders.add(student);
                    keys.add(student.getId());
                    leaderAdapter.notifyItemInserted(leaders.size() - 1);
                }

            }

            @Override
            public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {
                Student student = snapshot.getValue(Student.class);
                for (Student student1 : leaders){
                    if (student.getId().equals(student1.getId())) {
                        int index1 = leaders.indexOf(student1);
                        int index2 = keys.indexOf(student.getId());
                        leaders.remove(index1);
                        keys.remove(index2);
                        leaderAdapter.notifyItemRemoved(index1);
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    public void showPopup(View v){
        LayoutInflater inflater = getLayoutInflater();
        View popupView = inflater.inflate(R.layout.add_leader_popup, null);
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        popupWindow = new PopupWindow(popupView, width, height, true);
        popupWindow.setElevation(10);
        popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

        EditText etEmail = popupView.findViewById(R.id.etEmail);
        EditText etPosition = popupView.findViewById(R.id.etPosition);
        Button btnCancel = popupView.findViewById(R.id.btnCancel);
        Button btnOk = popupView.findViewById(R.id.btnOk);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = new ProgressDialog(StudentLeaders.this);
                progressDialog.show();
                progressDialog.setContentView(R.layout.progress_dialog);
                progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                progressDialog.setCancelable(false);

                String email = etEmail.getText().toString();
                String position = etPosition.getText().toString();

                if (email.isEmpty()){
                    progressDialog.dismiss();
                    etEmail.setError("Enter Student Email");
                    etEmail.requestFocus();
                }

                else if (position.isEmpty()){
                    progressDialog.dismiss();
                    etPosition.setError("Enter Position");
                    etPosition.requestFocus();
                }

                else {
                    Query query = firebaseDatabase.getReference().child("students").orderByChild("email").equalTo(email);
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                for (DataSnapshot snap : snapshot.getChildren()){
                                    Student student = snap.getValue(Student.class);
                                    DatabaseReference reference = snap.getRef();
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("type", "leader");
                                    map.put("position", position);

                                    reference.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(StudentLeaders.this, "Student leader added successfully", Toast.LENGTH_LONG).show();
                                                popupWindow.dismiss();
                                            }
                                            else {
                                                Toast.makeText(StudentLeaders.this, "Failed to add student leader. Try again later", Toast.LENGTH_LONG).show();
                                            }
                                            progressDialog.dismiss();
                                        }
                                    });
                                    break;
                                }
                            }
                            else {
                                progressDialog.dismiss();
                                Toast.makeText(StudentLeaders.this, "There is no user with such Email", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });
                }
            }
        });
    }

    public void showEntryDialog(String child){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.entry_dialog2, null);
        builder.setView(view);
        EditText etItem = view.findViewById(R.id.etString);
        builder.setTitle("Enter " + child);
        etItem.requestFocus();
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnOk = view.findViewById(R.id.btnOk);
        alertDialog = builder.create();
        alertDialog.show();

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String item = etItem.getText().toString().trim();
                if(item.isEmpty()){
                    etItem.setError("Enter Item");
                    etItem.requestFocus();
                }
                else {
                    showAlertDialog(item);
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });
    }

    public void showAlertDialog(String item){
        Dialog deleteDialog = new Dialog(StudentLeaders.this);
        deleteDialog.setContentView(R.layout.delete_dialog2);
        deleteDialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        deleteDialog.setCancelable(false);

        TextView textView = deleteDialog.findViewById(R.id.tvTitle);
        Button btnCancel = deleteDialog.findViewById(R.id.btnCancel);
        Button btnDelete = deleteDialog.findViewById(R.id.btnDelete);

        textView.setText("Are you sure you want to remove this Student Leader?");

        deleteDialog.show();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDialog.dismiss();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressDialog progressDialog = new ProgressDialog(StudentLeaders.this);
                progressDialog.show();
                progressDialog.setContentView(R.layout.progress_dialog);
                progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                deleteDialog.dismiss();

                Query query = firebaseDatabase.getReference().child("students").orderByChild("email").equalTo(item);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            for (DataSnapshot snap : snapshot.getChildren()){
                                Student student = snap.getValue(Student.class);

                                if (student.getType().equals("leader")) {
                                    DatabaseReference reference = snap.getRef();
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("type", "student");
                                    map.put("position", null);

                                    reference.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(StudentLeaders.this, "Student leader removed successfully", Toast.LENGTH_LONG).show();
                                                alertDialog.dismiss();
                                            } else {
                                                Toast.makeText(StudentLeaders.this, "Failed to remove student leader. Try again later", Toast.LENGTH_LONG).show();
                                            }
                                            progressDialog.dismiss();
                                        }
                                    });
                                    break;
                                }

                                else {
                                    progressDialog.dismiss();
                                    Toast.makeText(StudentLeaders.this, "This user is not a Student Leader", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                        else {
                            progressDialog.dismiss();
                            Toast.makeText(StudentLeaders.this, "There is no user with such Email", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        });
    }
}