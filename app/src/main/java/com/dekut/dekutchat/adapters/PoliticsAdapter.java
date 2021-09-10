package com.dekut.dekutchat.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dekut.dekutchat.R;
import com.dekut.dekutchat.activities.Comments;
import com.dekut.dekutchat.activities.ViewImage;
import com.dekut.dekutchat.activities.ViewProfile;
import com.dekut.dekutchat.activities.ViewVideo;
import com.dekut.dekutchat.utils.HomePost;
import com.dekut.dekutchat.utils.PoliticsPost;
import com.dekut.dekutchat.utils.PoliticsPost;
import com.dekut.dekutchat.utils.Student;
import com.dekut.dekutchat.utils.TimeCalc;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class PoliticsAdapter extends RecyclerView.Adapter {
    List<PoliticsPost> posts;
    Context context;
    String user;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    Activity activity;
    Dialog deleteDialog;
    private static final int PLAIN_POST = 1;
    private static final int MEDIA_POST = 2;
    private static final int POLL_POST = 3;
    boolean edit;

    public PoliticsAdapter(List<PoliticsPost> posts, Context context, String user, boolean edit){
        this.posts = posts;
        this.context = context;
        this.user = user;
        this.edit = edit;
        activity = (Activity) context;
    }

    @Override
    public int getItemViewType(int position) {
        PoliticsPost politicsPost = posts.get(position);
        String type = politicsPost.getType();

        if (type.equals("text")){
            return PLAIN_POST;
        }

        else if (type.equals("media")){
            return MEDIA_POST;
        }

        else {
            return POLL_POST;
        }
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == PLAIN_POST) {
            view = inflater.inflate(R.layout.home_plain_card_view, parent, false);
            return new PoliticsAdapter.PlainViewHolder(view);
        }

        else if (viewType == MEDIA_POST){
            view = inflater.inflate(R.layout.home_media_card_view, parent, false);
            return new PoliticsAdapter.MediaViewHolder(view);
        }
        else if (viewType == POLL_POST){
            view = inflater.inflate(R.layout.poll_card_view, parent, false);
            return new PoliticsAdapter.PollViewHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        PoliticsPost politicsPost = posts.get(position);

        switch (holder.getItemViewType()) {
            case PLAIN_POST:
                ((PoliticsAdapter.PlainViewHolder) holder).bind(politicsPost);
                break;
            case MEDIA_POST:
                ((PoliticsAdapter.MediaViewHolder) holder).bind(politicsPost);
                break;
            case POLL_POST:
                ((PoliticsAdapter.PollViewHolder) holder).bind(politicsPost);
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class MediaViewHolder extends RecyclerView.ViewHolder{
        TextView tvText;
        ImageView profilePic, type, postImage;
        TextView tvUserName, tvPosition, tvPostTime, tvLikes, tvComments;
        CardView playCard, postImageCard;
        ImageButton btnLike, btnComment, btnShare, btnPlay, menu;
        ProgressBar progressBar;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tvText);
            profilePic = itemView.findViewById(R.id.profilePic);
            type = itemView.findViewById(R.id.type);
            postImage = itemView.findViewById(R.id.postImage);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvPosition = itemView.findViewById(R.id.tvPosition);
            tvPostTime = itemView.findViewById(R.id.tvPostTime);
            tvLikes = itemView.findViewById(R.id.tvLikes);
            tvComments = itemView.findViewById(R.id.tvComments);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
            btnShare = itemView.findViewById(R.id.btnShare);
            btnPlay = itemView.findViewById(R.id.btnPlay);
            playCard = itemView.findViewById(R.id.playCard);
            progressBar = itemView.findViewById(R.id.progressBar);
            postImageCard = itemView.findViewById(R.id.postImageCard);
            menu = itemView.findViewById(R.id.menu);

            if (context instanceof Comments){
                btnComment.setEnabled(false);
            }
        }

        public void bind(PoliticsPost politicsPost){
            int imgHeight = politicsPost.getImgHeight();
            postImage.requestLayout();
            postImage.getLayoutParams().height = imgHeight;

            politicsPost.getPoster(new PoliticsPost.SimpleCallback<Student>() {
                @Override public void callback(Student poster) {
                    if(poster.getProfileUrl() != null) {
                        Glide.with(itemView.getContext())
                                .load(poster.getProfileUrl())
                                .into(profilePic);
                    }
                    else {
                        Drawable drawable = DrawableCompat.wrap(AppCompatResources.getDrawable(context, R.drawable.ic_person3));
                        profilePic.setImageDrawable(drawable);
                    }

                    tvUserName.setText(poster.getUserName());

                    if(poster.getType().equals("leader")){
                        type.setVisibility(View.VISIBLE);
                        tvPosition.setText("Student Leader");
                    }
                    else {
                        type.setVisibility(View.GONE);
                        tvPosition.setVisibility(View.GONE);
                    }

                    tvText.setText(politicsPost.getText());
                    if(politicsPost.getImageUrl() != null){
                        playCard.setVisibility(View.INVISIBLE);
                        Glide.with(itemView.getContext())
                                .load(politicsPost.getImageUrl())
                                .into(postImage);
                    }
                    if(politicsPost.getVideoUrl() != null){
                        playCard.setVisibility(View.VISIBLE);
                    }
                    if(politicsPost.getImageUrl() == null && politicsPost.getVideoUrl() == null){
                        postImage.setVisibility(LinearLayout.GONE);
                    }

                    TimeCalc timeCalc = new TimeCalc();
                    String time = timeCalc.getTimeAgo(politicsPost.getTimestamp());
                    tvPostTime.setText(time);

                    politicsPost.isLiked(new PoliticsPost.SimpleCallback<Boolean>() {
                        @Override
                        public void callback(Boolean isLiked) {
                            if (isLiked){
                                Drawable drawable = btnLike.getDrawable();
                                DrawableCompat.setTint(drawable, ContextCompat.getColor(context, R.color.primaryColor));
                            }
                            else {
                                Drawable drawable = DrawableCompat.wrap(AppCompatResources.getDrawable(context, R.drawable.ic_thumb_up));
                                btnLike.setImageDrawable(drawable);
                            }

                            btnLike.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    DatabaseReference reference = firebaseDatabase.getReference().child("politicsPosts").child(politicsPost.getId()).child("likes");
                                    if(isLiked){
                                        Query query = reference.orderByChild("id").equalTo(email);
                                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    for (DataSnapshot snap : snapshot.getChildren()) {
                                                        String key = snap.getKey();
                                                        DatabaseReference reference1 = reference.child(key);
                                                        reference1.removeValue();
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                            }
                                        });
                                    }
                                    else {
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("id", email);
                                        reference.push().setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                            }
                                        });
                                    }
                                }

                            });
                        }
                    });

                    postImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(politicsPost.getVideoUrl() != null){
                                Intent intent = new Intent(context, ViewVideo.class);
                                intent.putExtra("videoUrl", politicsPost.getVideoUrl());
                                activity.startActivity(intent);
                            }
                            else if(politicsPost.getImageUrl() != null){
                                Intent intent = new Intent(context, ViewImage.class);
                                intent.putExtra("url", politicsPost.getImageUrl());
                                context.startActivity(intent);
                            }
                        }
                    });

                    politicsPost.getLikesCount(new PoliticsPost.SimpleCallback<Long>() {
                        @Override
                        public void callback(Long count) {
                            tvLikes.setText(String.valueOf(count));
                        }
                    });

                    politicsPost.getCommentsCount(new PoliticsPost.SimpleCallback<Long>() {
                        @Override
                        public void callback(Long count) {
                            tvComments.setText(String.valueOf(count));
                        }
                    });

                    profilePic.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, ViewImage.class);
                            intent.putExtra("url", poster.getProfileUrl());
                            context.startActivity(intent);

                        }
                    });

                    tvUserName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(context, ViewProfile.class);
                            intent.putExtra("profileEmail", poster.getEmail());
                            context.startActivity(intent);
                        }
                    });

                    btnComment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, Comments.class);
                            intent.putExtra("id", politicsPost.getId());
                            intent.putExtra("source", "politics");
                            context.startActivity(intent);
                        }
                    });

                    btnPlay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, ViewVideo.class);
                            intent.putExtra("videoUrl", politicsPost.getVideoUrl());
                            activity.startActivity(intent);
                        }
                    });

                    btnShare.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Uri bmpUri = getLocalBitmapUri(postImage);

                            if (politicsPost.getVideoUrl() == null) {
                                Intent shareIntent = new Intent();
                                shareIntent.setType("image/*");
                                shareIntent.setAction(Intent.ACTION_SEND);
                                shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);

                                if (politicsPost.getText() != null) {
                                    shareIntent.putExtra(Intent.EXTRA_TEXT, politicsPost.getText());
                                }

                                context.startActivity(Intent.createChooser(shareIntent, "Share Image"));

                            }

                            else {
                                showDialog(politicsPost, view);
                            }
                        }
                    });

                    if (edit){
                        menu.setVisibility(View.VISIBLE);
                        menu.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                selectOperation(politicsPost);
                            }
                        });
                    }
                }
            });
        }
    }

    public class PlainViewHolder extends RecyclerView.ViewHolder{
        TextView tvText;
        ImageView profilePic, type;
        TextView tvUserName, tvPosition, tvPostTime, tvLikes, tvComments;
        ImageButton btnLike, btnComment, btnShare, menu;

        public PlainViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tvText);
            profilePic = itemView.findViewById(R.id.profilePic);
            type = itemView.findViewById(R.id.type);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvPosition = itemView.findViewById(R.id.tvPosition);
            tvPostTime = itemView.findViewById(R.id.tvPostTime);
            tvLikes = itemView.findViewById(R.id.tvLikes);
            tvComments = itemView.findViewById(R.id.tvComments);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
            btnShare = itemView.findViewById(R.id.btnShare);
            menu = itemView.findViewById(R.id.menu);

            if (context instanceof Comments){
                btnComment.setEnabled(false);
            }
        }

        public void bind(PoliticsPost politicsPost){
            politicsPost.getPoster(new PoliticsPost.SimpleCallback<Student>() {
                @Override public void callback(Student poster) {
                    if(poster.getProfileUrl() != null) {
                        Glide.with(itemView.getContext())
                                .load(poster.getProfileUrl())
                                .into(profilePic);
                    }
                    else {
                        Drawable drawable = DrawableCompat.wrap(AppCompatResources.getDrawable(context, R.drawable.ic_person3));
                        profilePic.setImageDrawable(drawable);
                    }

                    tvUserName.setText(poster.getUserName());

                    if(poster.getType().equals("leader")){
                        type.setVisibility(View.VISIBLE);
                        tvPosition.setText("Student Leader");
                    }
                    else {
                        type.setVisibility(View.GONE);
                        tvPosition.setVisibility(View.GONE);
                    }

                    tvText.setText(politicsPost.getText());

                    TimeCalc timeCalc = new TimeCalc();
                    String time = timeCalc.getTimeAgo(politicsPost.getTimestamp());
                    tvPostTime.setText(time);

                    politicsPost.isLiked(new PoliticsPost.SimpleCallback<Boolean>() {
                        @Override
                        public void callback(Boolean isLiked) {
                            if (isLiked){
                                Drawable drawable = btnLike.getDrawable();
                                DrawableCompat.setTint(drawable, ContextCompat.getColor(context, R.color.primaryColor));
                            }
                            else {
                                Drawable drawable = DrawableCompat.wrap(AppCompatResources.getDrawable(context, R.drawable.ic_thumb_up));
                                btnLike.setImageDrawable(drawable);
                            }

                            btnLike.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    DatabaseReference reference = firebaseDatabase.getReference().child("politicsPosts").child(politicsPost.getId()).child("likes");
                                    if(isLiked){
                                        Query query = reference.orderByChild("id").equalTo(email);
                                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    for (DataSnapshot snap : snapshot.getChildren()) {
                                                        String key = snap.getKey();
                                                        DatabaseReference reference1 = reference.child(key);
                                                        reference1.removeValue();
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                            }
                                        });
                                    }
                                    else {
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("id", email);
                                        reference.push().setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                            }
                                        });
                                    }
                                }

                            });
                        }
                    });

                    politicsPost.getLikesCount(new PoliticsPost.SimpleCallback<Long>() {
                        @Override
                        public void callback(Long count) {
                            tvLikes.setText(String.valueOf(count));
                        }
                    });

                    politicsPost.getCommentsCount(new PoliticsPost.SimpleCallback<Long>() {
                        @Override
                        public void callback(Long count) {
                            tvComments.setText(String.valueOf(count));
                        }
                    });

                    profilePic.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, ViewImage.class);
                            intent.putExtra("url", poster.getProfileUrl());
                            context.startActivity(intent);

                        }
                    });

                    btnComment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, Comments.class);
                            intent.putExtra("id", politicsPost.getId());
                            intent.putExtra("source", "politics");
                            context.startActivity(intent);
                        }
                    });

                    tvUserName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(context, ViewProfile.class);
                            intent.putExtra("profileEmail", poster.getEmail());
                            context.startActivity(intent);
                        }
                    });

                    btnShare.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_TEXT, politicsPost.getText());
                            context.startActivity(Intent.createChooser(shareIntent, "Share Text"));
                        }
                    });

                    if (edit){
                        menu.setVisibility(View.VISIBLE);
                        menu.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                selectOperation(politicsPost);
                            }
                        });
                    }
                }
            });
        }
    }

    public class PollViewHolder extends RecyclerView.ViewHolder{
        TextView tvText;
        ImageView profilePic, type;
        TextView tvUserName, tvPosition, tvPostTime, tvLikes, tvComments;
        ImageButton btnLike, btnComment, btnShare, menu;
        LinearLayout optionsLayout;

        public PollViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tvText);
            profilePic = itemView.findViewById(R.id.profilePic);
            type = itemView.findViewById(R.id.type);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvPosition = itemView.findViewById(R.id.tvPosition);
            tvPostTime = itemView.findViewById(R.id.tvPostTime);
            tvLikes = itemView.findViewById(R.id.tvLikes);
            tvComments = itemView.findViewById(R.id.tvComments);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
            btnShare = itemView.findViewById(R.id.btnShare);
            optionsLayout = itemView.findViewById(R.id.optionsLayout);
            menu = itemView.findViewById(R.id.menu);

            if (context instanceof Comments){
                btnComment.setEnabled(false);
            }

            btnShare.setEnabled(false);
        }

        public void bind(PoliticsPost politicsPost){
            politicsPost.getPoster(new PoliticsPost.SimpleCallback<Student>() {
                @Override public void callback(Student poster) {
                    if(poster.getProfileUrl() != null) {
                        Glide.with(itemView.getContext())
                                .load(poster.getProfileUrl())
                                .into(profilePic);
                    }
                    else {
                        Drawable drawable = DrawableCompat.wrap(AppCompatResources.getDrawable(context, R.drawable.ic_person3));
                        profilePic.setImageDrawable(drawable);
                    }

                    tvUserName.setText(poster.getUserName());

                    if(poster.getType().equals("leader")){
                        type.setVisibility(View.VISIBLE);
                        tvPosition.setText("Student Leader");
                    }
                    else {
                        type.setVisibility(View.GONE);
                        tvPosition.setVisibility(View.GONE);
                    }

                    tvText.setText(politicsPost.getText());

                    TimeCalc timeCalc = new TimeCalc();
                    String time = timeCalc.getTimeAgo(politicsPost.getTimestamp());
                    tvPostTime.setText(time);

                    politicsPost.getOptions(new PoliticsPost.SimpleCallback<Map<String, Integer>>() {
                        @Override
                        public void callback(Map<String, Integer> options) {
                            politicsPost.hasVoted(new PoliticsPost.SimpleCallback<Boolean>() {
                                @Override
                                public void callback(Boolean hasVoted) {
                                    if (hasVoted){
                                        optionsLayout.removeAllViews();
                                        int total = 0;
                                        Map<String, Integer> map = new HashMap<>();
                                        for (int num : options.values()){
                                            total += num;
                                        }

                                        int counter = 0;
                                        for (Map.Entry<String, Integer> set : options.entrySet()){
                                            String name = set.getKey();
                                            int votes = set.getValue();
                                            int percent = 0;
                                            if(votes != 0) {
                                                percent = ((votes * 100) / total);
                                            }

                                            map.put(name, percent);

                                            RelativeLayout relativeLayout = new RelativeLayout(context);
                                            int width = LinearLayout.LayoutParams.MATCH_PARENT;
                                            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
                                            params.setMargins(70, 0, 70, 20);
                                            relativeLayout.setLayoutParams(params);

                                            ProgressBar progressBar = new ProgressBar(context,  null, android.R.attr.progressBarStyleHorizontal);
                                            progressBar.setId(counter);
                                            int width1 = RelativeLayout.LayoutParams.MATCH_PARENT;
                                            int height1 = RelativeLayout.LayoutParams.WRAP_CONTENT;
                                            RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(width1, height);
                                            progressBar.setLayoutParams(params1);
                                            progressBar.setScaleY(8f);
                                            progressBar.setProgress(percent);
                                            params1.addRule(RelativeLayout.CENTER_IN_PARENT);
                                            relativeLayout.addView(progressBar);

                                            TextView textView = new TextView(context);
                                            textView.setId(counter);
                                            int width2 = RelativeLayout.LayoutParams.WRAP_CONTENT;
                                            int height2 = RelativeLayout.LayoutParams.WRAP_CONTENT;
                                            RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(width2, height2);
                                            textView.setText(name + " - " +percent + "%");
                                            textView.setTextColor(ContextCompat.getColor(context, R.color.textColor));
                                            textView.setTextSize(18);
                                            textView.setLayoutParams(params2);
                                            params2.addRule(RelativeLayout.CENTER_IN_PARENT);
                                            relativeLayout.addView(textView);
                                            optionsLayout.addView(relativeLayout);
                                            counter += 1;
                                        }
                                    }
                                    else {
                                        optionsLayout.removeAllViews();
                                        int counter = 0;
                                        for (Map.Entry<String, Integer> set : options.entrySet()) {
                                            String name = set.getKey();
                                            int votes = set.getValue();
                                            Button button = new Button(context);
                                            button.setId(counter);
                                            button.setText(name);
                                            int width = LinearLayout.LayoutParams.MATCH_PARENT;
                                            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
                                            params.setMargins(70, 0, 70, 10);
                                            button.setLayoutParams(params);

                                            button.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    DatabaseReference reference = firebaseDatabase.getReference().child("politicsPosts").child(politicsPost.getId()).child("voters");
                                                    Map<String, Object> map = new HashMap<>();
                                                    map.put("voter", email);
                                                    map.put("votedFor", name);
                                                    reference.push().setValue(map);
                                                }
                                            });
                                            optionsLayout.addView(button);
                                            counter += 1;
                                        }
                                    }
                                }
                            });
                        }
                    });

                    politicsPost.isLiked(new PoliticsPost.SimpleCallback<Boolean>() {
                        @Override
                        public void callback(Boolean isLiked) {
                            if (isLiked){
                                Drawable drawable = btnLike.getDrawable();
                                DrawableCompat.setTint(drawable, ContextCompat.getColor(context, R.color.primaryColor));
                            }
                            else {
                                Drawable drawable = DrawableCompat.wrap(AppCompatResources.getDrawable(context, R.drawable.ic_thumb_up));
                                btnLike.setImageDrawable(drawable);
                            }

                            btnLike.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    DatabaseReference reference = firebaseDatabase.getReference().child("politicsPosts").child(politicsPost.getId()).child("likes");
                                    if(isLiked){
                                        Query query = reference.orderByChild("id").equalTo(email);
                                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    for (DataSnapshot snap : snapshot.getChildren()) {
                                                        String key = snap.getKey();
                                                        DatabaseReference reference1 = reference.child(key);
                                                        reference1.removeValue();
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                            }
                                        });
                                    }
                                    else {
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("id", email);
                                        reference.push().setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                            }
                                        });
                                    }
                                }

                            });
                        }
                    });

                    politicsPost.getLikesCount(new PoliticsPost.SimpleCallback<Long>() {
                        @Override
                        public void callback(Long count) {
                            tvLikes.setText(String.valueOf(count));
                        }
                    });

                    politicsPost.getCommentsCount(new PoliticsPost.SimpleCallback<Long>() {
                        @Override
                        public void callback(Long count) {
                            tvComments.setText(String.valueOf(count));
                        }
                    });

                    profilePic.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, ViewImage.class);
                            intent.putExtra("url", poster.getProfileUrl());
                            context.startActivity(intent);

                        }
                    });

                    tvUserName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(context, ViewProfile.class);
                            intent.putExtra("profileEmail", poster.getEmail());
                            context.startActivity(intent);
                        }
                    });

                    btnComment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, Comments.class);
                            intent.putExtra("id", politicsPost.getId());
                            intent.putExtra("source", "politics");
                            context.startActivity(intent);
                        }
                    });

                    if (edit){
                        menu.setVisibility(View.VISIBLE);
                        menu.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                selectOperation(politicsPost);
                            }
                        });
                    }
                }
            });
        }
    }

    public void selectOperation(PoliticsPost politicsPost) {
        final CharSequence[] options = {"Delete","Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Option");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Delete")) {
                    deleteDialog = new Dialog(context);
                    deleteDialog.setContentView(R.layout.delete_dialog1);
                    deleteDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    deleteDialog.setCancelable(true);

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
                            StorageReference reference1 = null;
                            if(politicsPost.getVideoUrl() != null){
                                reference1 = firebaseStorage.getReferenceFromUrl(politicsPost.getVideoUrl());
                                reference1.delete();
                            }
                            if(politicsPost.getImageUrl() != null){
                                reference1 = firebaseStorage.getReferenceFromUrl(politicsPost.getImageUrl());
                                reference1.delete();
                            }

                            DatabaseReference reference = firebaseDatabase.getReference().child("politicsPosts").child(politicsPost.getId());
                            reference.removeValue();
                            deleteDialog.dismiss();
                        }
                    });

                }
                else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        return file;
    }

    public Uri getLocalBitmapUri(ImageView imageView) {
        Bitmap bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        Uri bmpUri = null;
        try {
            File file =  createImageFile();
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
            bmpUri = FileProvider.getUriForFile(context, "com.example.android.fileprovider", file);
            //bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    public void showDialog(PoliticsPost politicsPost, View view){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.share_video_popup, null);
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);
        popupWindow.setElevation(10);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        ProgressBar progressBar = popupView.findViewById(R.id.progressBar);
        Button btnCancel = popupView.findViewById(R.id.btnCancel);
        TextView tvProgress = popupView.findViewById(R.id.tvProgress);

        tvProgress.setText("0%");

        StorageReference reference = firebaseStorage.getReferenceFromUrl(politicsPost.getVideoUrl());
        try {
            File localFile = createImageFile();
            reference.getFile(localFile).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull FileDownloadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    int currentProgress = (int) progress;
                    progressBar.setProgress(currentProgress);
                    tvProgress.setText(String.valueOf(currentProgress) + "%");
                }
            }).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Uri uri = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                            ? FileProvider.getUriForFile(context, "com.example.android.fileprovider", localFile)
                            : Uri.fromFile(localFile);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("video/mp4");
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    if (politicsPost.getText() != null){
                        intent.putExtra(Intent.EXTRA_TEXT, politicsPost.getText());
                    }
                    context.startActivity(intent);
                    popupWindow.dismiss();
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

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (FileDownloadTask task : reference.getActiveDownloadTasks()){
                    task.cancel();
                    popupWindow.dismiss();
                }
            }
        });
    }

}
