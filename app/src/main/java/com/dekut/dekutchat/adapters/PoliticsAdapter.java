package com.dekut.dekutchat.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

                    if(poster.getType().equals("Admin") || poster.getType().equals("leader")){
                        type.setVisibility(View.VISIBLE);
                        tvPosition.setText(poster.getType());
                    }
                    else {
                        type.setVisibility(View.INVISIBLE);
                        tvPosition.setVisibility(View.INVISIBLE);
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

                    if(poster.getType().equals("Admin") || poster.getType().equals("leader")){
                        type.setVisibility(View.VISIBLE);
                        tvPosition.setText(poster.getType());
                    }
                    else {
                        type.setVisibility(View.INVISIBLE);
                        tvPosition.setVisibility(View.INVISIBLE);
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

                    if(poster.getType().equals("Admin") || poster.getType().equals("leader")){
                        type.setVisibility(View.VISIBLE);
                        tvPosition.setText(poster.getType());
                    }
                    else {
                        type.setVisibility(View.INVISIBLE);
                        tvPosition.setVisibility(View.INVISIBLE);
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

}
