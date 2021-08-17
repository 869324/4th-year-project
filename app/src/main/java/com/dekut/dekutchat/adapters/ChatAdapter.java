package com.dekut.dekutchat.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dekut.dekutchat.R;
import com.dekut.dekutchat.activities.UserChat;
import com.dekut.dekutchat.activities.ViewImage;
import com.dekut.dekutchat.utils.Conversation;
import com.dekut.dekutchat.utils.GetTime;
import com.dekut.dekutchat.utils.Message;
import com.dekut.dekutchat.utils.Student;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    List<Conversation> conversations;
    Context context;
    String userEmail;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    public ChatAdapter(List<Conversation> conversations, Context context, String userEmail){
        this.conversations = conversations;
        this.context = context;
        this.userEmail = userEmail;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.chat_card_view, parent, false);
        ChatAdapter.ViewHolder viewHolder = new ChatAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);

        conversation.getConversationWith(new Conversation.SimpleCallback<Student>() {
            @Override
            public void callback(Student receiver) {
                holder.profilePic.setImageBitmap(null);
                holder.tvChatName.setText(receiver.getUserName());
                if (receiver.getProfileUrl() != null) {
                    Glide.with(context)
                            .load(receiver.getProfileUrl())
                            .into(holder.profilePic);
                }
                else {
                    Drawable drawable = DrawableCompat.wrap(AppCompatResources.getDrawable(context, R.drawable.ic_person3));
                    holder.profilePic.setImageDrawable(drawable);
                }

                conversation.getLastMessage(new Conversation.SimpleCallback<Message>() {
                    @Override
                    public void callback(Message lastMessage) {
                        GetTime getTime = new GetTime();
                        String deliveryTime = getTime.getTime(lastMessage.getSentAt());
                        String deliveryDate = getTime.getDate(lastMessage.getSentAt());
                        String currentDate = getTime.getDate(System.currentTimeMillis());

                        if(lastMessage.getMessageType().equals("text")){
                            holder.imageView.setVisibility(View.GONE);
                            holder.tvLastMsg.setText(lastMessage.getText());
                        }

                        else if(lastMessage.getMessageType().equals("file")){
                            holder.imageView.setVisibility(View.VISIBLE);
                            holder.imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_file));
                            if (lastMessage.getText() != null){
                                holder.tvLastMsg.setText(lastMessage.getText());
                            }
                        }

                        else if(lastMessage.getMessageType().equals("video")){
                            holder.imageView.setVisibility(View.VISIBLE);
                            holder.imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_video_library));
                            if (lastMessage.getText() != null){
                                holder.tvLastMsg.setText(lastMessage.getText());
                            }
                        }

                        else {
                            holder.imageView.setVisibility(View.VISIBLE);
                            holder.imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_gallery));
                            if (lastMessage.getText() != null){
                                holder.tvLastMsg.setText(lastMessage.getText());
                            }
                        }

                        if (currentDate.equals(deliveryDate)) {
                            holder.tvDeliveryTime.setText(deliveryTime);
                        } else {
                            holder.tvDeliveryTime.setText(deliveryDate);
                        }

                        Query query = firebaseDatabase.getReference().child("conversations").child(conversation.getConvoId()).child("messages").orderByChild("readAt").equalTo(0);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    int count = 0;
                                    for (DataSnapshot snap : snapshot.getChildren()){
                                        Message message = snap.getValue(Message.class);
                                        if(message.getReceiverId().equals(userEmail)){
                                            count += 1;
                                        }
                                    }
                                    if (count > 0){
                                        holder.badgeCard.setVisibility(View.VISIBLE);
                                        holder.chatBadge.setText(String.valueOf(count));
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        holder.profilePic.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(context, ViewImage.class);
                                intent.putExtra("url", receiver.getProfileUrl());
                                context.startActivity(intent);
                            }
                        });

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(context, UserChat.class);
                                intent.putExtra("email", receiver.getEmail());
                                intent.putExtra("url", receiver.getProfileUrl());
                                intent.putExtra("name", receiver.getUserName());
                                context.startActivity(intent);
                                holder.badgeCard.setVisibility(View.INVISIBLE);
                            }
                        });

                    }
                });
            }
        });

    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvChatName, tvLastMsg, tvDeliveryTime, chatBadge;
        ImageView profilePic, imageView;
        CardView badgeCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChatName = itemView.findViewById(R.id.tvChatName);
            tvLastMsg = itemView.findViewById(R.id.tvLastMsg);
            profilePic = itemView.findViewById(R.id.profilePic);
            tvDeliveryTime = itemView.findViewById(R.id.tvDeliveryTime);
            chatBadge = itemView.findViewById(R.id.chatBadge);
            badgeCard = itemView.findViewById(R.id.badgeCard);
            imageView = itemView.findViewById(R.id.imageView);
            badgeCard.setVisibility(View.INVISIBLE);
        }
    }


}
