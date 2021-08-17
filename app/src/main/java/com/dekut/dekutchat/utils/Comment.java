package com.dekut.dekutchat.utils;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Comment {
    String id, posterId, post, text;
    long timestamp;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    public Comment(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPosterId() {
        return posterId;
    }

    public void setPoster(String posterId) {
        this.posterId = posterId;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void getPoster(@NonNull Comment.SimpleCallback<Student> finishedCallback){
        Query query = firebaseDatabase.getReference().child("students").orderByChild("email").equalTo(posterId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap : snapshot.getChildren()) {
                    Student poster = snap.getValue(Student.class);
                    finishedCallback.callback(poster);
                    break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public interface SimpleCallback<T> {
        void callback(T data);
    }
}
