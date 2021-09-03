package com.dekut.dekutchat.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dekut.dekutchat.R;
import com.dekut.dekutchat.utils.Conversation;
import com.dekut.dekutchat.utils.Group;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreateGroup extends AppCompatActivity {
    
    EditText etGroupName, etPassword, etDescription;
    Button btnCreate;
    ImageButton btnEditPic;
    ImageView imageView;
    RadioGroup radioGroup;
    ProgressDialog progressDialog;
    TextView tvInfo;

    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    int pickImageCamera = 1, pickImageGallery = 2, requestCamera = 3, requestGallery = 4;
    Uri selectedImage;
    String groupName, description, currentPhotoPath, imageUrl, type, password = "";
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    StorageReference storageReference;
    int compressionRate;
    Bitmap bitmap;
    byte[] bytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        
        etGroupName = findViewById(R.id.etGroupName);
        etPassword = findViewById(R.id.etPassword);
        etDescription = findViewById(R.id.etDescription);
        btnCreate = findViewById(R.id.btnCreate);
        btnEditPic = findViewById(R.id.btnEditPic);
        imageView = findViewById(R.id.imageView);
        radioGroup = findViewById(R.id.radioGroup);
        tvInfo = findViewById(R.id.tvInfo);
        
        btnEditPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnCreate.setEnabled(false);
                groupName = etGroupName.getText().toString();
                password = etPassword.getText().toString();
                description = etDescription.getText().toString();
                int checkedId = radioGroup.getCheckedRadioButtonId();

                if(groupName.isEmpty()){
                    etGroupName.setError("Enter group name");
                    etGroupName.requestFocus();
                    btnCreate.setEnabled(true);
                }

                else if(checkedId == -1){
                    Toast.makeText(getApplicationContext(), "Select group type", Toast.LENGTH_SHORT).show();
                    btnCreate.setEnabled(true);
                }

                else{
                    progressDialog = new ProgressDialog(CreateGroup.this);
                    progressDialog.show();
                    progressDialog.setContentView(R.layout.progress_dialog);
                    progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    progressDialog.setCancelable(false);

                    if (checkedId == 1){
                        type = "public";
                    }

                    if (checkedId == 2){
                        type = "private";
                    }

                    Query query = firebaseDatabase.getReference().child("groups").orderByChild("name").equalTo(groupName);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                Toast.makeText(getApplicationContext(), "There is another group with this name", Toast.LENGTH_LONG).show();
                            }
                            else {
                                if(selectedImage != null) {
                                    try {
                                        bitmap = MediaStore.Images.Media.getBitmap(CreateGroup.this.getContentResolver(), selectedImage);
                                        long sizeKb = bitmap.getByteCount() / 1024;
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
                                        bitmap.compress(Bitmap.CompressFormat.WEBP, 25, bout);
                                        bytes = bout.toByteArray();

                                        storageReference = firebaseStorage.getReference().child("groupPics/" + UUID.randomUUID().toString());
                                        UploadTask uploadTask = storageReference.putBytes(bytes);
                                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        imageUrl = uri.toString();
                                                        uploadData();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        storageReference.delete();
                                                        btnCreate.setEnabled(true);
                                                        progressDialog.dismiss();
                                                        Toast.makeText(getApplicationContext(), "Failed to create group. Try Again Later", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                bitmap = null;
                                                bytes = null;
                                                btnCreate.setEnabled(true);
                                                progressDialog.dismiss();
                                                Toast.makeText(getApplicationContext(), "Failed to create group. Try Again Later", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }
                                else {
                                    uploadData();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == pickImageCamera) {
            try {
                File file = new File(currentPhotoPath);
                selectedImage = Uri.fromFile(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        else if (requestCode == pickImageGallery) {
            selectedImage = data.getData();
        }
        imageView.setImageURI(selectedImage);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == requestCamera){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, pickImageCamera);
            }
            else {
                Toast.makeText(getApplicationContext(), "Camera Permissions Denied!", Toast.LENGTH_SHORT).show();
            }
        }

        if(requestCode == requestGallery){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, pickImageGallery);
            } else {
                Toast.makeText(this, "Gallery Permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void uploadData(){
        DatabaseReference reference = firebaseDatabase.getReference().child("groups");
        Map<String, Object> group = new HashMap<>();
        group.put("name", groupName);
        group.put("imageUrl", imageUrl);
        group.put("description", description);
        group.put("password", password);
        group.put("type", type);
        group.put("timestamp", ServerValue.TIMESTAMP);
        group.put("creator", email);

        reference.push().setValue(group, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                String key = ref.getKey();
                DatabaseReference reference1 = reference.child(key);
                reference1.child("groupId").setValue(key).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Map<String, Object> map = new HashMap<>();
                            map.put("id", email);
                            map.put("joinedAt", ServerValue.TIMESTAMP);
                            map.put("lastRead", 0l);
                            reference1.child("members").child(email.replace(".", "_")).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){

                                        DatabaseReference reference2 = reference1.child("admins").child(email.replace(".", "_"));
                                        reference2.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    DatabaseReference reference2 = firebaseDatabase.getReference().child("groupConversations");
                                                    Map<String, Object> conversation = new HashMap<>();
                                                    conversation.put("convoId", key);
                                                    conversation.put("lastMessageT", ServerValue.TIMESTAMP);
                                                    reference2.child(key).setValue(conversation).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                Toast.makeText(getApplicationContext(), "Group created", Toast.LENGTH_LONG).show();
                                                                Intent intent = new Intent(getApplicationContext(), ViewGroup.class);
                                                                intent.putExtra("guid", key);
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                            else{
                                                                Toast.makeText(getApplicationContext(), "Failed to create group. Try Again Later", Toast.LENGTH_SHORT).show();
                                                                reference1.removeValue();
                                                                storageReference.delete();
                                                            }
                                                        }
                                                    });
                                                }
                                                else {
                                                    Toast.makeText(getApplicationContext(), "Failed to create group. Try Again Later", Toast.LENGTH_SHORT).show();
                                                    reference1.removeValue();
                                                    storageReference.delete();
                                                }
                                            }
                                        });

                                    }else{
                                        Toast.makeText(getApplicationContext(), "Failed to create group. Try Again Later", Toast.LENGTH_SHORT).show();
                                        reference1.removeValue();
                                        storageReference.delete();
                                    }
                                }
                            });
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Failed to create group. Try Again Later", Toast.LENGTH_SHORT).show();
                            reference1.removeValue();
                            storageReference.delete();
                        }
                    }
                });

            }
        });
    }

    public void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose From Gallery","Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateGroup.this);
        builder.setTitle("Select Option");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    dialog.dismiss();
                    if (ContextCompat.checkSelfPermission(CreateGroup.this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_DENIED){
                        ActivityCompat.requestPermissions(CreateGroup.this, new String[] {Manifest.permission.CAMERA}, requestCamera);
                    }
                    else {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            File photoFile = null;

                            try {
                                photoFile = createImageFile();
                            } catch (IOException ex) {

                            }

                            if (photoFile != null) {
                                Uri photoURI = FileProvider.getUriForFile(CreateGroup.this, "com.example.android.fileprovider", photoFile);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                startActivityForResult(intent, pickImageCamera);
                            }
                        }
                    }

                } else if (options[item].equals("Choose From Gallery")) {
                    dialog.dismiss();
                    if (ContextCompat.checkSelfPermission(CreateGroup.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){
                        ActivityCompat.requestPermissions(CreateGroup.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, requestGallery);
                    }
                    else {
                        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(pickPhoto, pickImageGallery);
                    }

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
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