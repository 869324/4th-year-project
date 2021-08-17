package com.dekut.dekutchat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.dekut.dekutchat.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class ViewVideo extends AppCompatActivity {
    VideoView videoView;
    ProgressBar progressBar;
    ImageButton btnBack;

    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    File localFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_video);

        videoView = findViewById(R.id.videoView);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);

        Bundle extras = getIntent().getExtras();
        String videoUrl = extras.getString("videoUrl");

        StorageReference reference = firebaseStorage.getReferenceFromUrl(videoUrl);
        try {
            localFile = File.createTempFile("images", "jpg");
            reference.getFile(localFile).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull FileDownloadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    int currentProgress = (int) progress;
                    progressBar.setProgress(currentProgress);
                }
            }).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    videoView.setVideoPath(localFile.getPath());
                    videoView.setMediaController(new MediaController(ViewVideo.this));
                    progressBar.setVisibility(View.INVISIBLE);
                    videoView.start();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        localFile.delete();
    }
}