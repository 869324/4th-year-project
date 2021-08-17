package com.dekut.dekutchat.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dekut.dekutchat.R;
import com.dekut.dekutchat.adapters.HomeAdapter;
import com.dekut.dekutchat.adapters.ProfilePostAdapter;
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
 * Use the {@link Home1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Home1 extends Fragment {

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
    HomeAdapter adapter;
    Context context;
    Query query;
    List<HomePost> posts = new ArrayList<>();
    List<String> keys = new ArrayList<>();
    boolean isLoading = false;
    long timestamp = 0;
    String profileEmail;

    public Home1() {
        // Required empty public constructor
    }

    public Home1(String profileEmail){
        this.profileEmail = profileEmail;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Home1.
     */
    // TODO: Rename and change types and number of parameters
    public static Home1 newInstance(String param1, String param2) {
        Home1 fragment = new Home1();
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
        View view = inflater.inflate(R.layout.fragment_home1, container, false);

        recyclerView = view.findViewById((R.id.recyclerView));

        linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);

        if (email.equals(profileEmail)) {
            adapter = new HomeAdapter(posts, context, profileEmail, true);
        }
        else {
            adapter = new HomeAdapter(posts, context, profileEmail, false);
        }
        adapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        recyclerView.setAdapter(adapter);

        fetchPosts();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull @NotNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();

                if (lastPosition < 2){
                    if (!isLoading){
                        isLoading = true;
                        fetchPosts();
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void fetchPosts(){
        if (timestamp == 0) {
            query = firebaseDatabase.getReference().child("homePosts").orderByChild("timestamp").limitToLast(100);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        int counter = 0;
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            if (snap.exists()) {
                                HomePost homePost = snap.getValue(HomePost.class);
                                if (!keys.contains(homePost.getId()) && homePost.getPoster().equals(profileEmail)) {
                                    posts.add(homePost);
                                    keys.add(homePost.getId());
                                    adapter.notifyItemInserted(posts.size());
                                }
                                if (counter == 0) {
                                    timestamp = homePost.getTimestamp();
                                }
                                counter += 1;
                            }
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
            query = firebaseDatabase.getReference().child("homePosts").orderByChild("timestamp").limitToLast(100).endBefore(timestamp);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        int counter = 0;
                        for (DataSnapshot snap : snapshot.getChildren()){
                            HomePost homePost = snap.getValue(HomePost.class);
                            if (!keys.contains(homePost.getId()) && homePost.getPoster().equals(profileEmail)) {
                                posts.add(counter, homePost);
                                keys.add(counter, homePost.getId());
                                adapter.notifyItemInserted(counter);
                            }
                            if(counter == 0){
                                timestamp = homePost.getTimestamp();
                            }
                            counter += 1;
                        }
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

            }

            @Override
            public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {
                HomePost homePost = snapshot.getValue(HomePost.class);
                for (HomePost homePost1 : posts){
                    if (homePost.getId().equals(homePost1.getId())){
                        int index = posts.indexOf(homePost1);
                        posts.remove(index);
                        adapter.notifyItemRemoved(index);
                        keys.remove(homePost1.getId());
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