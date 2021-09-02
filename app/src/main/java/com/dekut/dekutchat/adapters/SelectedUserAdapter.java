package com.dekut.dekutchat.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dekut.dekutchat.R;
import com.dekut.dekutchat.activities.SelectUser;
import com.dekut.dekutchat.activities.ViewImage;
import com.dekut.dekutchat.utils.Student;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SelectedUserAdapter extends RecyclerView.Adapter<SelectedUserAdapter.ViewHolder> {
    List<Student> students;
    Context context;
    String email;

    Activity activity;
    SelectUser selectUser;

    public SelectedUserAdapter(List<Student> students, Context context, String email){
        this.students = students;
        this.context = context;
        this.email = email;

        activity = (Activity) context;
        selectUser = (SelectUser) activity;
    }

    @NonNull
    @NotNull
    @Override
    public SelectedUserAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.selected_user_card, parent, false);
        SelectedUserAdapter.ViewHolder viewHolder = new SelectedUserAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull SelectedUserAdapter.ViewHolder holder, int position) {
        Student student = students.get(position);
        String username = student.getUserName();
        String email = student.getEmail();
        String picUrl = student.getProfileUrl();

        holder.tvUsername.setText(username);
        if (picUrl != null) {
            Glide.with(context)
                    .load(picUrl)
                    .into(holder.profilePic);
        }

        holder.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectUser.removeUser(student);
                selectUser.Uncheck(student);
            }
        });
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername;
        ImageView profilePic;
        ImageButton btnCancel;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            tvUsername = itemView.findViewById(R.id.tvUsername);
            profilePic = itemView.findViewById(R.id.profilePic);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }
}
