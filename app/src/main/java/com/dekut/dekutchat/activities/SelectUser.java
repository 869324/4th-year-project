package com.dekut.dekutchat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.dekut.dekutchat.R;
import com.dekut.dekutchat.adapters.ChatAdapter;
import com.dekut.dekutchat.adapters.SelectUserAdapter;
import com.dekut.dekutchat.adapters.SelectedUserAdapter;
import com.dekut.dekutchat.utils.Student;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SelectUser extends AppCompatActivity {
    RecyclerView recyclerView1, recyclerView2;
    EditText etSearch;

    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    LinearLayoutManager linearLayoutManager1, linearLayoutManager2;
    String operation, groupId, key, keyword;
    List<Student> students = new ArrayList<>();
    List<Student> selectedStudents = new ArrayList<>();
    List<String> keys1 = new ArrayList<>();
    List<String> keys2 = new ArrayList<>();
    Query query;
    SelectUserAdapter selectUserAdapter;
    SelectedUserAdapter selectedUserAdapter;
    boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_user);

        recyclerView1 = findViewById(R.id.recyclerView1);
        recyclerView2 = findViewById(R.id.recyclerView2);
        etSearch = findViewById(R.id.etSearch);

        Bundle extras = getIntent().getExtras();
        operation = extras.getString("operation");
        groupId = extras.getString("groupId");

        linearLayoutManager1 = new LinearLayoutManager(SelectUser.this);
        recyclerView1.setLayoutManager(linearLayoutManager1);
        selectUserAdapter = new SelectUserAdapter(students, SelectUser.this, email);
        selectUserAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        recyclerView1.setAdapter(selectUserAdapter);

        linearLayoutManager2 = new LinearLayoutManager(SelectUser.this);
        recyclerView2.setLayoutManager(linearLayoutManager2);
        selectedUserAdapter = new SelectedUserAdapter(students, SelectUser.this, email);
        selectedUserAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        recyclerView2.setAdapter(selectedUserAdapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                keyword = String.valueOf(charSequence);
                searchUsers();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        isLoading = true;
        searchUsers();
    }

    public void searchUsers() {
        if (key == null) {
            query = firebaseDatabase.getReference().child("students").orderByKey().limitToFirst(2);
        }

        else {
            query = firebaseDatabase.getReference().child("students").orderByKey().limitToFirst(2).startAt(key);
        }

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        Student student = snap.getValue(Student.class);

                        if (student.getEmail().equals(email) || keys1.contains(student.getId())) {
                            continue;
                        }

                        else {
                            if (keyword == null) {
                                students.add(student);
                                keys1.add(student.getId());
                                selectUserAdapter.notifyItemInserted(students.size() - 1);

                            }

                            else {
                                String name = student.getUserName().toLowerCase();
                                String email = student.getEmail().toLowerCase();

                                if (name.contains(keyword) || email.contains(keyword)) {
                                    students.add(student);
                                    keys1.add(student.getId());
                                    selectUserAdapter.notifyItemInserted(students.size() - 1);
                                }
                            }
                        }
                        key = student.getId();
                    }

                    if (students.isEmpty()){
                        searchUsers();
                    }
                    isLoading = false;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}