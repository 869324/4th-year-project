package com.dekut.dekutchat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dekut.dekutchat.R;
import com.dekut.dekutchat.utils.Comment;
import com.dekut.dekutchat.utils.GetTime;
import com.dekut.dekutchat.utils.Student;
import com.dekut.dekutchat.utils.TimeCalc;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {
    List<Comment> comments;
    Context context;

    public CommentsAdapter(List<Comment> comments, Context context){
        this.comments = comments;
        this.context = context;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.comment_card_view, parent, false);
        CommentsAdapter.ViewHolder viewHolder = new CommentsAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        Comment comment = comments.get(position);

        comment.getPoster(new Comment.SimpleCallback<Student>() {
            @Override
            public void callback(Student student) {
                holder.tvName.setText(student.getUserName());
                Glide.with(context)
                        .load(student.getProfileUrl())
                        .into(holder.profilePic);
            }
        });

        holder.tvText.setText(comment.getText());

        TimeCalc timeCalc = new TimeCalc();
        String time = timeCalc.getTimeAgo(comment.getTimestamp());
        holder.tvPostTime.setText(time);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePic;
        TextView tvName, tvPostTime, tvText;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            profilePic = itemView.findViewById(R.id.profilePic);
            tvName = itemView.findViewById(R.id.tvName);
            tvPostTime = itemView.findViewById(R.id.tvPostTime);
            tvText = itemView.findViewById(R.id.tvText);
        }
    }
}
