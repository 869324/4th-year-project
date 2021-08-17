package com.dekut.dekutchat.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.Model;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.sql.Timestamp;
import java.util.Map;
import java.time.LocalDateTime;

public class HomePost {
    String id, poster, text, imageUrl, videoUrl;
    long timestamp;
    int imgHeight;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

    public HomePost(){

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

    public void getStudent(@NonNull SimpleCallback<Student> finishedCallback){
        Query query = firebaseDatabase.getReference().child("students").orderByChild("email").equalTo(poster);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        Student student = snap.getValue(Student.class);
                        finishedCallback.callback(student);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void isLiked(@NonNull SimpleCallback<Boolean> finishedCallback){
        Query query = firebaseDatabase.getReference().child("homePosts").child(id).child("likes").orderByChild("id").equalTo(email);
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

    public void getLikesCount(@NonNull SimpleCallback<Long> finishedCallback){
        Query query = firebaseDatabase.getReference().child("homePosts").child(id).child("likes");
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

    public void getCommentsCount(@NonNull SimpleCallback<Long> finishedCallback){
        Query query = firebaseDatabase.getReference().child("homePosts").child(id).child("comments");
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

    public interface SimpleCallback<T> {
        void callback(T data);
    }
}
