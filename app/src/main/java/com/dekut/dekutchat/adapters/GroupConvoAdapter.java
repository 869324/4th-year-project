package com.dekut.dekutchat.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dekut.dekutchat.R;
import com.dekut.dekutchat.utils.GetId;
import com.dekut.dekutchat.utils.GetTime;
import com.dekut.dekutchat.utils.Message;
import com.dekut.dekutchat.utils.Student;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GroupConvoAdapter extends RecyclerView.Adapter {
    List<Message> messageList;
    Context context;
    String groupId;
    long lastDate = 0;
    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    public GroupConvoAdapter(List<Message> messageList, Context context, String groupId) {
        this.messageList = messageList;
        this.context = context;
        this.groupId = groupId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        String senderId = message.getSenderId();
        String receiverId = message.getReceiverId();

        if (senderId.equals(email)){
            return VIEW_TYPE_MESSAGE_SENT;
        }
        else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = inflater.inflate(R.layout.sent_bubble, parent, false);
            return new SentViewHolder(view);
        }

        else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED){
            view = inflater.inflate(R.layout.received_bubble, parent, false);
            return new ReceivedViewHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        message.setConvoId(groupId);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentViewHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedViewHolder) holder).bind(message);
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
            String currentDate = getTime.getDate(System.currentTimeMillis());

            if(lastDate == 0){
                tvDate.setVisibility(View.VISIBLE);
                tvDate.setText(date);
                lastDate = message.getSentAt();
            }
            else if(getTime.sameDay(message.getSentAt(), lastDate)){
                //tvDate.setText(date);
                tvDate.setVisibility(View.INVISIBLE);
            }
            if(!getTime.sameDay(message.getSentAt(), lastDate)){
                tvDate.setVisibility(View.VISIBLE);
                tvDate.setText(date);
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
            String currentDate = getTime.getDate(System.currentTimeMillis());

            if(lastDate == 0){
                tvDate.setVisibility(View.VISIBLE);
                tvDate.setText(date);
                lastDate = message.getSentAt();
            }
            else if(getTime.sameDay(message.getSentAt(), lastDate)){
                //tvDate.setText(date);
                tvDate.setVisibility(View.INVISIBLE);
            }
            if(!getTime.sameDay(message.getSentAt(), lastDate)){
                tvDate.setVisibility(View.VISIBLE);
                tvDate.setText(date);
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
            message.addGroupReadAt();
            message.addLastRead(email);

        }
    }

}
