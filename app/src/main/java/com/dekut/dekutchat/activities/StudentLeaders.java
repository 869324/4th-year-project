package com.dekut.dekutchat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.dekut.dekutchat.R;
import com.dekut.dekutchat.adapters.GroupChatAdapter;
import com.dekut.dekutchat.adapters.LeaderAdapter;
import com.dekut.dekutchat.adapters.SearchUserAdapter;
import com.dekut.dekutchat.utils.Student;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

public class StudentLeaders extends AppCompatActivity {
    RecyclerView recyclerView;
    FloatingActionButton fabAdd;

    ProgressDialog progressDialog;
    PopupWindow popupWindow;
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
                    } else {
                        fabAdd.setVisibility(View.GONE);
                    }
                    break;
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        Query query1 = firebaseDatabase.getReference().child("students").orderByChild("type").equalTo("leader");
        query1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()){
                    Student student = snap.getValue(Student.class);
                    if (!keys.contains(student.getId())) {
                        leaders.add(student);
                        keys.add(student.getId());
                        leaderAdapter.notifyItemInserted(leaders.size() - 1);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view);
            }
        });
    }

    public void showPopup(View v){
        LayoutInflater inflater = getLayoutInflater();
        View popupView = inflater.inflate(R.layout.add_leader_popup, null);
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        popupWindow = new PopupWindow(popupView, width, height, true);
        popupWindow.setElevation(10);
        popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

        EditText etEmail = popupView.findViewById(R.id.etEmail);
        EditText etPostion = popupView.findViewById(R.id.etPosition);
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
                String position = etPostion.getText().toString();

                if (email.isEmpty()){
                    progressDialog.dismiss();
                    etEmail.setError("Enter Email");
                }

                else if (position.isEmpty()){
                    progressDialog.dismiss();
                    etPostion.setError("Enter Position");
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
                                            }
                                            else {
                                                Toast.makeText(StudentLeaders.this, "Failed to add student leader. Try again later", Toast.LENGTH_LONG).show();
                                            }
                                            progressDialog.dismiss();
                                            popupWindow.dismiss();
                                        }
                                    });
                                    break;
                                }
                            }
                            else {
                                progressDialog.dismiss();
                                popupWindow.dismiss();
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
}