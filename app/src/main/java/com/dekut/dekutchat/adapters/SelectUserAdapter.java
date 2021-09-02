package com.dekut.dekutchat.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dekut.dekutchat.R;
import com.dekut.dekutchat.activities.UserChat;
import com.dekut.dekutchat.activities.ViewImage;
import com.dekut.dekutchat.activities.ViewProfile;
import com.dekut.dekutchat.utils.Student;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SelectUserAdapter extends RecyclerView.Adapter<SelectUserAdapter.ViewHolder> {
    List<Student> students;
    Context context;
    String email;

    public SelectUserAdapter(List<Student> students, Context context, String email){
        this.students = students;
        this.context = context;
        this.email = email;
    }

    @NonNull
    @NotNull
    @Override
    public SelectUserAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.select_user_card, parent, false);
        SelectUserAdapter.ViewHolder viewHolder = new SelectUserAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull SelectUserAdapter.ViewHolder holder, int position) {
        Student student = students.get(position);
        String username = student.getUserName();
        String email = student.getEmail();
        String picUrl = student.getProfileUrl();

        holder.tvUsername.setText(username);
        holder.tvEmail.setText(email);
        if (picUrl != null) {
            Glide.with(context)
                    .load(picUrl)
                    .into(holder.profilePic);
        }

        holder.profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewImage.class);
                intent.putExtra("url", picUrl);
                context.startActivity(intent);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.almostWhite));
            }
        });
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvEmail;
        ImageView profilePic;
        CheckBox checkBox;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            profilePic = itemView.findViewById(R.id.profilePic);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
}
