package com.dekut.dekutchat.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dekut.dekutchat.R;
import com.dekut.dekutchat.activities.UserChat;
import com.dekut.dekutchat.activities.ViewImage;
import com.dekut.dekutchat.activities.ViewProfile;
import com.dekut.dekutchat.utils.HomePost;
import com.dekut.dekutchat.utils.Student;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchUserAdapter extends RecyclerView.Adapter<SearchUserAdapter.ViewHolder> {
    List<Student> students;
    Context context;
    PopupWindow popupWindow;

    public SearchUserAdapter(List<Student> students, Context context, PopupWindow popupWindow){
        this.students = students;
        this.context = context;
        this.popupWindow = popupWindow;
    }

    public SearchUserAdapter(List<Student> students, Context context){
        this.students = students;
        this.context = context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.search_user_card_view, parent, false);
        SearchUserAdapter.ViewHolder viewHolder = new SearchUserAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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
                if (popupWindow != null) {
                    Intent intent = new Intent(context, UserChat.class);
                    intent.putExtra("email", email);
                    intent.putExtra("name", username);
                    intent.putExtra("url", picUrl);
                    context.startActivity(intent);
                    popupWindow.dismiss();
                }

                else {
                    Intent intent = new Intent(context, ViewProfile.class);
                    intent.putExtra("profileEmail", email);
                    context.startActivity(intent);
                }

            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvEmail;
        ImageView profilePic;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            profilePic = itemView.findViewById(R.id.profilePic);
        }
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

}
