package com.dekut.dekutchat.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Conversation {
    String  convoId, user1, user2;
    long lastMessageT;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    Message lastMessage;
    Group group;
    Student receiver;
    String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

    public Conversation(){}

    public String getConvoId() {
        return convoId;
    }

    public void setConvoId(String convoId) {
        this.convoId = convoId;
    }

    public String getUser1() {
        return user1;
    }

    public void setUser1(String user1) {
        this.user1 = user1;
    }

    public String getUser2() {
        return user2;
    }

    public void setUser2(String user2) {
        this.user2 = user2;
    }

    public long getLastMessageT() {
        return lastMessageT;
    }

    public void setLastMessageT(long lastMessageT) {
        this.lastMessageT = lastMessageT;
    }

    public void getConversationWith(@NonNull SimpleCallback<Student> finishedCallback){
        String receiverId;
        if(user1.equals(userEmail)){
            receiverId = user2;
        }
        else {
            receiverId = user1;
        }
        Query query = firebaseDatabase.getReference().child("students").orderByChild("email").equalTo(receiverId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap : snapshot.getChildren()){
                    receiver = snap.getValue(Student.class);
                    finishedCallback.callback(receiver);
                    break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getLastMessage(@NonNull SimpleCallback<Message> finishedCallback){
        Query query = firebaseDatabase.getReference().child("conversations").child(convoId).child("messages").limitToLast(1);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap : snapshot.getChildren()){
                    lastMessage = snap.getValue(Message.class);
                    finishedCallback.callback(lastMessage);
                    break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getGroupLastMessage(@NonNull SimpleCallback<Message> finishedCallback){
        Query query = firebaseDatabase.getReference().child("groupConversations").child(convoId).child("messages").limitToLast(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap : snapshot.getChildren()){
                    lastMessage = snap.getValue(Message.class);
                    finishedCallback.callback(lastMessage);
                    break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getGroupLastRead(@NonNull SimpleCallback<Long> finishedCallback){
        Query query = firebaseDatabase.getReference().child("groups").child(convoId).child("members").orderByChild("id").equalTo(userEmail);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        try {
                            long timestamp = (long) snap.child("lastRead").getValue();
                            finishedCallback.callback(timestamp);
                        }catch (Exception ex){
                            finishedCallback.callback((long) 0);
                        }
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getGroup(@NonNull SimpleCallback<Group> finishedCallback){
        Query query = firebaseDatabase.getReference().child("groups").orderByChild("groupId").equalTo(convoId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Group group = null;
                for(DataSnapshot snap : snapshot.getChildren()){
                    group = snap.getValue(Group.class);
                    break;
                }
                finishedCallback.callback(group);
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
