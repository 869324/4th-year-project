package com.dekut.dekutchat.utils;

import android.app.DownloadManager;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Group {
    String groupId, name, description, password, imageUrl, type;
    String creator;
    long timestamp;
    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    public Group() {
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void isJoined(@NonNull SimpleCallback<Boolean> finishedCallback){
        Query query = firebaseDatabase.getReference().child("groups").child(groupId).child("members").orderByChild("id").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean status = false;
                if(snapshot.exists()){
                    status = true;
                }
                finishedCallback.callback(status);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void isAdmin(@NonNull SimpleCallback<Boolean> finishedCallback){
        Query query = firebaseDatabase.getReference().child("groups").child(groupId).child("admins").orderByChild("id").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean status = false;
                if(snapshot.exists()){
                    status = true;
                }
                finishedCallback.callback(status);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getMembersCount(@NonNull SimpleCallback<Long> finishedCallback){
        if (groupId != null) {
            Query query = firebaseDatabase.getReference().child("groups").child(groupId).child("members");
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long num = 0;
                    if (snapshot.exists()) {
                        num = snapshot.getChildrenCount();
                    }

                    finishedCallback.callback(num);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    public void getAdminCount(@NonNull SimpleCallback<Long> finishedCallback){
        Query query = firebaseDatabase.getReference().child("groups").child(groupId).child("admins");
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

    public interface SimpleCallback<T>{
        void callback(T data);
    }
}
