package com.dekut.dekutchat.activities;

import androidx.annotation.NonNull;
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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dekut.dekutchat.R;
import com.dekut.dekutchat.utils.Student;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.io.ByteArrayOutputStream;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateProfile extends AppCompatActivity {
    ImageView profilePic;
    TextView tvTop;
    EditText etRegNo, etUsername;
    Button btnSave, btnEditPic;
    SearchableSpinner searchableSpinner;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    DatabaseReference reference;
    StorageReference storageReference;
    Student student;

    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    String regNo, userName, course, imageUrl;
    Bitmap bitmap;
    Uri selectedImage;
    int pickImageCamera = 1, pickImageGallery = 2, requestCamera = 3, requestGallery = 4, requestWriteStorage = 5;
    ProgressDialog progressDialog;
    byte[] bytes;
    String currentPhotoPath;
    int compressionRate;
    File image, file;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        profilePic = findViewById(R.id.profilePic);
        searchableSpinner = findViewById(R.id.searchSpinner);
        etRegNo = findViewById(R.id.etRegNo);
        etUsername = findViewById(R.id.etUsername);
        btnSave = findViewById(R.id.btnSave);
        btnEditPic = findViewById(R.id.btnEditPic);
        tvTop = findViewById(R.id.tvTop);

        Resources res = getResources();
        String[] courses = res.getStringArray(R.array.courses);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, courses);
        searchableSpinner.setAdapter(adapter);
        searchableSpinner.setTitle("Select Course");

        btnEditPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSave.setEnabled(false);
                userName = etUsername.getText().toString();
                regNo = etRegNo.getText().toString().toUpperCase();
                course = searchableSpinner.getSelectedItem().toString();
                Pattern pattern = Pattern.compile("[A-Z]\\d{3,}-0([12])-\\d{4}/(\\d{4})");
                Matcher matcher = pattern.matcher(regNo);

                if (regNo.isEmpty()) {
                    etRegNo.setError("Enter RegNo");
                    etRegNo.requestFocus();
                    btnSave.setEnabled(true);
                }

                else if (userName.isEmpty()) {
                    etUsername.setError("Enter Username");
                    etUsername.requestFocus();
                    btnSave.setEnabled(true);
                }

                else if (course.equals("Select Course")) {
                    Toast.makeText(getApplicationContext(), "Select valid course", Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                }

                else if (!matcher.matches()) {
                    etRegNo.setError("Enter valid RegNo");
                    etRegNo.requestFocus();
                    btnSave.setEnabled(true);
                } else {
                    progressDialog = new ProgressDialog(CreateProfile.this);
                    progressDialog.show();
                    progressDialog.setContentView(R.layout.progress_dialog);
                    progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                    if(bitmap != null){
                        compress();
                        if (bytes != null) {
                            storageReference = firebaseStorage.getReference().child("profilePics/" + UUID.randomUUID().toString());
                            UploadTask uploadTask = storageReference.putBytes(bytes);
                            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            imageUrl = uri.toString();
                                            uploadData(imageUrl);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            storageReference.delete();
                                            uploadData(imageUrl);
                                            Toast.makeText(getApplicationContext(), "Failed to Upload Image", Toast.LENGTH_LONG).show();
                                        }
                                    });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    uploadData(imageUrl);
                                    Toast.makeText(getApplicationContext(), "Failed to Upload Image", Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            uploadData(imageUrl);
                        }
                    }
                    else {
                        uploadData(imageUrl);
                    }
                    btnSave.setEnabled(true);
                }
                btnSave.setEnabled(true);

            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == pickImageCamera) {
            try {
                file = new File(currentPhotoPath);
                selectedImage = Uri.fromFile(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        else if (requestCode == pickImageGallery) {
            try {
                selectedImage = data.getData();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        profilePic.setImageURI(selectedImage);
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == requestCamera){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(ContextCompat.checkSelfPermission(CreateProfile.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        File photoFile = null;

                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {

                        }

                        if (photoFile != null) {
                            Uri photoURI = FileProvider.getUriForFile(CreateProfile.this, "com.example.android.fileprovider", photoFile);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(intent, pickImageCamera);
                        }
                    }
                }
                else {
                    ActivityCompat.requestPermissions(CreateProfile.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestWriteStorage);
                }
            }
            else {
                Toast.makeText(getApplicationContext(), "Camera Permissions Denied!", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == requestWriteStorage){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(ContextCompat.checkSelfPermission(CreateProfile.this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        File photoFile = null;

                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {

                        }

                        if (photoFile != null) {
                            Uri photoURI = FileProvider.getUriForFile(CreateProfile.this, "com.example.android.fileprovider", photoFile);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(intent, pickImageCamera);
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

        else if(requestCode == requestGallery){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, pickImageGallery);
            } else {
                Toast.makeText(this, "Gallery Permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try{
            file.delete();
        }catch (Exception ex){

        }
    }

    public void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose From Gallery","Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateProfile.this);
        builder.setTitle("Select Option");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    dialog.dismiss();
                    if (ContextCompat.checkSelfPermission(CreateProfile.this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
                        if (ContextCompat.checkSelfPermission(CreateProfile.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if (intent.resolveActivity(getPackageManager()) != null) {
                                File photoFile = null;

                                try {
                                    photoFile = createImageFile();
                                } catch (IOException ex) {

                                }

                                if (photoFile != null) {
                                    Uri photoURI = FileProvider.getUriForFile(CreateProfile.this, "com.example.android.fileprovider", photoFile);
                                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                    startActivityForResult(intent, pickImageCamera);
                                }
                            }
                        }
                        else {
                            ActivityCompat.requestPermissions(CreateProfile.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestWriteStorage);
                        }
                    }
                    else {
                        ActivityCompat.requestPermissions(CreateProfile.this, new String[] {Manifest.permission.CAMERA}, requestCamera);
                    }

                } else if (options[item].equals("Choose From Gallery")) {
                    dialog.dismiss();
                    if (ContextCompat.checkSelfPermission(CreateProfile.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){
                        ActivityCompat.requestPermissions(CreateProfile.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, requestGallery);
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


    private void uploadData(String url) {
        student =  new Student();
        student.setEmail(email);
        student.setRegNo(regNo);
        student.setUserName(userName);
        student.setCourse(course);
        student.setType("student");
        if(url != null) {
            student.setProfileUrl(imageUrl);
        }

        reference = firebaseDatabase.getReference().child("students");
        DatabaseReference reference1 = reference.push();
        String key = reference1.getKey();
        student.setId(key);

        reference1.setValue(student).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(CreateProfile.this, MainActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                    try{
                        image.delete();
                    }catch (Exception ex){

                    }
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(CreateProfile.this, "Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    void compress(){
        long sizeKb = bitmap.getByteCount() / 1024;
        if(sizeKb <= 200){
            compressionRate = 100;
        }
        else if(sizeKb <= 500){
            compressionRate = 90;
        }
        else if(sizeKb <= 1000){
            compressionRate = 70;
        }
        else if(sizeKb < 2000){
            compressionRate = 50;
        }
        else if(sizeKb < 4000){
            compressionRate = 25;
        }
        else{
            compressionRate = 15;
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.WEBP, compressionRate, bout);
        bytes = bout.toByteArray();
        try {
            bout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

}