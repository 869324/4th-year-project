package com.dekut.dekutchat.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
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
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dekut.dekutchat.R;
import com.dekut.dekutchat.adapters.SearchUserAdapter;
import com.dekut.dekutchat.utils.Group;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
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

public class EditGroup extends AppCompatActivity {
    ImageView imageView;
    ImageButton btnEditPic, btnEditType, btnEditName, btnEditDesc;
    TextView tvType, tvName, tvDesc;
    Button btnAddMembers, btnAddAdmins, btnAddPassword, btnDelete;

    int pickImageCamera = 1, pickImageGallery = 2, requestCamera = 3, requestGallery = 4;
    Uri selectedImage;
    String previousUrl, currentPhotoPath, groupId;
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    StorageReference storageReference;
    int compressionRate;
    Bitmap bitmap;
    byte[] bytes;
    File file;
    Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group);

        imageView = findViewById(R.id.imageView);
        btnEditPic = findViewById(R.id.btnEditPic);
        btnEditType = findViewById(R.id.btnEditType);
        btnEditName = findViewById(R.id.btnEditName);
        btnEditDesc = findViewById(R.id.btnEditDesc);
        tvType = findViewById(R.id.tvType2);
        tvName = findViewById(R.id.tvName2);
        tvDesc = findViewById(R.id.tvDescription2);
        btnAddMembers = findViewById(R.id.btnAddMembers);
        btnAddAdmins = findViewById(R.id.btnAddAdmin);
        btnAddPassword = findViewById(R.id.btnAddPassword);
        btnDelete = findViewById(R.id.btnDelete);

        Bundle extras = getIntent().getExtras();
        groupId = extras.getString("groupId");

        Query query = firebaseDatabase.getReference().child("groups").orderByKey().equalTo(groupId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap : snapshot.getChildren()){
                    group = snap.getValue(Group.class);

                    if (group.getImageUrl() != null) {
                        Glide.with(getApplicationContext())
                                .load(group.getImageUrl())
                                .into(imageView);
                    }

                    tvName.setText(group.getName());
                    tvType.setText(group.getType());
                    if (group.getDescription() != null){
                        tvDesc.setText(group.getDescription());
                    }

                    break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditGroup.this, ViewImage.class);
                intent.putExtra("url", group.getImageUrl());
                startActivity(intent);
            }
        });

        btnEditPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        btnEditName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEntryDialog("Name");
            }
        });

        btnEditType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEntryDialog2();
            }
        });

        btnEditDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEntryDialog("Description");
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (group.getPassword().isEmpty()){
                    showDeleteDialog();
                }
                else {
                    showPasswordDialog();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
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
            ProgressDialog progressDialog = new ProgressDialog(EditGroup.this);
            progressDialog.show();
            progressDialog.setContentView(R.layout.progress_dialog);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            progressDialog.setCancelable(false);

            try {
                bitmap = MediaStore.Images.Media.getBitmap(EditGroup.this.getContentResolver(), selectedImage);
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
                                String imageUrl = uri.toString();
                                DatabaseReference reference = firebaseDatabase.getReference().child("groups").child(groupId).child("imageUrl");
                                reference.setValue(imageUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            StorageReference reference1 = firebaseStorage.getReferenceFromUrl(previousUrl);
                                            reference1.delete();
                                            Toast.makeText(getApplicationContext(), "Group Picture Updated", Toast.LENGTH_SHORT).show();
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
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose From Gallery", "Remove Photo","Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(EditGroup.this);
        builder.setTitle("Select Option");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Remove Photo")){
                    StorageReference reference = firebaseStorage.getReferenceFromUrl(group.getImageUrl());
                    DatabaseReference reference1 = firebaseDatabase.getReference().child("groups").child(groupId).child("imageUrl");
                    reference1.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                            if (task.isSuccessful()){
                                reference.delete();
                            }
                        }
                    });

                    //imageView.setImageDrawable(null);
                }
                else if (options[item].equals("Take Photo")) {
                    dialog.dismiss();
                    if (ContextCompat.checkSelfPermission(EditGroup.this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_DENIED){
                        ActivityCompat.requestPermissions(EditGroup.this, new String[] {Manifest.permission.CAMERA}, requestCamera);
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
                                Uri photoURI = FileProvider.getUriForFile(EditGroup.this, "com.example.android.fileprovider", photoFile);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                startActivityForResult(intent, pickImageCamera);
                            }
                        }
                    }

                } else if (options[item].equals("Choose From Gallery")) {
                    dialog.dismiss();
                    if (ContextCompat.checkSelfPermission(EditGroup.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){
                        ActivityCompat.requestPermissions(EditGroup.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, requestGallery);
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

    public void showPasswordDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(EditGroup.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.entry_dialog2, null);
        builder.setView(view);
        EditText etItem = view.findViewById(R.id.etString);
        etItem.setTransformationMethod(PasswordTransformationMethod.getInstance());
        builder.setTitle("Enter Password");
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
                    if (password.equals(group.getPassword())) {
                        showDeleteDialog();
                    }

                    else {
                        Toast.makeText(EditGroup.this, "You entered the wrong password", Toast.LENGTH_LONG).show();
                    }
                    alertDialog.dismiss();
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

    public void showDeleteDialog(){
        Dialog deleteDialog = new Dialog(EditGroup.this);
        deleteDialog.setContentView(R.layout.delete_dialog2);
        deleteDialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        deleteDialog.setCancelable(false);

        TextView textView = deleteDialog.findViewById(R.id.tvTitle);
        Button btnCancel = deleteDialog.findViewById(R.id.btnCancel);
        Button btnDelete = deleteDialog.findViewById(R.id.btnDelete);

        textView.setText("Are you sure you want to delete this group?");

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
                ProgressDialog progressDialog = new ProgressDialog(EditGroup.this);
                progressDialog.show();
                progressDialog.setContentView(R.layout.progress_dialog);
                progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                DatabaseReference reference = firebaseDatabase.getReference().child("groups").child(groupId);
                reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()){
                            DatabaseReference reference1 = firebaseDatabase.getReference().child("groupConversations").child(groupId);
                            reference1.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        StorageReference reference2 = firebaseStorage.getReferenceFromUrl(group.getImageUrl());
                                        reference2.delete();
                                        Toast.makeText(EditGroup.this, "This group has been deleted", Toast.LENGTH_LONG).show();
                                        onBackPressed();
                                        finish();
                                    }
                                }
                            });

                        }
                    }
                });
            }
        });
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
                    if (child.equals("Name")) {
                        alertDialog.dismiss();

                        DatabaseReference reference =firebaseDatabase.getReference().child("groups").child(groupId);
                        Map<String, Object> map = new HashMap<>();
                        map.put("name", item);
                        reference.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(getApplicationContext(), "Group Name has been updated", Toast.LENGTH_LONG).show();
                                }
                                if(!task.isSuccessful()){
                                    Toast.makeText(getApplicationContext(), "Failed to update name. Try again later", Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                    }else if(child.equals("Description")){
                        alertDialog.dismiss();
                        DatabaseReference reference = firebaseDatabase.getReference().child("groups").child(groupId);
                        Map<String, Object> map = new HashMap<>();
                        map.put("description", item);
                        reference.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(getApplicationContext(), "Group description has been updated", Toast.LENGTH_LONG).show();
                                }
                                else {
                                    Toast.makeText(getApplicationContext(), "Failed to update description. Try again later", Toast.LENGTH_LONG).show();
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
        View view = inflater.inflate(R.layout.type_dialog, null);
        builder.setView(view);
        builder.setTitle("Select Type");

        RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnOk = view.findViewById(R.id.btnOk);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int checkedId = radioGroup.getCheckedRadioButtonId();

                if (checkedId == -1){
                    Toast.makeText(EditGroup.this, "Select type", Toast.LENGTH_LONG).show();
                }
                else {
                    String type = null;
                    if (checkedId == 1) {
                        type = "public";
                    } else if (checkedId == 2) {
                        type = "private";
                    }

                    if (type != null) {
                        DatabaseReference reference = firebaseDatabase.getReference().child("groups").child(groupId);
                        Map<String, Object> map = new HashMap<>();
                        map.put("type", type);
                        reference.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Group type has been updated", Toast.LENGTH_LONG).show();
                                    alertDialog.dismiss();
                                }
                                if (!task.isSuccessful()) {
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
}