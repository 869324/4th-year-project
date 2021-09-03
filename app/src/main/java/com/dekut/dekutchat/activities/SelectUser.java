package com.dekut.dekutchat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.dekut.dekutchat.R;
import com.dekut.dekutchat.adapters.ChatAdapter;
import com.dekut.dekutchat.adapters.SelectUserAdapter;
import com.dekut.dekutchat.adapters.SelectedUserAdapter;
import com.dekut.dekutchat.utils.Group;
import com.dekut.dekutchat.utils.Student;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectUser extends AppCompatActivity {
    RecyclerView recyclerView1, recyclerView2;
    EditText etSearch;
    Button btnAdd;

    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    LinearLayoutManager linearLayoutManager1, linearLayoutManager2;
    String operation, groupId, key, keyword;
    List<Student> students = new ArrayList<>();
    List<Student> selectedStudents = new ArrayList<>();
    List<String> keys1 = new ArrayList<>();
    List<String> keys2 = new ArrayList<>();
    Query query;
    Group group;
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
        btnAdd = findViewById(R.id.btnAdd);

        Bundle extras = getIntent().getExtras();
        operation = extras.getString("operation");
        groupId = extras.getString("groupId");

        if (operation.contains("remove")){
            btnAdd.setText("Remove");
        }

        linearLayoutManager1 = new LinearLayoutManager(SelectUser.this);
        linearLayoutManager1.setOrientation(RecyclerView.HORIZONTAL);
        recyclerView1.setLayoutManager(linearLayoutManager1);
        selectedUserAdapter = new SelectedUserAdapter(selectedStudents, SelectUser.this, email);
        selectedUserAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        recyclerView1.setAdapter(selectedUserAdapter);

        linearLayoutManager2 = new LinearLayoutManager(SelectUser.this);
        recyclerView2.setLayoutManager(linearLayoutManager2);
        selectUserAdapter = new SelectUserAdapter(students, SelectUser.this, email);
        selectUserAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        recyclerView2.setAdapter(selectUserAdapter);

        Query query = firebaseDatabase.getReference().child("groups").orderByKey().equalTo(groupId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    group = snap.getValue(Group.class);
                    students.clear();
                    keys1.clear();
                    key = null;
                    selectUserAdapter.notifyDataSetChanged();

                    if (operation.equals("addAdmins")) {
                        fetchMembers();
                    }
                    else if (operation.equals("addMembers")){
                        searchUsers();
                    }

                    else if (operation.equals("removeMembers")){
                        fetchMembers2();
                    }

                    else if (operation.equals("removeAdmins")){
                        fetchAdmins();
                    }
                    break;
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        recyclerView2.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull @NotNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager linearLayoutManager1 = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastPosition = linearLayoutManager1.findLastCompletelyVisibleItemPosition();
                int totalItems = linearLayoutManager1.getItemCount();

                if ((totalItems -  lastPosition) < 3){
                    if (!isLoading) {
                        if (operation.equals("addAdmins")) {
                            fetchMembers();
                        }
                        else if (operation.equals("addMembers")){
                            searchUsers();
                        }

                        else if (operation.equals("removeMembers")){
                            fetchMembers2();
                        }

                        else if (operation.equals("removeAdmins")){
                            fetchAdmins();
                        }
                    }
                }
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference reference;
                if (operation.equals("addMembers") || operation.equals("removeMembers")) {
                    reference = firebaseDatabase.getReference().child("groups").child(groupId).child("members");
                }

                else {
                    reference = firebaseDatabase.getReference().child("groups").child(groupId).child("admins");
                }

                for (Student student : selectedStudents) {
                    DatabaseReference reference1 = reference.child(student.getEmail().replace(".", "_"));

                    if (operation.contains("add")) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", student.getEmail());

                        if (operation.equals("addMembers")) {
                            map.put("joinedAt", ServerValue.TIMESTAMP);
                            map.put("lastRead", 0l);
                        }

                        reference1.setValue(map);
                    }

                    else {
                        reference1.removeValue();
                    }
                }

                onBackPressed();
                finish();
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                keyword = String.valueOf(charSequence).toLowerCase();
                students.clear();
                keys1.clear();
                selectUserAdapter.notifyDataSetChanged();
                key = null;

                if (operation.equals("addAdmins")) {
                    fetchMembers();
                }
                else if (operation.equals("addMembers")){
                    searchUsers();
                }

                else if (operation.equals("removeMembers")){
                    fetchMembers2();
                }

                else if (operation.equals("removeAdmins")){
                    fetchAdmins();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void searchUsers() {
        isLoading = true;
        if (key == null) {
            query = firebaseDatabase.getReference().child("students").orderByKey().limitToFirst(100);
        } else {
            query = firebaseDatabase.getReference().child("students").orderByKey().limitToFirst(100).startAt(key);
        }

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        Student student = snap.getValue(Student.class);

                        Query query = firebaseDatabase.getReference().child("groups").child(groupId).child("members").orderByChild("id").equalTo(student.getEmail());
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                if (!snapshot.exists()){
                                    if (!student.getEmail().equals(email) && !keys1.contains(student.getId())) {
                                        if (keyword == null || keyword.isEmpty()) {
                                            students.add(student);
                                            keys1.add(student.getId());
                                            selectUserAdapter.notifyItemInserted(students.size() - 1);
                                        } else {
                                            String name = student.getUserName().toLowerCase();
                                            String email = student.getEmail().toLowerCase();

                                            if (name.contains(keyword) || email.contains(keyword)) {
                                                students.add(student);
                                                keys1.add(student.getId());
                                                selectUserAdapter.notifyItemInserted(students.size() - 1);
                                            }
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });
                        key = student.getId();
                    }

                    if (students.isEmpty()){
                        searchUsers();
                    }
                    else {
                        isLoading = false;
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void addUser(Student student){
        if (!keys2.contains(student.getId())){
            recyclerView1.setVisibility(View.VISIBLE);
            btnAdd.setVisibility(View.VISIBLE);
            selectedStudents.add(student);
            keys2.add(student.getId());
            selectedUserAdapter.notifyItemInserted(selectedStudents.size() - 1);
        }
    }

    public void removeUser(Student student){
        if (keys2.contains(student.getId())){
            for (Student student1 : selectedStudents) {
                if (student.getId().equals(student1.getId())){
                    int index1 = selectedStudents.indexOf(student1);
                    int index2 = keys2.indexOf(student.getId());
                    selectedStudents.remove(index1);
                    keys2.remove(index2);
                    selectedUserAdapter.notifyItemRemoved(index1);
                    if (selectedStudents.isEmpty()){
                        recyclerView1.setVisibility(View.GONE);
                        btnAdd.setVisibility(View.GONE);
                    }

                    break;
                }
            }
        }
    }

    public boolean isSelected(Student student) {
        boolean isSelected = false;
        for (Student student1 : selectedStudents) {
            if (student.getId().equals(student1.getId())){
                isSelected = true;
                break;
            }
        }
        return isSelected;
    }

    public void Uncheck (Student student){
        selectUserAdapter.uncheck(student);
    }

    public void fetchMembers(){
        isLoading = true;
        if (key == null) {
            query = firebaseDatabase.getReference().child("groups").child(groupId).child("members").orderByKey().limitToFirst(100);
        } else {
            query = firebaseDatabase.getReference().child("groups").child(groupId).child("members").orderByKey().limitToFirst(100).startAt(key);
        }

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        String id = snap.child("id").getValue().toString();
                        Query query1 = firebaseDatabase.getReference().child("groups").child(groupId).child("admins").orderByChild("id").equalTo(id);
                        query1.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                if (!snapshot.exists()) {
                                    Query query2 = firebaseDatabase.getReference().child("students").orderByChild("email").equalTo(id);
                                    query2.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                            for (DataSnapshot snap : snapshot.getChildren()) {
                                                Student student = snap.getValue(Student.class);

                                                if (!keys1.contains(student.getEmail()) && !student.getEmail().equals(email)) {

                                                    if (keyword == null || keyword.isEmpty()) {
                                                        students.add(student);
                                                        keys1.add(student.getEmail());
                                                        selectUserAdapter.notifyItemInserted(students.size() - 1);
                                                    } else {
                                                        String name = student.getUserName().toLowerCase();
                                                        String email = student.getEmail().toLowerCase();

                                                        if (name.contains(keyword) || email.contains(keyword)) {
                                                            students.add(student);
                                                            keys1.add(student.getEmail());
                                                            selectUserAdapter.notifyItemInserted(students.size() - 1);
                                                        }
                                                    }
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
                        key = id.replace(".", "_");
                    }

                    if (students.isEmpty()){
                        fetchMembers();
                    }
                    else {
                        isLoading = false;
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    public void fetchMembers2(){
        isLoading = true;
        if (key == null) {
            query = firebaseDatabase.getReference().child("groups").child(groupId).child("members").orderByKey().limitToFirst(100);
        } else {
            query = firebaseDatabase.getReference().child("groups").child(groupId).child("members").orderByKey().limitToFirst(100).startAt(key);
        }

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        String id = snap.child("id").getValue().toString();

                        Query query2 = firebaseDatabase.getReference().child("students").orderByChild("email").equalTo(id);
                        query2.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                for (DataSnapshot snap : snapshot.getChildren()) {
                                    Student student = snap.getValue(Student.class);

                                    if (!keys1.contains(student.getEmail()) && !student.getEmail().equals(email)) {

                                        if (keyword == null || keyword.isEmpty()) {
                                            students.add(student);
                                            keys1.add(student.getEmail());
                                            selectUserAdapter.notifyItemInserted(students.size() - 1);
                                        } else {
                                            String name = student.getUserName().toLowerCase();
                                            String email = student.getEmail().toLowerCase();

                                            if (name.contains(keyword) || email.contains(keyword)) {
                                                students.add(student);
                                                keys1.add(student.getEmail());
                                                selectUserAdapter.notifyItemInserted(students.size() - 1);
                                            }
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });
                        key = id.replace(".", "_");
                    }

                    if (students.isEmpty()){
                        fetchMembers2();
                    }
                    else {
                        isLoading = false;
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    public void fetchAdmins(){
        isLoading = true;
        if (key == null) {
            query = firebaseDatabase.getReference().child("groups").child(groupId).child("admins").orderByKey().limitToFirst(100);
        } else {
            query = firebaseDatabase.getReference().child("groups").child(groupId).child("admins").orderByKey().limitToFirst(100).startAt(key);
        }

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        String id = snap.child("id").getValue().toString();

                        Query query2 = firebaseDatabase.getReference().child("students").orderByChild("email").equalTo(id);
                        query2.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                for (DataSnapshot snap : snapshot.getChildren()) {
                                    Student student = snap.getValue(Student.class);

                                    if (!keys1.contains(student.getEmail()) && !student.getEmail().equals(email)) {

                                        if (keyword == null || keyword.isEmpty()) {
                                            students.add(student);
                                            keys1.add(student.getEmail());
                                            selectUserAdapter.notifyItemInserted(students.size() - 1);
                                        } else {
                                            String name = student.getUserName().toLowerCase();
                                            String email = student.getEmail().toLowerCase();

                                            if (name.contains(keyword) || email.contains(keyword)) {
                                                students.add(student);
                                                keys1.add(student.getEmail());
                                                selectUserAdapter.notifyItemInserted(students.size() - 1);
                                            }
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });
                        key = id.replace(".", "_");
                    }

                    if (students.isEmpty()){
                        fetchAdmins();
                    }
                    else {
                        isLoading = false;
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}