package com.dekut.dekutchat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dekut.dekutchat.R;
import com.dekut.dekutchat.utils.Conversation;
import com.dekut.dekutchat.utils.Message;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoadMedia extends AppCompatActivity {
    ImageButton btnBack, btnSend, btnPlay;
    ImageView imageView;
    EditText etText;
    CardView playCard;
    TextView tvFileName;

    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    ProgressDialog progressDialog;
    Uri mediaUri;
    String videoUrl, imageUrl, fileUrl, text;
    Bitmap image, thumbnail;
    String mediaType, receiverType, receiverId, senderId, convoId;
    int compressionRate;
    byte[] imageBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_media);

        btnBack = findViewById(R.id.btnBack);
        btnSend = findViewById(R.id.btnSend);
        imageView = findViewById(R.id.imageView);
        etText = findViewById(R.id.etText);
        playCard = findViewById(R.id.playCard);
        btnPlay = findViewById(R.id.btnPlay);
        tvFileName = findViewById(R.id.tvFileName);

        Bundle extras = getIntent().getExtras();
        mediaUri = Uri.parse(extras.getString("mediaUri"));
        mediaType = extras.getString("mediaType");
        senderId = extras.getString("senderId");
        receiverId = extras.getString("receiverId");
        receiverType = extras.getString("receiverType");
        convoId = extras.getString("convoId");

        Drawable drawable1 = btnSend.getDrawable();
        Drawable drawable2 = btnBack.getDrawable();
        DrawableCompat.setTint(drawable1, ContextCompat.getColor(LoadMedia.this, R.color.primaryColor));
        DrawableCompat.setTint(drawable2, ContextCompat.getColor(LoadMedia.this, R.color.black));

        if(mediaType.equals("image")){
            imageView.setImageURI(mediaUri);
        }

        if(mediaType.equals("video")){
            Glide.with(this)
                    .asBitmap()
                    .load(mediaUri)
                    .into(imageView);

            playCard.setVisibility(View.VISIBLE);
        }

        if(mediaType.equals("file")){
            imageView.setImageDrawable(ContextCompat.getDrawable(LoadMedia.this, R.drawable.ic_file));
            tvFileName.setText(getFileName(mediaUri));
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnSend.setEnabled(false);
                text = etText.getText().toString();

                progressDialog = new ProgressDialog(LoadMedia.this);
                progressDialog.show();
                progressDialog.setContentView(R.layout.upload_progress);
                progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                progressDialog.setCancelable(false);
                TextView textView = progressDialog.findViewById(R.id.textView);

                if (mediaType.equals("image")) {
                    try {
                        image = MediaStore.Images.Media.getBitmap(LoadMedia.this.getContentResolver(), mediaUri);
                        long sizeKb = image.getByteCount() / 1024;
                        if (sizeKb <= 200) {
                            compressionRate = 100;
                        } else if (sizeKb <= 500) {
                            compressionRate = 90;
                        } else if (sizeKb <= 1000) {
                            compressionRate = 70;
                        } else if (sizeKb < 2000) {
                            compressionRate = 50;
                        } else if (sizeKb < 4000) {
                            compressionRate = 25;
                        } else {
                            compressionRate = 15;
                        }
                        ByteArrayOutputStream bout = new ByteArrayOutputStream();

                        image.compress(Bitmap.CompressFormat.WEBP, compressionRate, bout);
                        imageBytes = bout.toByteArray();
                        uploadImage();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                if (mediaType.equals("video")) {
                    try {
                        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
                        thumbnail = drawable.getBitmap();
                        ByteArrayOutputStream bout = new ByteArrayOutputStream();

                        thumbnail.compress(Bitmap.CompressFormat.WEBP, 100, bout);
                        imageBytes = bout.toByteArray();

                        StorageReference ref = firebaseStorage.getReference().child("messageVideos/" + UUID.randomUUID().toString());
                        UploadTask uploadTask = ref.putFile(mediaUri);
                        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                                int currentProgress = (int) progress;
                                textView.setVisibility(View.VISIBLE);
                                textView.setText(String.valueOf(currentProgress + "%"));
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        videoUrl = uri.toString();
                                        uploadImage();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        ref.delete();
                                        Toast.makeText(getApplicationContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                if (mediaType.equals("file")){
                    StorageReference ref = firebaseStorage.getReference().child("messageFiles/" + UUID.randomUUID().toString());
                    UploadTask uploadTask = ref.putFile(mediaUri);
                    uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                            int currentProgress = (int) progress;
                            textView.setVisibility(View.VISIBLE);
                            textView.setText(String.valueOf(currentProgress + "%"));
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    fileUrl = uri.toString();
                                    sendMessage();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    ref.delete();
                                    Toast.makeText(getApplicationContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                btnSend.setEnabled(true);
            }

        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void uploadImage(){
        if (imageBytes != null) {
            StorageReference ref = firebaseStorage.getReference().child("messagePics/" + UUID.randomUUID().toString());
            UploadTask uploadTask = ref.putBytes(imageBytes);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            imageUrl = uri.toString();
                            sendMessage();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            ref.delete();
                            Toast.makeText(getApplicationContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    public void sendMessage(){
        Map<String, Object> message = new HashMap<>();
        if (!text.isEmpty()) {
            message.put("text", text);
        }

        if (mediaType.equals("image")) {
            message.put("imageUrl", imageUrl);
        }

        if (mediaType.equals("video")) {
            message.put("videoUrl", videoUrl);
            message.put("imageUrl", imageUrl);
        }

        if (mediaType.equals("file")) {
            message.put("fileUrl", fileUrl);
        }

        message.put("senderId", senderId);
        message.put("receiverId", receiverId);
        message.put("sentAt", ServerValue.TIMESTAMP);
        message.put("readAt", 0);
        message.put("messageType", mediaType);

        if (receiverType.equals("user")) {
            Query query = firebaseDatabase.getReference().child("conversations").orderByChild("convoId").equalTo(convoId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        uploadData(message);
                    }
                    else {
                        DatabaseReference reference = firebaseDatabase.getReference().child("conversations").child(convoId);
                        Conversation conversation = new Conversation();
                        conversation.setConvoId(convoId);
                        conversation.setUser1(senderId);
                        conversation.setUser2(receiverId);
                        reference.setValue(conversation).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    uploadData(message);

                                }
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        else {
            uploadData(message);
        }
    }

    public void uploadData(Map<String, Object> message){
        DatabaseReference reference;
        if (receiverType.equals("user")) {
            reference = firebaseDatabase.getReference().child("conversations").child(convoId).child("messages");
        }
        else {
            reference = firebaseDatabase.getReference().child("groupConversations").child(convoId).child("messages");
        }

        DatabaseReference keyRef = reference.push();
        String key = keyRef.getKey();
        message.put("id", key);
        keyRef.setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                Query query = reference.orderByKey().equalTo(key);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Message message1 = snap.getValue(Message.class);
                            long timestamp = message1.getSentAt();

                            DatabaseReference reference1;
                            if (receiverType.equals("user")) {
                                reference1 = firebaseDatabase.getReference().child("conversations").child(convoId).child("lastMessage");
                            }
                            else {
                                reference1 = firebaseDatabase.getReference().child("groupConversations").child(convoId).child("lastMessage");
                            }
                            reference1.setValue(timestamp);
                            onBackPressed();
                            break;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    private String getFileName(Uri uri) throws IllegalArgumentException {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        if (cursor.getCount() <= 0) {
            cursor.close();
            throw new IllegalArgumentException("Can't obtain file name, cursor is empty");
        }

        cursor.moveToFirst();

        String fileName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));

        cursor.close();

        return fileName;
    }
}