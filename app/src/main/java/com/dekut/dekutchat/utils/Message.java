package com.dekut.dekutchat.utils;

import android.app.DownloadManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class Message {
    String text, senderId, receiverId, id;
    String messageType, imageUrl, videoUrl, fileUrl;
    long sentAt, readAt, mediaSize;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    String convoId;

    public Message(){}

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public long getSentAt() {
        return sentAt;
    }

    public void setSentAt(long sentAt) {
        this.sentAt = sentAt;
    }

    public long getReadAt() {
        return readAt;
    }

    public void setReadAt(long readAt) {
        this.readAt = readAt;
    }

    public void setConvoId(String convoId) {
        this.convoId = convoId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public void getGroupMessageId(@NonNull SimpleCallback<String> finishedCallback){
        Query query = firebaseDatabase.getReference().child("groupConversations").child(convoId).child("messages").orderByChild("sentAt").equalTo(sentAt);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String key = "";
                for (DataSnapshot snap : snapshot.getChildren()){
                    Message message = snap.getValue(Message.class);
                    if(message.getSenderId().equals(senderId)){
                        key = snap.getKey();
                        break;
                    }
                }
                finishedCallback.callback(key);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getSender(@NonNull SimpleCallback<Student> finishedCallback){
        Query query = firebaseDatabase.getReference().child("students").orderByChild("email").equalTo(senderId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Student student = null;
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        student = snap.getValue(Student.class);
                        break;
                    }
                }
                finishedCallback.callback(student);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void addReadAt(){
        Query query = firebaseDatabase.getReference().child("conversations").child(convoId).child("messages").orderByKey().equalTo(id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        String read = snap.child("readAt").getValue().toString();
                        if (read.equals("0")) {
                            DatabaseReference reference = query.getRef();
                            reference.child(id).child("readAt").setValue(ServerValue.TIMESTAMP);
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

    public void addGroupReadAt(){
        getGroupMessageId(new SimpleCallback<String>() {
            @Override
            public void callback(String id) {
                Query query = firebaseDatabase.getReference().child("groupConversations").child(convoId).child("messages").orderByKey().equalTo(id);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot snap : snapshot.getChildren()) {
                                String read = snap.child("readAt").getValue().toString();
                                if (read.equals("0")) {
                                    DatabaseReference reference = query.getRef();
                                    reference.child(id).child("readAt").setValue(ServerValue.TIMESTAMP);
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
        });

    }

    public void addLastRead(String email){
        DatabaseReference reference = firebaseDatabase.getReference().child("groups").child(convoId).child("members").child(email.replace(".", "_"));
        reference.child("lastRead").setValue(ServerValue.TIMESTAMP);
    }

    public void addReadListener(@NonNull SimpleCallback<Long> finishedCallback){
        Query query = firebaseDatabase.getReference().child("conversations").child(convoId).child("messages").orderByKey().equalTo(id);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Message message = snap.getValue(Message.class);
                    finishedCallback.callback(message.readAt);
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
