package com.dekut.dekutchat.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dekut.dekutchat.R;
import com.dekut.dekutchat.adapters.ConvoAdapter;
import com.dekut.dekutchat.adapters.GroupConvoAdapter;
import com.dekut.dekutchat.utils.Conversation;
import com.dekut.dekutchat.utils.Group;
import com.dekut.dekutchat.utils.Message;
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

public class GroupChat extends AppCompatActivity {

    TextView groupName, tvInfo;
    ImageButton btnBack, btnAdd, btnSend;
    ImageView groupIcon;
    EditText etText;
    RecyclerView dialogList;

    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    String groupId, name, imageUrl;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    List<Message> messageList = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    GroupConvoAdapter groupConvoAdapter;
    boolean found = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        groupName = findViewById(R.id.groupName);
        btnBack = findViewById(R.id.btnBack);
        groupIcon = findViewById(R.id.groupIcon);
        btnAdd = findViewById(R.id.btnAdd);
        btnSend = findViewById(R.id.btnSend);
        etText = findViewById(R.id.etText);
        dialogList = findViewById(R.id.dialogList);
        tvInfo = findViewById(R.id.tvInfo);

        Bundle extras = getIntent().getExtras();
        groupId = extras.getString("guid");
        name = extras.getString("name");
        imageUrl = extras.getString("imageUrl");

        fetchMessages();

        groupName.setText(name);
        Glide.with(getApplicationContext())
                .load(imageUrl)
                .into(groupIcon);

        Query query = firebaseDatabase.getReference().child("groups").orderByKey().equalTo(groupId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        Group group = snap.getValue(Group.class);
                        group.isJoined(new Group.SimpleCallback<Boolean>() {
                            @Override
                            public void callback(Boolean isJoined) {
                                if(!isJoined){
                                    tvInfo.setVisibility(View.VISIBLE);
                                    etText.setVisibility(View.INVISIBLE);
                                    btnAdd.setVisibility(View.INVISIBLE);
                                    btnSend.setVisibility(View.INVISIBLE);
                                }
                                else {
                                    tvInfo.setVisibility(View.INVISIBLE);
                                    etText.setVisibility(View.VISIBLE);
                                    btnAdd.setVisibility(View.VISIBLE);
                                    btnSend.setVisibility(View.VISIBLE);
                                }
                                btnSend.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String text = etText.getText().toString();
                                        if(!text.isEmpty()){
                                            sendMessage(text);
                                            etText.setText("");
                                        }
                                    }
                                });
                            }
                        });
                        break;
                    }
                }
                else {
                    onBackPressed();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });

        groupName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupChat.this, ViewGroup.class);
                intent.putExtra("guid", groupId);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void sendMessage(String text){
        Map<String, Object> message = new HashMap<>();
        message.put("text", text);
        message.put("senderId", email);
        message.put("receiverId", groupId);
        message.put("sentAt", ServerValue.TIMESTAMP);
        message.put("readAt", 0);

        DatabaseReference reference = firebaseDatabase.getReference().child("groupConversations").child(groupId).child("messages");
        DatabaseReference keyRef = reference.push();
        String key = keyRef.getKey();
        message.put("id", key);

        keyRef.setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()){
                    keyRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            Message message1 = snapshot.getValue(Message.class);
                            long timestamp = message1.getSentAt();
                            DatabaseReference reference1 = firebaseDatabase.getReference().child("groupConversations").child(groupId).child("lastMessage");
                            reference1.setValue(timestamp);
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });
                }
                else {
                    keyRef.removeValue();
                }
            }
        });
    }

    public void fetchMessages(){
        linearLayoutManager = new LinearLayoutManager(GroupChat.this);
        dialogList.setLayoutManager(linearLayoutManager);
        dialogList.setHasFixedSize(true);
        groupConvoAdapter = new GroupConvoAdapter(messageList, GroupChat.this, groupId);
        groupConvoAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        dialogList.setAdapter(groupConvoAdapter);

        Query query = firebaseDatabase.getReference().child("groupConversations").child(groupId).child("messages").orderByChild("sentAt");
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                messageList.add(message);
                groupConvoAdapter.notifyItemInserted(messageList.size() - 1);
                dialogList.smoothScrollToPosition(groupConvoAdapter.getItemCount() - 1);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                message.setConvoId(groupId);
                for(Message message1 : messageList){
                    String id1 = message.getId();
                    String id2 = message1.getId();
                    if(id1.equals(id2)) {
                        int index = messageList.indexOf(message1);
                        messageList.remove(index);
                        messageList.add(index, message);
                        groupConvoAdapter.notifyItemChanged(index);
                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}