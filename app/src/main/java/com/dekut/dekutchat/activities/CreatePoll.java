package com.dekut.dekutchat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.dekut.dekutchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreatePoll extends AppCompatActivity {
    ImageButton btnCancel, btnAdd;
    EditText etText;
    Button btnPost;
    ScrollView scrollView;
    LinearLayout entriesLayout;

    ProgressDialog progressDialog;
    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    int num = 3, counter = 0;
    List<String> options = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_poll);

        btnCancel = findViewById(R.id.btnCancel);
        btnAdd = findViewById(R.id.btnAdd);
        etText = findViewById(R.id.etText);
        btnPost = findViewById(R.id.btnPost);
        scrollView = findViewById(R.id.scrollView);
        entriesLayout = findViewById(R.id.entriesLayout);

        addEntries();

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                num += 1;
                addEntries();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = new ProgressDialog(CreatePoll.this);
                progressDialog.show();
                progressDialog.setContentView(R.layout.upload_progress);
                progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                progressDialog.setCancelable(false);
                TextView textView = progressDialog.findViewById(R.id.textView);

                String question = etText.getText().toString();
                for (int i = 0; i < num; i++) {
                    EditText editText = entriesLayout.findViewById(i);
                    String option = editText.getText().toString();
                    if (!option.isEmpty()){
                        options.add(option);
                    }
                }

                if( question.isEmpty() || options.isEmpty()){
                    progressDialog.dismiss();
                    Toast.makeText(CreatePoll.this, "Enter all necessary details", Toast.LENGTH_SHORT).show();
                }
                else {
                    DatabaseReference reference = firebaseDatabase.getReference().child("politicsPosts");
                    Map<String, Object> map = new HashMap<>();
                    map.put("type", "poll");
                    map.put("poster", email);
                    map.put("text", question);
                    map.put("imgHeight", 0);
                    map.put("timestamp", ServerValue.TIMESTAMP);

                    int counter2 = 0;
                    Map<String, Object> map1 = new HashMap<>();
                    for (String entry : options){
                        Map<String, Object> map2 = new HashMap<>();
                        map2.put("name", entry);
                        map1.put("option" + String.valueOf( counter2 + 1), map2);
                        counter2 += 1;
                    }

                    map.put("options", map1);

                    DatabaseReference reference1 = reference.push();
                    String key = reference1.getKey();
                    map.put("id", key);
                    reference1.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                            if (task.isSuccessful()){
                                progressDialog.dismiss();
                                onBackPressed();
                            }
                            else {
                                reference1.removeValue();
                                Toast.makeText(CreatePoll.this, "Failed, try again later", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void addEntries() {
        for (; counter < num; counter++) {
            EditText editText = new EditText(this);
            editText.setId(counter);
            editText.setHint("option" + String.valueOf(counter + 1));
            int width = LinearLayout.LayoutParams.MATCH_PARENT;
            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
            params.setMargins(70, 0, 70, 5);
            editText.setLayoutParams(params);
            editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            editText.setMaxLines(1);
            editText.setEllipsize(TextUtils.TruncateAt.END);
            entriesLayout.addView(editText);
        }
    }
}