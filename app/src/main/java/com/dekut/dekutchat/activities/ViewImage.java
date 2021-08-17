package com.dekut.dekutchat.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.dekut.dekutchat.R;

import java.io.ByteArrayInputStream;
import java.util.concurrent.ExecutionException;

public class ViewImage extends AppCompatActivity {
    ImageView imageView;
    ProgressBar progressBar;
    byte[] bytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        imageView = findViewById(R.id.imageView);
        progressBar = findViewById(R.id.progressBar);

        Bundle extras = getIntent().getExtras();
        String url = extras.getString("url");
        progressBar.setVisibility(View.GONE);
        Glide.with(getApplicationContext())
                .load(url)
                .into(imageView);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}