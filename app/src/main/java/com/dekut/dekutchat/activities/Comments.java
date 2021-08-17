package com.dekut.dekutchat.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dekut.dekutchat.R;
import com.dekut.dekutchat.adapters.CommentsAdapter;
import com.dekut.dekutchat.adapters.HomeAdapter;
import com.dekut.dekutchat.adapters.PoliticsAdapter;
import com.dekut.dekutchat.utils.Comment;
import com.dekut.dekutchat.utils.HomePost;
import com.dekut.dekutchat.utils.PoliticsPost;
import com.dekut.dekutchat.utils.Student;
import com.dekut.dekutchat.utils.TimeCalc;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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

public class Comments extends AppCompatActivity {
    RecyclerView recyclerView, recyclerView1;
    TextView tvText;
    ImageButton btnSend;
    EditText etComment;

    FirebaseDatabase firebaseDatabase =  FirebaseDatabase.getInstance();
    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    String source, id;
    HomePost homePost;
    PoliticsPost politicsPost;
    CommentsAdapter adapter;
    RecyclerView.Adapter postAdapter;
    LinearLayoutManager linearLayoutManager;
    LinearLayoutManager linearLayoutManager1;
    List<Comment> comments = new ArrayList<>();
    List<HomePost> hPosts = new ArrayList<>();
    List<PoliticsPost> pPosts = new ArrayList<>();
    List<String> keys = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        tvText = findViewById(R.id.tvText);
        btnSend = findViewById(R.id.btnSend);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView1 = findViewById(R.id.recyclerView1);
        etComment = findViewById(R.id.etComment);

        Bundle extras = getIntent().getExtras();
        source = extras.getString("source");
        id = extras.getString("id");

        linearLayoutManager1 = new LinearLayoutManager(Comments.this);
        recyclerView1.setLayoutManager(linearLayoutManager1);
        recyclerView1.setHasFixedSize(true);
        recyclerView1.setItemViewCacheSize(20);

        if (source.equals("home")) {
            postAdapter = new HomeAdapter(hPosts, Comments.this, email, false);
            postAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
            recyclerView1.setAdapter(postAdapter);

            Query query = firebaseDatabase.getReference().child("homePosts").orderByKey().equalTo(id);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        homePost = snap.getValue(HomePost.class);
                        hPosts.add(homePost);
                        postAdapter.notifyDataSetChanged();
                        break;
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        }
        else if (source.equals("politics")){
            postAdapter = new PoliticsAdapter(pPosts, Comments.this, email, false);
            postAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
            recyclerView1.setAdapter(postAdapter);

            Query query = firebaseDatabase.getReference().child("politicsPosts").orderByKey().equalTo(id);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        politicsPost = snap.getValue(PoliticsPost.class);
                        pPosts.add(politicsPost);
                        postAdapter.notifyDataSetChanged();
                        break;
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        }

        linearLayoutManager = new LinearLayoutManager(Comments.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        adapter = new CommentsAdapter(comments, Comments.this);
        adapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        recyclerView.setAdapter(adapter);

        fetchComments();

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                        View view = getCurrentFocus();
                        if (view == null) {
                            view = new View(Comments.this);
                        }
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                });

                thread.start();

                String text = etComment.getText().toString();
                etComment.setText("");
                if (!text.isEmpty()){
                    DatabaseReference reference;
                    if (source.equals("home")){
                        reference = firebaseDatabase.getReference().child("homePosts").child(id).child("comments");
                    }
                    else {
                        reference = firebaseDatabase.getReference().child("politicsPosts").child(id).child("comments");
                    }

                    Map<String, Object> map = new HashMap<>();
                    map.put("text", text);
                    map.put("posterId", email);
                    map.put("post", id);

                    DatabaseReference reference1 = reference.push();
                    String key = reference1.getKey();
                    map.put("id", key);
                    map.put("timestamp", ServerValue.TIMESTAMP);

                    reference1.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                        }
                    });
                }
            }
        });

    }

    public void fetchComments(){
        Query query;
        if (source.equals("home")){
            query = firebaseDatabase.getReference().child("homePosts").child(id).child("comments");
        }
        else {
            query = firebaseDatabase.getReference().child("politicsPosts").child(id).child("comments");
        }

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                Comment comment = snapshot.getValue(Comment.class);
                if (comment.getId() != null && !keys.contains(comment.getId())) {
                    comments.add(comment);
                    keys.add(comment.getId());
                    adapter.notifyItemInserted(comments.size());
                }
            }

            @Override
            public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                Comment comment = snapshot.getValue(Comment.class);
                for(Comment comment1 : comments) {
                    if (comment.getId() != null && comment.getId().equals(comment1.getId())) {
                        int index = comments.indexOf(comment1);
                        comments.remove(index);
                        comments.add(index, comment);
                        adapter.notifyItemChanged(index);
                        //adapter.notifyItemChanged(index, "likes");
                        break;
                    }
                    else if(comment.getId() != null && !keys.contains(comment.getId())){
                        comments.add(comment);
                        keys.add(comment.getId());
                        adapter.notifyItemInserted(comments.size());
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {
                Comment comment = snapshot.getValue(Comment.class);
                for(Comment comment1 : comments) {
                    if (comment.getId().equals(comment1.getId())) {
                        int index = comments.indexOf(comment1);
                        int index2 = keys.indexOf(comment1.getId());
                        comments.remove(index);
                        keys.remove(index2);
                        adapter.notifyItemRemoved(index);
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