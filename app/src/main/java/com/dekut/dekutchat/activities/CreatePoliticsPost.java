package com.dekut.dekutchat.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.dekut.dekutchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreatePoliticsPost extends AppCompatActivity {
    ImageButton btnCancel, btnCamera, btnGallery, btnCamcorder, btnPlay, btnPoll;
    Button btnPost;
    FloatingActionButton btnCancelImage;
    ImageView imageView;
    EditText etText;
    CardView playCard;
    ProgressDialog progressDialog;
    VideoView videoView;

    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    String postText, type = "text";
    byte[] bytes;
    File image;
    String imageUrl, currentPhotoPath, videoUrl;
    int PICK_MEDIA_GALLERY = 1,PICK_IMAGE_CAMERA = 2, PICK_VIDEO_CAMERA = 3, CHOOSE_FILE = 4;
    int ACCESS_CAMERA1 = 5, ACCESS_CAMERA2 = 6, READ_STORAGE1 = 7, WRITE_STORAGE = 8;
    Bitmap bitmap, thumbnail;
    Uri selectedImage, videoUri;
    int imgHeight, compressionRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_politics_post);

        btnCancel = findViewById(R.id.btnCancel);
        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);
        btnPost = findViewById(R.id.btnPost);
        btnCancelImage = findViewById(R.id.btnCancelImage);
        imageView = findViewById(R.id.imageView);
        etText = findViewById(R.id.etText);
        btnCamcorder = findViewById(R.id.btnCamcorder);
        videoView = findViewById(R.id.videoView);
        btnPlay = findViewById(R.id.btnPlay);
        playCard = findViewById(R.id.playCard);
        btnPoll = findViewById(R.id.btnPoll);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });

        btnCancelImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageDrawable(null);
                videoView.stopPlayback();
                videoView.setVisibility(View.INVISIBLE);
                selectedImage = null;
                videoUri = null;
                playCard.setVisibility(View.INVISIBLE);
                try{
                    image.delete();
                }catch (Exception ex){}
            }
        });

        btnPoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CreatePoliticsPost.this, CreatePoll.class);
                startActivity(intent);
                finish();
            }
        });

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(CreatePoliticsPost.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){
                    ActivityCompat.requestPermissions(CreatePoliticsPost.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE1);
                }
                else {
                    selectMedia("gallery");
                }

            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectMedia("camera");
            }
        });

        btnCamcorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(CreatePoliticsPost.this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_DENIED){
                    ActivityCompat.requestPermissions(CreatePoliticsPost.this, new String[] {Manifest.permission.CAMERA}, ACCESS_CAMERA2);
                }
                else {
                    selectMedia("camcorder");
                }
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgHeight = imageView.getMeasuredHeight() - 60;
                playCard.setVisibility(View.INVISIBLE);
                imageView.setVisibility(View.INVISIBLE);
                videoView.setVisibility(View.VISIBLE);
                videoView.start();
            }
        });

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPost.setEnabled(false);
                postText = etText.getText().toString();
                videoView.stopPlayback();
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                        View view = getCurrentFocus();
                        if (view == null) {
                            view = new View(CreatePoliticsPost.this);
                        }
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                });

                thread.start();

                if (postText.isEmpty() && imageView.getDrawable() == null) {
                    Toast.makeText(getApplicationContext(), "You have nothing to post", Toast.LENGTH_SHORT).show();
                }
                else {
                    progressDialog = new ProgressDialog(CreatePoliticsPost.this);
                    progressDialog.show();
                    progressDialog.setContentView(R.layout.upload_progress);
                    progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    progressDialog.setCancelable(false);
                    TextView textView = progressDialog.findViewById(R.id.textView);

                    if (!postText.isEmpty() && imageView.getDrawable() == null) {
                        uploadData();
                    }

                    if (selectedImage != null && videoUri == null) {
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(CreatePoliticsPost.this.getContentResolver(), selectedImage);
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

                            bitmap.compress(Bitmap.CompressFormat.WEBP, compressionRate, bout);
                            bytes = bout.toByteArray();
                            imgHeight = imageView.getMeasuredHeight() - 60;
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    if (videoUri != null) {
                        try {
                            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
                            thumbnail = drawable.getBitmap();
                            ByteArrayOutputStream bout = new ByteArrayOutputStream();

                            thumbnail.compress(Bitmap.CompressFormat.WEBP, 100, bout);
                            bytes = bout.toByteArray();
                            imgHeight = imageView.getMeasuredHeight() - 60;
                        } catch (Exception ex) {

                        }
                    }

                    if (bytes != null) {
                        StorageReference ref = firebaseStorage.getReference().child("politicsPics/" + UUID.randomUUID().toString());
                        UploadTask uploadTask = ref.putBytes(bytes);
                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        imageUrl = uri.toString();
                                        bytes = null;
                                        try {
                                            image.delete();
                                        }catch (Exception ex){

                                        }
                                        if(videoUri == null) {
                                            uploadData();
                                        }
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

                    if (videoUri != null) {
                        try {
                            StorageReference ref = firebaseStorage.getReference().child("politicsVideos/" + UUID.randomUUID().toString());
                            UploadTask uploadTask = ref.putFile(videoUri);
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
                                            uploadData();
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

                    btnPost.setEnabled(true);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == ACCESS_CAMERA1){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(ContextCompat.checkSelfPermission(CreatePoliticsPost.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        File photoFile = null;

                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {

                        }

                        if (photoFile != null) {
                            Uri photoURI = FileProvider.getUriForFile(CreatePoliticsPost.this, "com.example.android.fileprovider", photoFile);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(intent, PICK_IMAGE_CAMERA);
                        }
                    }
                }
                else {
                    ActivityCompat.requestPermissions(CreatePoliticsPost.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_STORAGE);
                }
            }
            else {
                Toast.makeText(getApplicationContext(), "Camera Permissions Denied!", Toast.LENGTH_SHORT).show();
            }
        }

        else if(requestCode == WRITE_STORAGE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(ContextCompat.checkSelfPermission(CreatePoliticsPost.this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        File photoFile = null;

                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {

                        }

                        if (photoFile != null) {
                            Uri photoURI = FileProvider.getUriForFile(CreatePoliticsPost.this, "com.example.android.fileprovider", photoFile);
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

        else if(requestCode == ACCESS_CAMERA2){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectMedia("camcorder");
            }else {
                Toast.makeText(getApplicationContext(), "Camera Permissions Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == PICK_IMAGE_CAMERA) {
                try {
                    File file = new File(currentPhotoPath);
                    Log.e("myTag", file.getAbsolutePath());
                    playCard.setVisibility(View.INVISIBLE);
                    selectedImage = Uri.fromFile(file);
                    imageView.setImageURI(selectedImage);
                    videoUri = null;
                    thumbnail = null;
                    videoView.setVisibility(View.INVISIBLE);
                    videoView.setVideoURI(null);
                    imageView.setVisibility(View.VISIBLE);
                    type = "media";
                } catch (Exception e) {
                }
            }

            else if (requestCode == PICK_MEDIA_GALLERY) {
                try {
                    Uri selectedMediaUri = data.getData();
                    if (selectedMediaUri.toString().contains("image")) {
                        selectedImage = selectedMediaUri;
                        videoUri = null;
                        playCard.setVisibility(View.INVISIBLE);
                        imageView.setImageURI(selectedImage);
                        videoUri = null;
                        thumbnail = null;
                        videoView.setVisibility(View.INVISIBLE);
                        videoView.setVideoURI(null);
                        imageView.setVisibility(View.VISIBLE);
                    } else if (selectedMediaUri.toString().contains("video")) {
                        videoUri = selectedMediaUri;
                        selectedImage = null;
                        imageView.setImageDrawable(null);
                        selectedImage = null;
                        videoView.setVideoURI(videoUri);
                        videoView.setMediaController(new MediaController(this));

                        Glide.with(this)
                                .asBitmap()
                                .load(videoUri)
                                .into(imageView);
                        imageView.setVisibility(View.VISIBLE);
                        playCard.setVisibility(View.VISIBLE);
                    }
                    type = "media";
                }catch (Exception ex) {}
            }

            else if (requestCode == PICK_VIDEO_CAMERA) {
                try {
                    videoUri = data.getData();
                    selectedImage = null;
                    imageView.setImageDrawable(null);
                    selectedImage = null;
                    videoView.setVideoURI(videoUri);
                    videoView.setMediaController(new MediaController(this));

                    Glide.with(this)
                            .asBitmap()
                            .load(videoUri)
                            .into(imageView);
                    imageView.setVisibility(View.VISIBLE);
                    playCard.setVisibility(View.VISIBLE);
                    type = "media";

                }catch (Exception ex){

                }
            }

        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    public void selectMedia(String source){
        if(source.equals("gallery")) {
            Intent mediaIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            mediaIntent.setType("image/* video/*");
            startActivityForResult(mediaIntent, PICK_MEDIA_GALLERY);
        }

        if (source.equals("camera")) {
            if (ContextCompat.checkSelfPermission(CreatePoliticsPost.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(CreatePoliticsPost.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        File photoFile = null;

                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {

                        }

                        if (photoFile != null) {
                            Uri photoURI = FileProvider.getUriForFile(CreatePoliticsPost.this, "com.example.android.fileprovider", photoFile);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(intent, PICK_IMAGE_CAMERA);
                        }
                    }
                } else {
                    ActivityCompat.requestPermissions(CreatePoliticsPost.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_STORAGE);
                }
            } else {
                ActivityCompat.requestPermissions(CreatePoliticsPost.this, new String[]{Manifest.permission.CAMERA}, ACCESS_CAMERA1);
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

    public void uploadData() {
        Map<String, Object> map = new HashMap();
        map.put("poster", email);
        if (imageUrl != null) {
            map.put("imageUrl", imageUrl);
        }
        if (videoUrl != null) {
            map.put("videoUrl", videoUrl);
        }
        map.put("text", postText);
        map.put("timestamp", ServerValue.TIMESTAMP);
        map.put("imgHeight", imgHeight);
        map.put("type", type);

        DatabaseReference reference = firebaseDatabase.getReference().child("politicsPosts");
        DatabaseReference keyRef = reference.push();
        String key = keyRef.getKey();
        map.put("id", key);
        keyRef.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()){
                    progressDialog.dismiss();
                    onBackPressed();
                    finish();
                }
                else{
                    keyRef.removeValue();
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Failed to post. Try again later", Toast.LENGTH_LONG).show();
                }
            }
        });
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