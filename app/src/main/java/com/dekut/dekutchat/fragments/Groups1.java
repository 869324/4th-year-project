package com.dekut.dekutchat.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dekut.dekutchat.R;
import com.dekut.dekutchat.adapters.HomeAdapter;
import com.dekut.dekutchat.adapters.SearchGroupAdapter;
import com.dekut.dekutchat.utils.Group;
import com.dekut.dekutchat.utils.HomePost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Groups1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Groups1 extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    SearchGroupAdapter adapter;
    Context context;
    Query query;
    List<Group> groups = new ArrayList<>();
    List<String> keys = new ArrayList<>();
    boolean isLoading = false;
    String key;
    String profileEmail;
    boolean found = false;

    public Groups1() {
        // Required empty public constructor
    }

    public Groups1(String profileEmail){
        this.profileEmail = profileEmail;
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Groups1.
     */
    // TODO: Rename and change types and number of parameters
    public static Groups1 newInstance(String param1, String param2) {
        Groups1 fragment = new Groups1();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = container.getContext();
        View view = inflater.inflate(R.layout.fragment_groups1, container, false);

        recyclerView = view.findViewById((R.id.recyclerView));

        linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);


        adapter = new SearchGroupAdapter(groups, context, profileEmail);
        adapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        recyclerView.setAdapter(adapter);

        isLoading = true;
        fetchGroups();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull @NotNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();

                if (lastPosition < 2){
                    if (!isLoading){
                        isLoading = true;
                        fetchGroups();
                    }
                }
            }
        });

        return view;
    }

    public void fetchGroups(){
        if (key == null) {
            query = firebaseDatabase.getReference().child("groups").orderByKey().limitToFirst(100);

            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            if (snap.exists()) {
                                Group group = snap.getValue(Group.class);
                                if (!keys.contains(group.getGroupId())) {
                                    Query query1 = firebaseDatabase.getReference().child("groups").child(group.getGroupId()).child("members").orderByChild("id").equalTo(profileEmail);
                                    query1.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                            if (snapshot.exists()){
                                                groups.add(group);
                                                keys.add(group.getGroupId());
                                                adapter.notifyItemInserted(groups.size() - 1);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                        }
                                    });
                                }

                                key = group.getGroupId();
                            }
                        }

                        if (groups.isEmpty()){
                            fetchGroups();
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
        else {
            query = firebaseDatabase.getReference().child("groups").orderByKey().limitToFirst(100).startAfter(key);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        for (DataSnapshot snap : snapshot.getChildren()){
                            Group group = snap.getValue(Group.class);
                            if (!keys.contains(group.getGroupId())) {
                                Query query1 = firebaseDatabase.getReference().child("groups").child(group.getGroupId()).child("members").orderByChild("id").equalTo(profileEmail);
                                query1.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                        if (snapshot.exists()){
                                            groups.add(group);
                                            keys.add(group.getGroupId());
                                            adapter.notifyItemInserted(groups.size() - 1);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                    }
                                });

                            }
                            key = group.getGroupId();
                        }
                    }

                    if (groups.isEmpty()){
                        fetchGroups();
                    }
                    else {
                        isLoading = false;
                    }

                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        }

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

            }

            @Override
            public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                Group group = snapshot.getValue(Group.class);
                for (Group group1 : groups){
                    if (group.getGroupId().equals(group1.getGroupId())){
                        Query query1 = firebaseDatabase.getReference().child("groups").child(group.getGroupId()).child("members").orderByChild("id").equalTo(profileEmail);
                        query1.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                if (!snapshot.exists()){
                                    int index = groups.indexOf(group1);
                                    groups.remove(index);
                                    adapter.notifyItemRemoved(index);
                                    keys.remove(group1.getGroupId());
                                    found = true;
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });

                        if (found) {
                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {
                Group group = snapshot.getValue(Group.class);
                for (Group group1 : groups){
                    if (group.getGroupId().equals(group1.getGroupId())){
                        int index = groups.indexOf(group1);
                        groups.remove(index);
                        adapter.notifyItemRemoved(index);
                        keys.remove(group1.getGroupId());
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

}