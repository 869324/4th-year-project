package com.dekut.dekutchat.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.dekut.dekutchat.R;
import com.dekut.dekutchat.activities.EditGroup;
import com.dekut.dekutchat.activities.SelectUser;
import com.dekut.dekutchat.utils.Group;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class ButtonsAdapter extends BaseAdapter {
    List<Map> entries;
    Context context;
    String groupId;
    LayoutInflater layoutInflater;
    EditGroup editGroup;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    Group group;

    public ButtonsAdapter(@NonNull Context context, List<Map> entries, String groupId) {
        this.entries = entries;
        this.context = context;
        this.groupId = groupId;

        layoutInflater = LayoutInflater.from(context);
        editGroup = (EditGroup) context;
    }

    @Override
    public int getCount() {
        return entries.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }


    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = layoutInflater.inflate(R.layout.buttons_card_view, null);

        Map<String, Object> map = entries.get(i);
        TextView textView = view.findViewById(R.id.textView);
        ImageView imageView = view.findViewById(R.id.imageView);

        imageView.setImageResource(Integer.parseInt(map.get("imgId").toString()));
        textView.setText(String.valueOf(map.get("text")));

        String text = String.valueOf(map.get("text"));

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Query query = firebaseDatabase.getReference().child("groups").orderByKey().equalTo(groupId);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot snap : snapshot.getChildren()){
                            group = snap.getValue(Group.class);

                            if (text.equals("Add Members")){
                                Intent intent = new Intent(context, SelectUser.class);
                                intent.putExtra("operation", "addMembers");
                                intent.putExtra("groupId", groupId);
                                context.startActivity(intent);
                            }
                            else if (text.equals("Add Admins")){
                                Intent intent = new Intent(context, SelectUser.class);
                                intent.putExtra("operation", "addAdmins");
                                intent.putExtra("groupId", groupId);
                                context.startActivity(intent);
                            }

                            else if (text.equals("Remove Members")){
                                Intent intent = new Intent(context, SelectUser.class);
                                intent.putExtra("operation", "removeMembers");
                                intent.putExtra("groupId", groupId);
                                context.startActivity(intent);
                            }

                            else if (text.equals("Remove Admins")){
                                Intent intent = new Intent(context, SelectUser.class);
                                intent.putExtra("operation", "removeAdmins");
                                intent.putExtra("groupId", groupId);
                                context.startActivity(intent);
                            }

                            /*else if (text.equals("Add Password")){
                                operation = "addMembers";
                            }
                            else if (text.equals("Change Password")){
                                operation = "addMembers";
                            }
                            else if (text.equals("Remove Password")){
                                operation = "addMembers";
                            }*/
                            else if (text.equals("Delete Group")){
                                if (group.getPassword().isEmpty()){
                                    showDeleteDialog(group);
                                }
                                else {
                                    showPasswordDialog(group);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

            }
        });


        return view;

    }

    public void showPasswordDialog(Group group){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = layoutInflater.inflate(R.layout.entry_dialog2, null);
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
                        showDeleteDialog(group);
                    }

                    else {
                        Toast.makeText(context, "You entered the wrong password", Toast.LENGTH_LONG).show();
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

    public void showDeleteDialog(Group group){
        Dialog deleteDialog = new Dialog(context);
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
                ProgressDialog progressDialog = new ProgressDialog(context);
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
                                        Toast.makeText(context, "This group has been deleted", Toast.LENGTH_LONG).show();
                                        editGroup.onBackPressed();
                                    }
                                }
                            });

                        }
                    }
                });
            }
        });
    }
}
