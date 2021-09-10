package com.dekut.dekutchat.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dekut.dekutchat.activities.Comments;
import com.dekut.dekutchat.activities.CreateHomePost;
import com.dekut.dekutchat.activities.Login;
import com.dekut.dekutchat.activities.SignUp;
import com.dekut.dekutchat.activities.UserChat;
import com.dekut.dekutchat.activities.ViewProfile;
import com.dekut.dekutchat.activities.ViewVideo;
import com.dekut.dekutchat.utils.HomePost;
import com.dekut.dekutchat.R;
import com.dekut.dekutchat.utils.PoliticsPost;
import com.dekut.dekutchat.utils.Student;
import com.dekut.dekutchat.utils.TimeCalc;
import com.dekut.dekutchat.activities.ViewImage;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import com.google.firebase.storage.StorageTask;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class HomeAdapter extends RecyclerView.Adapter{
    List<HomePost> posts;
    Context context;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    Dialog deleteDialog;
    String email;
    Activity activity;
    boolean edit;
    private static final int PLAIN_POST = 1;
    private static final int MEDIA_POST = 2;

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param 
     */
    public HomeAdapter(List<HomePost> posts, Context context,String email, boolean edit) {
        this.posts = posts;
        this.context = context;
        this.email = email;
        this.edit = edit;
        activity = (Activity) context;
    }

    @Override
    public int getItemViewType(int position) {
        HomePost homePost = posts.get(position);
        String imageUrl = homePost.getImageUrl();
        String videoUrl = homePost.getVideoUrl();

        if (imageUrl == null && videoUrl == null){
            return PLAIN_POST;
        }
        else {
            return MEDIA_POST;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == PLAIN_POST) {
            view = inflater.inflate(R.layout.home_plain_card_view, parent, false);
            return new HomeAdapter.PlainViewHolder(view);
        }

        else if (viewType == MEDIA_POST){
            view = inflater.inflate(R.layout.home_media_card_view, parent, false);
            return new HomeAdapter.MediaViewHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        HomePost homePost = posts.get(position);
        
        switch (holder.getItemViewType()) {
            case PLAIN_POST:
                ((HomeAdapter.PlainViewHolder) holder).bind(homePost);
                break;
            case MEDIA_POST:
                ((HomeAdapter.MediaViewHolder) holder).bind(homePost);
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
        
        public void bind(HomePost homePost){
            int imgHeight = homePost.getImgHeight();
            postImage.requestLayout();
            postImage.getLayoutParams().height = imgHeight;

            homePost.getStudent(new HomePost.SimpleCallback<Student>() {
                @Override public void callback(Student poster) {
                    if(poster.getProfileUrl() != null) {

                        if (poster.getProfileUrl() != null) {
                            Glide.with(itemView.getContext())
                                    .load(poster.getProfileUrl())
                                    .into(profilePic);
                        }
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

                    tvText.setText(homePost.getText());
                    if(homePost.getImageUrl() != null){
                        playCard.setVisibility(View.INVISIBLE);
                        Glide.with(itemView.getContext())
                                .load(homePost.getImageUrl())
                                .into(postImage);
                    }
                    if(homePost.getVideoUrl() != null){
                        playCard.setVisibility(View.VISIBLE);
                    }

                    TimeCalc timeCalc = new TimeCalc();
                    String time = timeCalc.getTimeAgo(homePost.getTimestamp());
                    tvPostTime.setText(time);

                    homePost.isLiked(new HomePost.SimpleCallback<Boolean>() {
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
                                    DatabaseReference reference = firebaseDatabase.getReference().child("homePosts").child(homePost.getId()).child("likes");
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

                    homePost.getLikesCount(new HomePost.SimpleCallback<Long>() {
                        @Override
                        public void callback(Long count) {
                            tvLikes.setText(String.valueOf(count));
                        }
                    });

                    homePost.getCommentsCount(new HomePost.SimpleCallback<Long>() {
                        @Override
                        public void callback(Long count) {
                            tvComments.setText(String.valueOf(count));
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

                    postImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(homePost.getVideoUrl() != null){
                                Intent intent = new Intent(context, ViewVideo.class);
                                intent.putExtra("videoUrl", homePost.getVideoUrl());
                                activity.startActivity(intent);
                            }
                            else if(homePost.getImageUrl() != null){
                                Intent intent = new Intent(context, ViewImage.class);
                                intent.putExtra("url", homePost.getImageUrl());
                                context.startActivity(intent);
                            }
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
                            intent.putExtra("id", homePost.getId());
                            intent.putExtra("source", "home");
                            context.startActivity(intent);
                        }
                    });

                    btnPlay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, ViewVideo.class);
                            intent.putExtra("videoUrl", homePost.getVideoUrl());
                            activity.startActivity(intent);
                        }
                    });

                    btnShare.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Uri bmpUri = getLocalBitmapUri(postImage);

                            if (homePost.getVideoUrl() == null) {
                                Intent shareIntent = new Intent();
                                shareIntent.setType("image/*");
                                shareIntent.setAction(Intent.ACTION_SEND);
                                shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);

                                if (homePost.getText() != null) {
                                    shareIntent.putExtra(Intent.EXTRA_TEXT, homePost.getText());
                                }

                                context.startActivity(Intent.createChooser(shareIntent, "Share Image"));

                            }

                            else {
                                showDialog(homePost, view);
                            }

                        }
                    });

                    if (edit){
                        menu.setVisibility(View.VISIBLE);
                        menu.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                selectOperation(homePost);
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
        
        public void bind(HomePost homePost){
            homePost.getStudent(new HomePost.SimpleCallback<Student>() {
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

                    tvText.setText(homePost.getText());
                    
                    TimeCalc timeCalc = new TimeCalc();
                    String time = timeCalc.getTimeAgo(homePost.getTimestamp());
                    tvPostTime.setText(time);

                    homePost.isLiked(new HomePost.SimpleCallback<Boolean>() {
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
                                    DatabaseReference reference = firebaseDatabase.getReference().child("homePosts").child(homePost.getId()).child("likes");
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

                    homePost.getLikesCount(new HomePost.SimpleCallback<Long>() {
                        @Override
                        public void callback(Long count) {
                            tvLikes.setText(String.valueOf(count));
                        }
                    });

                    homePost.getCommentsCount(new HomePost.SimpleCallback<Long>() {
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
                            intent.putExtra("id", homePost.getId());
                            intent.putExtra("source", "home");
                            context.startActivity(intent);
                        }
                    });

                    btnShare.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_TEXT, homePost.getText());
                            context.startActivity(Intent.createChooser(shareIntent, "Share Text"));
                        }
                    });

                    if (edit){
                        menu.setVisibility(View.VISIBLE);
                        menu.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                selectOperation(homePost);
                            }
                        });
                    }
                }
            });
        }
    }

    public void selectOperation(HomePost homePost) {
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
                            if(homePost.getVideoUrl() != null){
                                reference1 = firebaseStorage.getReferenceFromUrl(homePost.getVideoUrl());
                                reference1.delete();
                            }
                            if(homePost.getImageUrl() != null){
                                reference1 = firebaseStorage.getReferenceFromUrl(homePost.getImageUrl());
                                reference1.delete();
                            }

                            DatabaseReference reference = firebaseDatabase.getReference().child("homePosts").child(homePost.getId());
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

    public void showDialog(HomePost homePost, View view){
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

        StorageReference reference = firebaseStorage.getReferenceFromUrl(homePost.getVideoUrl());
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
                    if (homePost.getText() != null){
                        intent.putExtra(Intent.EXTRA_TEXT, homePost.getText());
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
