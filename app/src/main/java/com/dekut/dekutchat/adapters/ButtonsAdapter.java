package com.dekut.dekutchat.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dekut.dekutchat.R;
import com.dekut.dekutchat.activities.SelectUser;

import java.util.List;
import java.util.Map;

public class ButtonsAdapter extends BaseAdapter {
    List<Map> entries;
    Context context;
    String groupId;
    LayoutInflater layoutInflater;

    public ButtonsAdapter(@NonNull Context context, List<Map> entries, String groupId) {
        this.entries = entries;
        this.context = context;
        this.groupId = groupId;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return entries.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }


    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = layoutInflater.inflate(R.layout.buttons_card_view, null);

        Map<String, Object> map = entries.get(i);
        TextView textView = view.findViewById(R.id.textView);
        ImageView imageView = view.findViewById(R.id.imageView);

        imageView.setImageResource(Integer.parseInt(map.get("imgId").toString()));
        textView.setText(String.valueOf(map.get("text")));

        String text = String.valueOf(map.get("text"));

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (text.equals("Add Members")){
                    Intent intent = new Intent(context, SelectUser.class);
                    intent.putExtra("operation", "addMembers");
                    intent.putExtra("groupId", groupId);
                    context.startActivity(intent);
                }
                else if (text.equals("Add Admins")){
                    Intent intent = new Intent(context, SelectUser.class);
                    intent.putExtra("operation", "addAdmins");
                    intent.putExtra("groupId", groupId);
                    context.startActivity(intent);
                }
                /*else if (text.equals("Remove Members")){
                    operation = "removeMembers";
                }
                else if (text.equals("Remove Admins")){
                    operation = "removeAdmins";
                }
                else if (text.equals("Add Password")){
                    operation = "addMembers";
                }
                else if (text.equals("Change Password")){
                    operation = "addMembers";
                }
                else if (text.equals("Remove Password")){
                    operation = "addMembers";
                }
                else if (text.equals("Delete Group")){
                    operation = "addMembers";
                }*/
            }
        });


        return view;

    }
}
