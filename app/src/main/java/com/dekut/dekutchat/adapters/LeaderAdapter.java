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
import com.dekut.dekutchat.activities.UserChat;
import com.dekut.dekutchat.utils.Student;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class LeaderAdapter extends RecyclerView.Adapter<LeaderAdapter.ViewHolder> {
    List<Student> leaders;
    Context context;

    public LeaderAdapter(List<Student> leaders, Context context){
        this.leaders = leaders;
        this.context = context;
    }

    @NonNull
    @NotNull
    @Override
    public LeaderAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.student_leader_card, parent, false);
        LeaderAdapter.ViewHolder viewHolder = new LeaderAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull LeaderAdapter.ViewHolder holder, int position) {
        Student student = leaders.get(position);

        if (student.getProfileUrl() != null){
            Glide.with(context)
                    .load(student.getProfileUrl())
                    .into(holder.profilePic);
        }

        holder.tvUsername.setText(student.getUserName());
        holder.tvEmail.setText(student.getEmail());
        holder.tvPosition.setText(student.getPosition());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, UserChat.class);
                intent.putExtra("email", student.getEmail());
                intent.putExtra("name", student.getUserName());
                intent.putExtra("url", student.getProfileUrl());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return leaders.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvEmail, tvPosition;
        ImageView profilePic;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            profilePic = itemView.findViewById(R.id.profilePic);
            tvPosition = itemView.findViewById(R.id.tvPosition);
        }
    }
}
