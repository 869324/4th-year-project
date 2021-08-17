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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroupMembersAdapter extends RecyclerView.Adapter<GroupMembersAdapter.Viewholder> {


    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {

    }


    @Override
    public int getItemCount() {
        return 0;
    }

    public class Viewholder extends RecyclerView.ViewHolder{
        public Viewholder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
