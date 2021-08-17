package com.dekut.dekutchat.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dekut.dekutchat.R;
import com.dekut.dekutchat.activities.LoadMedia;
import com.dekut.dekutchat.activities.ViewImage;
import com.dekut.dekutchat.activities.ViewVideo;
import com.dekut.dekutchat.utils.GetId;
import com.dekut.dekutchat.utils.GetTime;
import com.dekut.dekutchat.utils.Message;
import com.dekut.dekutchat.utils.Student;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ConvoAdapter extends RecyclerView.Adapter{
    List<Message> messageList;
    Context context;
    String receiverEmail;
    long lastDate = 0;
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private static final int VIEW_TYPE_MESSAGE_SENT_MEDIA = 3;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED_MEDIA = 4;

    public ConvoAdapter(List<Message> messageList, Context context, String receiverEmail) {
        this.messageList = messageList;
        this.context = context;
        this.receiverEmail = receiverEmail;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        String senderId = message.getSenderId();

        if (senderId.equals(receiverEmail)){
            if (message.getMessageType().equals("text")) {
                return VIEW_TYPE_MESSAGE_RECEIVED;
            }
            else {
                return VIEW_TYPE_MESSAGE_RECEIVED_MEDIA;
            }
        }
        else {
            if(message.getMessageType().equals("text")) {
                return VIEW_TYPE_MESSAGE_SENT;
            }
            else {
                return VIEW_TYPE_MESSAGE_SENT_MEDIA;
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = inflater.inflate(R.layout.sent_bubble, parent, false);
            return new ConvoAdapter.SentViewHolder(view);
        }

        if (viewType == VIEW_TYPE_MESSAGE_RECEIVED){
            view = inflater.inflate(R.layout.received_bubble, parent, false);
            return new ConvoAdapter.ReceivedViewHolder(view);
        }

        if (viewType == VIEW_TYPE_MESSAGE_SENT_MEDIA) {
            view = inflater.inflate(R.layout.sent_bubble_media, parent, false);
            return new ConvoAdapter.SentMediaViewHolder(view);
        }

        if (viewType == VIEW_TYPE_MESSAGE_RECEIVED_MEDIA) {
            view = inflater.inflate(R.layout.received_bubble_media, parent, false);
            return new ConvoAdapter.ReceivedMediaViewHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        GetId getId = new GetId();
        message.setConvoId(getId.getId(message.getSenderId(), message.getReceiverId()));

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((ConvoAdapter.SentViewHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ConvoAdapter.ReceivedViewHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_SENT_MEDIA:
                ((ConvoAdapter.SentMediaViewHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED_MEDIA:
                ((ConvoAdapter.ReceivedMediaViewHolder) holder).bind(message);
        }
    }



    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class SentViewHolder extends RecyclerView.ViewHolder {
        TextView tvText, tvTime, tvDate, tvName;
        ImageView tick1, tick2;

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tvText);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvName = itemView.findViewById(R.id.tvName);
            tvDate= itemView.findViewById(R.id.tvDate);
            tick1 = itemView.findViewById(R.id.tick1);
            tick2 = itemView.findViewById(R.id.tick2);
        }

        void bind(Message message){
            GetTime getTime = new GetTime();
            String time = getTime.getTime(message.getSentAt());
            String date = getTime.getDate(message.getSentAt());
            long currentDate = System.currentTimeMillis();

            if(lastDate == 0){
                //tvDate.setVisibility(View.VISIBLE);
                //tvDate.setVisibility(View.VISIBLE);
                if (getTime.sameDay(message.getSentAt(), currentDate)){
                    tvDate.setText("Today");
                }
                else {
                    tvDate.setText(date);
                }
                lastDate = message.getSentAt();
            }
            else if(getTime.sameDay(message.getSentAt(), lastDate)){
                tvDate.setText("");
                //tvDate.setVisibility(View.GONE);
            }
            else if(!getTime.sameDay(message.getSentAt(), lastDate)){
                //tvDate.setVisibility(View.VISIBLE);
                if (getTime.sameDay(message.getSentAt(), currentDate)){
                    tvDate.setText("Today");
                }
                else {
                    tvDate.setText(date);
                }
                lastDate = message.getSentAt();
            }

            tvText.setText(message.getText());
            tvTime.setText(time);

            message.addReadListener(new Message.SimpleCallback<Long>() {
                @Override
                public void callback(Long readAt) {
                    if(readAt != 0) {
                        Drawable drawable1 = tick1.getDrawable();
                        Drawable drawable2 = tick2.getDrawable();
                        DrawableCompat.setTint(drawable1, ContextCompat.getColor(context, R.color.primaryColor));
                        DrawableCompat.setTint(drawable2, ContextCompat.getColor(context, R.color.primaryColor));
                    }

                }
            });
        }
    }

    public class ReceivedViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePic;
        TextView tvText, tvTime, tvDate, tvName;
        ImageView tick1, tick2;

        public ReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tvText);
            profilePic = itemView.findViewById(R.id.profilePic);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvName = itemView.findViewById(R.id.tvName);
            tvDate= itemView.findViewById(R.id.tvDate);
            tick1 = itemView.findViewById(R.id.tick1);
            tick2 = itemView.findViewById(R.id.tick2);
        }

        void bind(Message message){
            GetTime getTime = new GetTime();
            String time = getTime.getTime(message.getSentAt());
            String date = getTime.getDate(message.getSentAt());
            long currentDate = System.currentTimeMillis();

            if(lastDate == 0){
                //tvDate.setVisibility(View.VISIBLE);
                //tvDate.setVisibility(View.VISIBLE);
                if (getTime.sameDay(message.getSentAt(), currentDate)){
                    tvDate.setText("Today");
                }
                else {
                    tvDate.setText(date);
                }
                lastDate = message.getSentAt();
            }
            else if(getTime.sameDay(message.getSentAt(), lastDate)){
                tvDate.setText("");
                //tvDate.setVisibility(View.GONE);
            }
            else if(!getTime.sameDay(message.getSentAt(), lastDate)){
                //tvDate.setVisibility(View.VISIBLE);
                if (getTime.sameDay(message.getSentAt(), currentDate)){
                    tvDate.setText("Today");
                }
                else {
                    tvDate.setText(date);
                }
                lastDate = message.getSentAt();
            }

            message.getSender(new Message.SimpleCallback<Student>() {
                @Override
                public void callback(Student student) {
                    Glide.with(context)
                            .load(student.getProfileUrl())
                            .into(profilePic);
                    tvName.setText(student.getUserName());
                }
            });

            tvText.setText(message.getText());
            tvTime.setText(time);
            message.addReadAt();

        }
    }

    public class SentMediaViewHolder extends RecyclerView.ViewHolder {
        TextView tvText, tvTime, tvDate, tvName;
        ImageView imageView, tick1, tick2;
        ProgressBar progressBar;
        CardView playCard;
        ImageButton btnPlay;

        public SentMediaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tvText);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvName = itemView.findViewById(R.id.tvName);
            tvDate= itemView.findViewById(R.id.tvDate);
            tick1 = itemView.findViewById(R.id.tick1);
            tick2 = itemView.findViewById(R.id.tick2);
            imageView = itemView.findViewById(R.id.imageView);
            progressBar = itemView.findViewById(R.id.progressBar);
            playCard = itemView.findViewById(R.id.playCard);
            btnPlay = itemView.findViewById(R.id.btnPlay);
        }

        void bind(Message message){
            GetTime getTime = new GetTime();
            String time = getTime.getTime(message.getSentAt());
            String date = getTime.getDate(message.getSentAt());
            long currentDate = System.currentTimeMillis();

            if(lastDate == 0){
                //tvDate.setVisibility(View.VISIBLE);
                //tvDate.setVisibility(View.VISIBLE);
                if (getTime.sameDay(message.getSentAt(), currentDate)){
                    tvDate.setText("Today");
                }
                else {
                    tvDate.setText(date);
                }
                lastDate = message.getSentAt();
            }
            else if(getTime.sameDay(message.getSentAt(), lastDate)){
                tvDate.setText("");
                //tvDate.setVisibility(View.GONE);
            }
            else if(!getTime.sameDay(message.getSentAt(), lastDate)){
                //tvDate.setVisibility(View.VISIBLE);
                if (getTime.sameDay(message.getSentAt(), currentDate)){
                    tvDate.setText("Today");
                }
                else {
                    tvDate.setText(date);
                }
                lastDate = message.getSentAt();
            }

            tvText.setText(message.getText());
            tvTime.setText(time);

            if (message.getMessageType().equals("image")){
                playCard.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                Glide.with(context)
                        .load(message.getImageUrl())
                        .into(imageView);
            }

            if (message.getMessageType().equals("video")){
                playCard.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                Glide.with(context)
                        .load(message.getImageUrl())
                        .into(imageView);
            }

            if (message.getMessageType().equals("file")){
                playCard.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_file));
            }

            message.addReadListener(new Message.SimpleCallback<Long>() {
                @Override
                public void callback(Long readAt) {
                    if(readAt != 0) {
                        Drawable drawable1 = tick1.getDrawable();
                        Drawable drawable2 = tick2.getDrawable();
                        DrawableCompat.setTint(drawable1, ContextCompat.getColor(context, R.color.primaryColor));
                        DrawableCompat.setTint(drawable2, ContextCompat.getColor(context, R.color.primaryColor));
                    }

                }
            });

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(message.getMessageType().equals("image")){
                        Intent intent = new Intent(context, ViewImage.class);
                        intent.putExtra("url", message.getImageUrl());
                        context.startActivity(intent);
                    }

                    if(message.getMessageType().equals("video")){
                        Intent intent = new Intent(context, ViewVideo.class);
                        intent.putExtra("videoUrl", message.getVideoUrl());
                        context.startActivity(intent);
                    }
                }
            });

            btnPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ViewVideo.class);
                    intent.putExtra("videoUrl", message.getVideoUrl());
                    context.startActivity(intent);
                }
            });
        }
    }

    public class ReceivedMediaViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePic;
        TextView tvText, tvTime, tvDate, tvName;
        ImageView imageView, tick1, tick2;
        ProgressBar progressBar;
        ImageButton btnPlay;
        CardView playCard;

        public ReceivedMediaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tvText);
            profilePic = itemView.findViewById(R.id.profilePic);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvName = itemView.findViewById(R.id.tvName);
            tvDate= itemView.findViewById(R.id.tvDate);
            tick1 = itemView.findViewById(R.id.tick1);
            tick2 = itemView.findViewById(R.id.tick2);
            imageView = itemView.findViewById(R.id.imageView);
            progressBar = itemView.findViewById(R.id.progressBar);
            playCard = itemView.findViewById(R.id.playCard);
            btnPlay = itemView.findViewById(R.id.btnPlay);
        }

        void bind(Message message){
            GetTime getTime = new GetTime();
            String time = getTime.getTime(message.getSentAt());
            String date = getTime.getDate(message.getSentAt());
            long currentDate = System.currentTimeMillis();

            if(lastDate == 0){
                //tvDate.setVisibility(View.VISIBLE);
                //tvDate.setVisibility(View.VISIBLE);
                if (getTime.sameDay(message.getSentAt(), currentDate)){
                    tvDate.setText("Today");
                }
                else {
                    tvDate.setText(date);
                }
                lastDate = message.getSentAt();
            }
            else if(getTime.sameDay(message.getSentAt(), lastDate)){
                tvDate.setText("");
                //tvDate.setVisibility(View.GONE);
            }
            else if(!getTime.sameDay(message.getSentAt(), lastDate)){
                //tvDate.setVisibility(View.VISIBLE);
                if (getTime.sameDay(message.getSentAt(), currentDate)){
                    tvDate.setText("Today");
                }
                else {
                    tvDate.setText(date);
                }
                lastDate = message.getSentAt();
            }

            if (message.getMessageType().equals("image")){
                playCard.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                Glide.with(context)
                        .load(message.getImageUrl())
                        .into(imageView);
            }

            if (message.getMessageType().equals("video")){
                playCard.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                Glide.with(context)
                        .load(message.getImageUrl())
                        .into(imageView);
            }

            if (message.getMessageType().equals("file")){
                playCard.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_file));
            }

            message.getSender(new Message.SimpleCallback<Student>() {
                @Override
                public void callback(Student student) {
                    Glide.with(context)
                            .load(student.getProfileUrl())
                            .into(profilePic);
                    tvName.setText(student.getUserName());
                }
            });

            tvText.setText(message.getText());
            tvTime.setText(time);
            message.addReadAt();

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ViewImage.class);
                    intent.putExtra("url", message.getImageUrl());
                    context.startActivity(intent);
                }
            });

            btnPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ViewVideo.class);
                    intent.putExtra("videoUrl", message.getImageUrl());
                    context.startActivity(intent);
                }
            });

        }
    }
}
