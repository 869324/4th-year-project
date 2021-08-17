package com.dekut.dekutchat.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class PoliticsPost {
    String id, poster, text, imageUrl, videoUrl, type;
    long timestamp;
    int imgHeight;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

    public PoliticsPost(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPoster() {
        return poster;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getImgHeight() {
        return imgHeight;
    }

    public void setImgHeight(int imgHeight) {
        this.imgHeight = imgHeight;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void getPoster(@NonNull PoliticsPost.SimpleCallback<Student> finishedCallback){
        Query query = firebaseDatabase.getReference().child("students").orderByChild("email").equalTo(poster);
        query.addValueEventListener(new ValueEventListener() {
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

    public void isLiked(@NonNull PoliticsPost.SimpleCallback<Boolean> finishedCallback){
        Query query = firebaseDatabase.getReference().child("politicsPosts").child(id).child("likes").orderByChild("id").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isLiked = false;
                if(snapshot.exists()){
                    isLiked = true;
                }
                finishedCallback.callback(isLiked);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getLikesCount(@NonNull PoliticsPost.SimpleCallback<Long> finishedCallback){
        Query query = firebaseDatabase.getReference().child("politicsPosts").child(id).child("likes");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long num = 0;
                if(snapshot.exists()){
                    num = snapshot.getChildrenCount();
                }
                finishedCallback.callback(num);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getCommentsCount(@NonNull PoliticsPost.SimpleCallback<Long> finishedCallback){
        Query query = firebaseDatabase.getReference().child("politicsPosts").child(id).child("comments");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long num = 0;
                if(snapshot.exists()){
                    num = snapshot.getChildrenCount();
                }
                finishedCallback.callback(num);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getOptions(@NonNull PoliticsPost.SimpleCallback<Map<String, Integer>> finishedCallback){
        Query query = firebaseDatabase.getReference().child("politicsPosts").child(id).child("options");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Integer> map = new HashMap<>();
                if(snapshot.exists()){
                    for (DataSnapshot snap : snapshot.getChildren()){
                        String name = snap.child("name").getValue().toString();
                        Query query1 = firebaseDatabase.getReference().child("politicsPosts").child(id).child("voters").orderByChild("votedFor").equalTo(name);
                        query1.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                int votes = (int) snapshot.getChildrenCount();
                                map.put(name, votes);
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });
                    }
                }
                finishedCallback.callback(map);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void hasVoted(@NonNull PoliticsPost.SimpleCallback<Boolean> finishedCallback){
        Query query = firebaseDatabase.getReference().child("politicsPosts").child(id).child("voters");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean hasVoted = false;
                for (DataSnapshot snap : snapshot.getChildren()){
                    String id = snap.child("voter").getValue().toString();
                    if (id.equals(email)){
                        hasVoted = true;
                        break;
                    }
                }
                finishedCallback.callback(hasVoted);
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
