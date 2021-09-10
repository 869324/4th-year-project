package com.dekut.dekutchat.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    PopupWindow attachPopup;
    int PICK_MEDIA_GALLERY = 1,PICK_IMAGE_CAMERA = 2, PICK_VIDEO_CAMERA = 3, CHOOSE_FILE = 4;
    int ACCESS_CAMERA1 = 5, ACCESS_CAMERA2 = 6, READ_STORAGE1 = 7, READ_STORAGE2 = 8, WRITE_STORAGE = 9;
    String currentPhotoPath;
    Uri selectedImage, videoUri, fileUri;

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
        imageUrl = extras.getString("url");

        fetchMessages();

        groupName.setText(name);

        if (imageUrl != null) {
            Glide.with(getApplicationContext())
                    .load(imageUrl)
                    .into(groupIcon);
        }

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

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etText.clearFocus();
                LayoutInflater inflater = (LayoutInflater) GroupChat.this.getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.attach_popup, null);
                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                //int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                attachPopup = new PopupWindow(popupView, width, 500, true);
                attachPopup.setElevation(10);
                attachPopup.showAsDropDown(btnAdd, 5, -30);

                ImageButton btnCamera = popupView.findViewById(R.id.btnCamera);
                ImageButton btnGallery = popupView.findViewById(R.id.btnGallery);
                //ImageButton btnFile = popupView.findViewById(R.id.btnFile);
                ImageButton btnCamcorder = popupView.findViewById(R.id.btnCamcorder);

                btnCamera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectMedia("camera");
                    }
                });

                btnGallery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (ContextCompat.checkSelfPermission(GroupChat.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){
                            ActivityCompat.requestPermissions(GroupChat.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE1);
                        }
                        else {
                            selectMedia("gallery");
                        }
                    }
                });

                /*btnFile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (ContextCompat.checkSelfPermission(GroupChat.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){
                            ActivityCompat.requestPermissions(GroupChat.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE2);
                        }
                        else {
                            selectMedia("file");
                        }
                    }
                });*/

                btnCamcorder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (ContextCompat.checkSelfPermission(GroupChat.this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_DENIED){
                            ActivityCompat.requestPermissions(GroupChat.this, new String[] {Manifest.permission.CAMERA}, ACCESS_CAMERA2);
                        }
                        else {
                            selectMedia("camcorder");
                        }
                    }
                });
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Intent intent = new Intent(GroupChat.this, LoadMedia.class);
            intent.putExtra("receiverId", groupId);
            intent.putExtra("senderId", email);
            intent.putExtra("receiverType", "group");
            intent.putExtra("convoId", groupId);

            if (requestCode == PICK_IMAGE_CAMERA) {
                try {
                    File file = new File(currentPhotoPath);
                    selectedImage = Uri.fromFile(file);
                    videoUri = null;
                    fileUri = null;

                    intent.putExtra("mediaUri", selectedImage.toString());
                    intent.putExtra("mediaType", "image");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (requestCode == PICK_MEDIA_GALLERY) {
                Uri selectedMediaUri = data.getData();
                if (selectedMediaUri.toString().contains("image")) {
                    selectedImage = selectedMediaUri;
                    videoUri = null;
                    fileUri = null;

                    intent.putExtra("mediaUri", selectedImage.toString());
                    intent.putExtra("mediaType", "image");

                } else  if (selectedMediaUri.toString().contains("video")) {
                    videoUri = selectedMediaUri;
                    selectedImage = null;
                    fileUri = null;

                    intent.putExtra("mediaUri", videoUri.toString());
                    intent.putExtra("mediaType", "video");
                }

            }

            if (requestCode == PICK_VIDEO_CAMERA) {
                try {
                    videoUri = data.getData();
                    selectedImage = null;
                    fileUri = null;

                    intent.putExtra("mediaUri", videoUri.toString());
                    intent.putExtra("mediaType", "video");

                }catch (Exception ex){

                }
            }

            if (requestCode == CHOOSE_FILE) {
                fileUri = data.getData();
                videoUri = null;
                selectedImage = null;

                intent.putExtra("mediaUri", fileUri.toString());
                intent.putExtra("mediaType", "file");
            }

            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == ACCESS_CAMERA1){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(ContextCompat.checkSelfPermission(GroupChat.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        File photoFile = null;

                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {

                        }

                        if (photoFile != null) {
                            Uri photoURI = FileProvider.getUriForFile(GroupChat.this, "com.example.android.fileprovider", photoFile);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(intent, PICK_IMAGE_CAMERA);
                        }
                    }
                }
                else {
                    ActivityCompat.requestPermissions(GroupChat.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_STORAGE);
                }
            }
            else {
                Toast.makeText(getApplicationContext(), "Camera Permissions Denied!", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == WRITE_STORAGE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(ContextCompat.checkSelfPermission(GroupChat.this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        File photoFile = null;

                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {

                        }

                        if (photoFile != null) {
                            Uri photoURI = FileProvider.getUriForFile(GroupChat.this, "com.example.android.fileprovider", photoFile);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(intent, PICK_IMAGE_CAMERA);
                        }
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Camera Permissions Denied!", Toast.LENGTH_SHORT).show();
                }

            }
            else {
                Toast.makeText(getApplicationContext(), "Storage Permissions Denied!", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == READ_STORAGE1){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectMedia("gallery");
            }else {
                Toast.makeText(getApplicationContext(), "Gallery Permissions Denied", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == READ_STORAGE2){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectMedia("file");
            }else {
                Toast.makeText(getApplicationContext(), "Camera Permissions Denied", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == ACCESS_CAMERA2){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectMedia("camcorder");
            }else {
                Toast.makeText(getApplicationContext(), "Camera Permissions Denied", Toast.LENGTH_SHORT).show();
            }
        }
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
        message.put("messageType", "text");

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
                            DatabaseReference reference1 = firebaseDatabase.getReference().child("groupConversations").child(groupId).child("lastMessageT");
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

    public void selectMedia(String source){
        if(source.equals("gallery")) {
            Intent mediaIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            mediaIntent.setType("image/* video/*");
            startActivityForResult(mediaIntent, PICK_MEDIA_GALLERY);
        }

        if (source.equals("camera")) {
            if (ContextCompat.checkSelfPermission(GroupChat.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(GroupChat.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        File photoFile = null;

                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {

                        }

                        if (photoFile != null) {
                            Uri photoURI = FileProvider.getUriForFile(GroupChat.this, "com.example.android.fileprovider", photoFile);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(intent, PICK_IMAGE_CAMERA);
                        }
                    }
                } else {
                    ActivityCompat.requestPermissions(GroupChat.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_STORAGE);
                }
            } else {
                ActivityCompat.requestPermissions(GroupChat.this, new String[]{Manifest.permission.CAMERA}, ACCESS_CAMERA1);
            }
        }

        if(source.equals("camcorder")){
            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takeVideoIntent, PICK_VIDEO_CAMERA);
            }
        }

        if(source.equals("file")){
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

            startActivityForResult(intent, CHOOSE_FILE);
        }
    }

    public File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

}