package com.dekut.dekutchat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dekut.dekutchat.R;
import com.dekut.dekutchat.utils.Group;
import com.dekut.dekutchat.utils.Student;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditProfile extends AppCompatActivity {
    ImageView profilePic, editPic;
    ImageButton editUsername, editRegNo, editCourse;
    TextView tvUsername, tvReg, tvEmail, tvCourse;
    ProgressBar progressBar;
    Button btnDeleteAccount, btnChangePassword;

    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    Query query;
    Student student;
    int pickImageCamera = 1, pickImageGallery = 2, requestCamera = 3, requestGallery = 4;
    Uri selectedImage;
    String key, previousUrl, name, currentPhotoPath, status;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    StorageReference storageReference;
    int compressionRate;
    Bitmap bitmap;
    byte[] bytes;
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_edit_profile);

            profilePic = findViewById(R.id.profilePic);
            tvUsername = findViewById(R.id.tvUserName2);
            tvEmail = findViewById(R.id.tvEmail2);
            tvReg = findViewById(R.id.tvReg2);
            tvCourse = findViewById(R.id.tvCourse2);
            progressBar = findViewById(R.id.progressBar);
            editUsername = findViewById(R.id.editUsername);
            editRegNo = findViewById(R.id.editRegNo);
            editCourse = findViewById(R.id.editCourse);
            editPic = findViewById(R.id.editPic);
            btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
            btnChangePassword = findViewById(R.id.btnChangePassword);

            Bundle extras = getIntent().getExtras();
            status = extras.getString("status");

            if (status.equals("guest")) {
                editPic.setVisibility(View.INVISIBLE);
                editUsername.setVisibility(View.INVISIBLE);
                editRegNo.setVisibility(View.INVISIBLE);
                editCourse.setVisibility(View.INVISIBLE);
                btnDeleteAccount.setVisibility(View.INVISIBLE);
                btnChangePassword.setVisibility(View.INVISIBLE);
            }

            fetchData();

            profilePic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(EditProfile.this, ViewImage.class);
                    intent.putExtra("url", student.getProfileUrl());
                    startActivity(intent);
                }
            });

            editPic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectImage();
                }
            });

            editUsername.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEntryDialog("Username");
                }
            });

            editRegNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEntryDialog("Reg NO");
                }
            });

            editCourse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEntryDialog2();
                }
            });

            btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPasswordDialog("Enter Password", "delete");
                }
            });

            btnChangePassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPasswordDialog("Enter Password", "change");
                }
            });

        }catch (Exception ex){
            Log.e("myTag", "edit: "+ ex.getMessage());
        }
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
            selectedImage = data.getData();
        }

        if(selectedImage != null) {
            ProgressDialog progressDialog = new ProgressDialog(EditProfile.this);
            progressDialog.show();
            progressDialog.setContentView(R.layout.progress_dialog);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            progressDialog.setCancelable(false);

            previousUrl = student.getProfileUrl();

            try {
                bitmap = MediaStore.Images.Media.getBitmap(EditProfile.this.getContentResolver(), selectedImage);
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

                storageReference = firebaseStorage.getReference().child("profilePics/" + UUID.randomUUID().toString());
                UploadTask uploadTask = storageReference.putBytes(bytes);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();
                                DatabaseReference reference = firebaseDatabase.getReference().child("students").child(key).child("profileUrl");
                                reference.setValue(imageUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            StorageReference reference1 = firebaseStorage.getReferenceFromUrl(previousUrl);
                                            reference1.delete();
                                            fetchData();
                                            Toast.makeText(getApplicationContext(), "Profile Picture Updated", Toast.LENGTH_SHORT).show();
                                        } else {
                                            storageReference.delete();
                                            Toast.makeText(getApplicationContext(), "Failed To Upload Picture. Try Again Later: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                        progressDialog.dismiss();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                storageReference.delete();
                                Toast.makeText(getApplicationContext(), "Failed to Upload Image. Try Again Later: "+e.getMessage(), Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        bitmap = null;
                        bytes = null;
                        Toast.makeText(getApplicationContext(), "Failed To Upload Picture. Try Again Later: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

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
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void fetchData(){
        query = firebaseDatabase.getReference().child("students").orderByChild("email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    student = snap.getValue(Student.class);
                    progressBar.setVisibility(View.GONE);

                    if (student.getProfileUrl() != null) {
                        Glide.with(getApplicationContext())
                                .load(student.getProfileUrl())
                                .into(profilePic);
                    }
                    name = student.getUserName();
                    tvUsername.setText(name);
                    tvEmail.setText(email);
                    tvReg.setText(student.getRegNo());
                    tvCourse.setText(student.getCourse());
                    key = snap.getKey();
                    break;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose From Gallery", "Remove Photo","Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfile.this);
        builder.setTitle("Select Option");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Remove Photo")){
                    StorageReference reference = firebaseStorage.getReferenceFromUrl(student.getProfileUrl());
                    Query query = firebaseDatabase.getReference().child("students").orderByChild("email").equalTo(student.getEmail());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            for (DataSnapshot snap : snapshot.getChildren()){
                                DatabaseReference reference1 = snap.getRef();
                                reference1.child("profileUrl").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            reference.delete();
                                            Drawable drawable = getResources().getDrawable(R.drawable.ic_person3);
                                            profilePic.setImageDrawable(drawable);
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });

                }

                else if (options[item].equals("Take Photo")) {
                    dialog.dismiss();
                    if (ContextCompat.checkSelfPermission(EditProfile.this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_DENIED){
                        ActivityCompat.requestPermissions(EditProfile.this, new String[] {Manifest.permission.CAMERA}, requestCamera);
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
                                Uri photoURI = FileProvider.getUriForFile(EditProfile.this, "com.example.android.fileprovider", photoFile);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                startActivityForResult(intent, pickImageCamera);
                            }
                        }
                    }

                } else if (options[item].equals("Choose From Gallery")) {
                    dialog.dismiss();
                    if (ContextCompat.checkSelfPermission(EditProfile.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){
                        ActivityCompat.requestPermissions(EditProfile.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, requestGallery);
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

    public void showEntryDialog(String child){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.entry_dialog2, null);
        builder.setView(view);
        EditText etItem = view.findViewById(R.id.etString);
        builder.setTitle("Enter " + child);
        etItem.requestFocus();
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnOk = view.findViewById(R.id.btnOk);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String item = etItem.getText().toString().trim();
                if(item.isEmpty()){
                    etItem.setError("Enter Item");
                    etItem.requestFocus();
                }
                else {
                    if (child.equals("Reg NO")) {
                        alertDialog.dismiss();
                        Pattern pattern = Pattern.compile("[A-Z]\\d{3,}-0([12])-\\d{4}/(\\d{4})");
                        Matcher matcher = pattern.matcher(item);
                        if (matcher.matches()) {
                            alertDialog.cancel();
                            DatabaseReference reference =firebaseDatabase.getReference().child("students").child(key);
                            Map<String, Object> map = new HashMap<>();
                            map.put("regNo", item);
                            reference.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        fetchData();
                                        Toast.makeText(getApplicationContext(), "RegNo has been updated", Toast.LENGTH_LONG).show();
                                    }
                                    if(!task.isSuccessful()){
                                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } else {
                            etItem.requestFocus();
                            etItem.setError("Reg No is wrong formatted!");
                        }
                    }else if(child.equals("Username")){
                        alertDialog.dismiss();
                        DatabaseReference reference = firebaseDatabase.getReference().child("students").child(key);
                        Map<String, Object> map = new HashMap<>();
                        map.put("userName", item);
                        reference.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    fetchData();
                                    Toast.makeText(getApplicationContext(), "Username has been updated", Toast.LENGTH_LONG).show();
                                }
                                else {
                                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });
    }

    public void showEntryDialog2(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.entry_dialog1, null);
        builder.setView(view);
        builder.setTitle("Select Course");

        SearchableSpinner searchableSpinner = view.findViewById(R.id.searchSpinner);
        Resources res = getResources();
        String[] courses = res.getStringArray(R.array.courses);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, courses);
        searchableSpinner.setAdapter(adapter);
        searchableSpinner.setTitle("Select Course");

        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnOk = view.findViewById(R.id.btnOk);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String course = searchableSpinner.getSelectedItem().toString();
                if(course.equals("Select Course")){
                    Toast.makeText(EditProfile.this, "Select a course", Toast.LENGTH_LONG).show();
                }
                else {
                    DatabaseReference reference = firebaseDatabase.getReference().child("students").child(key);
                    Map<String, Object> map = new HashMap<>();
                    map.put("course",course);
                    reference.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                fetchData();
                                Toast.makeText(getApplicationContext(), "Course has been updated", Toast.LENGTH_LONG).show();
                                alertDialog.dismiss();
                            }
                            if(!task.isSuccessful()){
                                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
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

    public void showPasswordDialog(String title, String op) {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfile.this);
        View view = getLayoutInflater().inflate(R.layout.entry_dialog2, null);
        builder.setView(view);
        EditText etItem = view.findViewById(R.id.etString);
        etItem.setTransformationMethod(PasswordTransformationMethod.getInstance());
        builder.setTitle(title);
        etItem.requestFocus();
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnOk = view.findViewById(R.id.btnOk);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = etItem.getText().toString();
                if (password.isEmpty()) {
                    etItem.setError("Enter Password");
                } else {
                    if (op.equals("delete")) {
                        alertDialog.dismiss();
                        showAlertDialog(password);
                    }

                    else if (op.equals("change")) {
                        if (title.equals("Enter Password")) {
                            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    etItem.setText("");
                                    alertDialog.setTitle("Enter New Password");
                                    btnOk.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            String password = etItem.getText().toString();
                                            if (password.length() < 6){
                                                etItem.setError("Password must be at least 6 characters");
                                            }
                                            else {
                                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                user.updatePassword(password).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        Toast.makeText(EditProfile.this, "Password has been updated", Toast.LENGTH_LONG).show();
                                                        alertDialog.dismiss();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull @NotNull Exception e) {
                                                        alertDialog.dismiss();
                                                        Toast.makeText(EditProfile.this, "Failed to update password, try again later", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull @NotNull Exception e) {
                                    alertDialog.dismiss();
                                    Toast.makeText(EditProfile.this, "Failed to validate password", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }

                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    public void showAlertDialog(String password){
        Dialog deleteDialog = new Dialog(EditProfile.this);
        deleteDialog.setContentView(R.layout.delete_dialog2);
        deleteDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        deleteDialog.setCancelable(false);
        Button btnCancel = deleteDialog.findViewById(R.id.btnCancel);
        Button btnDelete = deleteDialog.findViewById(R.id.btnDelete);

        deleteDialog.show();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDialog.dismiss();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressDialog progressDialog = new ProgressDialog(EditProfile.this);
                progressDialog.show();
                progressDialog.setContentView(R.layout.progress_dialog);
                progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            DatabaseReference reference = firebaseDatabase.getReference().child("students").child(key);
                            reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        firebaseAuth.getCurrentUser().delete();
                                        firebaseAuth.signOut();
                                        Toast.makeText(getApplicationContext(), "Your Account has been deleted", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(EditProfile.this, Login.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Deletion failed. Try again later", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } else {
                            progressDialog.dismiss();
                            deleteDialog.dismiss();
                            Toast.makeText(getBaseContext(), "Failed, check password and try again later", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}