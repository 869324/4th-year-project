package com.dekut.dekutchat.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dekut.dekutchat.R;
import com.dekut.dekutchat.activities.ViewImage;
import com.dekut.dekutchat.activities.ViewVideo;
import com.dekut.dekutchat.utils.HomePost;
import com.dekut.dekutchat.utils.Student;
import com.dekut.dekutchat.utils.TimeCalc;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfilePostAdapter extends RecyclerView.Adapter<ProfilePostAdapter.ViewHolder> {

    List<HomePost> homePosts;
    Context context;
    Activity activity;
    Dialog deleteDialog;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    String email;

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param 
     */
    public ProfilePostAdapter(List<HomePost> homePosts, Context context, String email) {
        this.homePosts = homePosts;
        this.context = context;
        this.email = email;
        activity = (Activity) context;
    }

    @NonNull
    @Override
    public ProfilePostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.profile_card_view, parent, false);
        ProfilePostAdapter.ViewHolder viewHolder = new ProfilePostAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HomePost homePost = homePosts.get(position);
        
        int imgHeight = homePost.getImgHeight();
        holder.postImage.requestLayout();
        holder.postImage.getLayoutParams().height = imgHeight;

        homePost.getStudent(new HomePost.SimpleCallback<Student>() {
            @Override public void callback(Student poster) {
                if(poster.getProfileUrl() != null) {
                    Glide.with(holder.itemView.getContext())
                            .load(poster.getProfileUrl())
                            .into(holder.profilePic);
                }
                else {
                    Drawable drawable = DrawableCompat.wrap(AppCompatResources.getDrawable(context, R.drawable.ic_person3));
                    holder.profilePic.setImageDrawable(drawable);
                }

                holder.tvUserName.setText(poster.getUserName());

                if(poster.getType().equals("Admin") || poster.getType().equals("leader")){
                    holder.type.setVisibility(View.VISIBLE);
                    holder.tvPosition.setText(poster.getType());
                }
                else {
                    holder.type.setVisibility(View.INVISIBLE);
                    holder.tvPosition.setVisibility(View.INVISIBLE);
                }

                holder.tvText.setText(homePost.getText());
                if(homePost.getImageUrl() != null){
                    holder.playCard.setVisibility(View.INVISIBLE);
                    Glide.with(holder.itemView.getContext())
                            .load(homePost.getImageUrl())
                            .into(holder.postImage);
                }
                if(homePost.getVideoUrl() != null){
                    holder.playCard.setVisibility(View.VISIBLE);
                }

                TimeCalc timeCalc = new TimeCalc();
                String time = timeCalc.getTimeAgo(homePost.getTimestamp());
                holder.tvPostTime.setText(time);

                homePost.isLiked(new HomePost.SimpleCallback<Boolean>() {
                    @Override
                    public void callback(Boolean isLiked) {
                        if (isLiked){
                            Drawable drawable = holder.btnLike.getDrawable();
                            DrawableCompat.setTint(drawable, ContextCompat.getColor(context, R.color.primaryColor));
                        }
                        else {
                            Drawable drawable = DrawableCompat.wrap(AppCompatResources.getDrawable(context, R.drawable.ic_thumb_up));
                            holder.btnLike.setImageDrawable(drawable);
                        }

                        holder.btnLike.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                DatabaseReference reference = firebaseDatabase.getReference().child("homePosts").child(homePost.getId()).child("likes");
                                if(isLiked){
                                    Query query = reference.orderByChild("id").equalTo(email);
                                    DatabaseReference reference1 = query.getRef();
                                    reference1.removeValue();
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

                holder.postImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(homePost.getVideoUrl() != null){
                            Intent intent = new Intent(context, ViewVideo.class);
                            intent.putExtra("videoUrl", homePost.getVideoUrl());
                            activity.startActivity(intent);
                        }
                        else if(homePost.getImageUrl() != null) {
                            Intent intent = new Intent(context, ViewImage.class);
                            intent.putExtra("url", homePost.getImageUrl());
                            context.startActivity(intent);
                        }
                    }
                });

                homePost.getLikesCount(new HomePost.SimpleCallback<Long>() {
                    @Override
                    public void callback(Long likes) {
                        holder.tvLikes.setText(String.valueOf(likes));
                    }
                });



                holder.profilePic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ViewImage.class);
                        intent.putExtra("url", poster.getProfileUrl());
                        context.startActivity(intent);

                    }
                });

                holder.btnComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

                holder.btnPlay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ViewVideo.class);
                        intent.putExtra("videoUrl", homePost.getVideoUrl());
                        activity.startActivity(intent);
                    }
                });

                holder.menu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectOperation(homePost);
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return homePosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvText;
        CardView playCard;
        ImageView profilePic, type, postImage;
        TextView tvUserName, tvPosition, tvPostTime, tvLikes, tvComments;
        ImageButton btnLike, btnComment, btnShare, menu, btnPlay;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
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
            menu = itemView.findViewById(R.id.menu);
            playCard = itemView.findViewById(R.id.playCard);
            btnPlay = itemView.findViewById(R.id.btnPlay);
            progressBar = itemView.findViewById(R.id.progressBar);

        }
    }

    public void selectOperation(HomePost homePost) {
        final CharSequence[] options = {"Edit", "Delete","Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Option");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Edit")) {


                }
                else if (options[item].equals("Delete")) {
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

}
