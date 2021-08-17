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
import com.dekut.dekutchat.adapters.PoliticsAdapter;
import com.dekut.dekutchat.utils.HomePost;
import com.dekut.dekutchat.utils.PoliticsPost;
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
 * Use the {@link Politics1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Politics1 extends Fragment {

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
    PoliticsAdapter adapter;
    Context context;
    Query query;
    List<PoliticsPost> posts = new ArrayList<>();
    List<String> keys = new ArrayList<>();
    boolean isLoading = false;
    long timestamp = 0;
    String profileEmail;

    public Politics1() {
        // Required empty public constructor
    }

    public Politics1(String profileEmail){
        this.profileEmail = profileEmail;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Politics1.
     */
    // TODO: Rename and change types and number of parameters
    public static Politics1 newInstance(String param1, String param2) {
        Politics1 fragment = new Politics1();
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
        View view = inflater.inflate(R.layout.fragment_politics1, container, false);

        recyclerView = view.findViewById((R.id.recyclerView));

        linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);

        if (email.equals(profileEmail)) {
            adapter = new PoliticsAdapter(posts, context, profileEmail, true);
        }
        else {
            adapter = new PoliticsAdapter(posts, context, profileEmail, false);
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
            query = firebaseDatabase.getReference().child("politicsPosts").orderByChild("timestamp").limitToLast(100);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        int counter = 0;
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            PoliticsPost politicsPost = snap.getValue(PoliticsPost.class);
                            if (!keys.contains(politicsPost.getId()) && politicsPost.getPoster().equals(profileEmail)) {
                                posts.add(politicsPost);
                                keys.add(politicsPost.getId());
                                adapter.notifyItemInserted(posts.size());
                            }
                            if (counter == 0) {
                                timestamp = politicsPost.getTimestamp();
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
        else {
            query = firebaseDatabase.getReference().child("politicsPosts").orderByChild("timestamp").limitToLast(100).endBefore(timestamp);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        int counter = 0;
                        for (DataSnapshot snap : snapshot.getChildren()){
                            PoliticsPost politicsPost = snap.getValue(PoliticsPost.class);
                            if (!keys.contains(politicsPost.getId()) && politicsPost.getPoster().equals(profileEmail)) {
                                posts.add(counter, politicsPost);
                                keys.add(counter, politicsPost.getId());
                                adapter.notifyItemInserted(counter);
                            }
                            if(counter == 0){
                                timestamp = politicsPost.getTimestamp();
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
                PoliticsPost politicsPost = snapshot.getValue(PoliticsPost.class);
                for (PoliticsPost politicsPost1 : posts){
                    if (politicsPost.getId().equals(politicsPost1.getId())){
                        int index = posts.indexOf(politicsPost1);
                        posts.remove(index);
                        adapter.notifyItemRemoved(index);
                        keys.remove(politicsPost1.getId());
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