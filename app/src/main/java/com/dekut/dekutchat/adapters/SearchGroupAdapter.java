package com.dekut.dekutchat.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dekut.dekutchat.R;
import com.dekut.dekutchat.activities.ViewImage;
import com.dekut.dekutchat.utils.Group;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.util.List;

public class SearchGroupAdapter extends FirebaseRecyclerAdapter<Group, SearchGroupAdapter.ViewHolder> {

    Context context;
    String email;

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public SearchGroupAdapter(@NonNull FirebaseRecyclerOptions<Group> options, Context context, String email) {
        super(options);
        this.context = context;
        this.email = email;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.groups_card_view, parent, false);
        SearchGroupAdapter.ViewHolder viewHolder = new SearchGroupAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Group model) {

        holder.tvGroupName.setText(model.getName());
        Glide.with(context)
                .load(model.getImageUrl())
                .into(holder.avatar);

        model.getMembersCount(new Group.SimpleCallback<Long>() {
            @Override
            public void callback(Long num) {
                holder.membersCount.setText(String.valueOf(num));

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, com.dekut.dekutchat.activities.ViewGroup.class);
                intent.putExtra("guid", model.getGroupId());
                context.startActivity(intent);
            }
        });

        holder.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewImage.class);
                intent.putExtra("url", model.getImageUrl());
                context.startActivity(intent);
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvGroupName, membersCount;
        ImageView avatar;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            membersCount = itemView.findViewById(R.id.membersCount);
            avatar = itemView.findViewById(R.id.avatar);
        }
    }
}


