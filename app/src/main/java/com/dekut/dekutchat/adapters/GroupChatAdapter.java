package com.dekut.dekutchat.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dekut.dekutchat.R;
import com.dekut.dekutchat.activities.GroupChat;
import com.dekut.dekutchat.activities.UserChat;
import com.dekut.dekutchat.activities.ViewImage;
import com.dekut.dekutchat.utils.Conversation;
import com.dekut.dekutchat.utils.GetTime;
import com.dekut.dekutchat.utils.Group;
import com.dekut.dekutchat.utils.Message;
import com.dekut.dekutchat.utils.Student;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.ViewHolder> {
    List<Conversation> conversations;
    String email;
    Context context;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param
     */
    public GroupChatAdapter(List<Conversation> conversations, Context context, String email) {
        this.conversations = conversations;
        this.context = context;
        this.email = email;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.group_chat_card_view, parent, false);
        GroupChatAdapter.ViewHolder viewHolder = new GroupChatAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);

        conversation.getGroup(new Conversation.SimpleCallback<Group>() {
            @Override
            public void callback(Group group) {
                holder.profilePic.setImageBitmap(null);
                holder.tvChatName.setText(group.getName());
                if (group.getImageUrl() != null) {
                    Glide.with(context)
                            .load(group.getImageUrl())
                            .into(holder.profilePic);
                }
                else {
                    Drawable drawable = DrawableCompat.wrap(AppCompatResources.getDrawable(context, R.drawable.ic_group));
                    holder.profilePic.setImageDrawable(drawable);
                }

                conversation.getGroupLastMessage(new Conversation.SimpleCallback<Message>() {
                    @Override
                    public void callback(Message lastMessage) {
                        if(lastMessage != null) {
                            GetTime getTime = new GetTime();
                            String deliveryTime = getTime.getTime(lastMessage.getSentAt());
                            String deliveryDate = getTime.getDate(lastMessage.getSentAt());
                            String currentDate = getTime.getDate(System.currentTimeMillis());

                            holder.tvLastMsg.setText(lastMessage.getText());

                            if (currentDate.equals(deliveryDate)) {
                                holder.tvDeliveryTime.setText(deliveryTime);
                            } else {
                                holder.tvDeliveryTime.setText(deliveryDate);
                            }
                        }
                        else {
                            holder.tvLastMsg.setText("You joined this group");
                            holder.tvDeliveryTime.setText("...");
                        }

                        Query query = firebaseDatabase.getReference().child("groupConversations").child(conversation.getConvoId()).child("messages").limitToLast(100);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    conversation.getGroupLastRead(new Conversation.SimpleCallback<Long>() {
                                        @Override
                                        public void callback(Long lastRead) {
                                            int count = 0;
                                            for (DataSnapshot snap : snapshot.getChildren()){
                                                Message message = snap.getValue(Message.class);
                                                if(message.getSentAt() > lastRead && !message.getSenderId().equals(email)){
                                                    count += 1;
                                                }
                                            }
                                            if (count > 0){
                                                holder.badgeCard.setVisibility(View.VISIBLE);
                                                holder.chatBadge.setText(String.valueOf(count));
                                            }
                                        }
                                    });

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });

                holder.profilePic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ViewImage.class);
                        intent.putExtra("url", group.getImageUrl());
                        context.startActivity(intent);
                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, GroupChat.class);
                        intent.putExtra("guid", group.getGroupId());
                        intent.putExtra("url", group.getImageUrl());
                        intent.putExtra("name", group.getName());
                        context.startActivity(intent);
                        holder.badgeCard.setVisibility(View.INVISIBLE);
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
        ImageView profilePic;
        CardView badgeCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChatName = itemView.findViewById(R.id.tvChatName);
            tvLastMsg = itemView.findViewById(R.id.tvLastMsg);
            profilePic = itemView.findViewById(R.id.profilePic);
            tvDeliveryTime = itemView.findViewById(R.id.tvDeliveryTime);
            chatBadge = itemView.findViewById(R.id.chatBadge);
            badgeCard = itemView.findViewById(R.id.badgeCard);
            badgeCard.setVisibility(View.INVISIBLE);
        }
    }
}
