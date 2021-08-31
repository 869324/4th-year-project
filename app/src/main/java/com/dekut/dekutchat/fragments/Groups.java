package com.dekut.dekutchat.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;

import com.dekut.dekutchat.R;
import com.dekut.dekutchat.activities.CreateGroup;
import com.dekut.dekutchat.adapters.GroupChatAdapter;
import com.dekut.dekutchat.adapters.GroupChatAdapter;
import com.dekut.dekutchat.adapters.ConvoAdapter;
import com.dekut.dekutchat.adapters.SearchGroupAdapter;
import com.dekut.dekutchat.utils.Conversation;
import com.dekut.dekutchat.utils.Group;
import com.dekut.dekutchat.utils.Student;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Groups#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Groups extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    RecyclerView chatRecyclerView, groupRecyclerView;
    ProgressBar progressBar, searchUserProgressBar;
    FloatingActionButton fabAddGroup;
    Toolbar toolbar;
    SearchView groupSearchView;
    ImageButton btnDismissPopup;
    Button newGroup;

    LinearLayoutManager linearLayoutManager1;
    LinearLayoutManager linearLayoutManager2;
    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    GroupChatAdapter groupChatAdapter;
    SearchGroupAdapter searchGroupAdapter;
    Context context;
    List<Group> groups = new ArrayList<>();
    String key = null;
    String keyword = null;
    Query groupQuery;
    boolean isLoading = false;
    List<Conversation> conversations = new ArrayList<>();
    List<String> keys = new ArrayList<>();
    List<String> groupKeys = new ArrayList<>();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Groups() {
        // Required empty public constructor
    }
    public Groups(String email){
        this.email = email;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Groups.
     */
    // TODO: Rename and change types and number of parameters
    public static Groups newInstance(String param1, String param2) {
        Groups fragment = new Groups();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = container.getContext();
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        chatRecyclerView = view.findViewById(R.id.groupMainRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        fabAddGroup = view.findViewById(R.id.fabAddGroup);
        toolbar = getActivity().findViewById(R.id.toolbar);

        linearLayoutManager1 = new LinearLayoutManager(context);
        linearLayoutManager1.setReverseLayout(true);
        linearLayoutManager1.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(linearLayoutManager1);
        groupChatAdapter = new GroupChatAdapter(conversations, context, email);
        groupChatAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        chatRecyclerView.setAdapter(groupChatAdapter);

        fetchChats();

        fabAddGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.search_popup, null);
                int width = LinearLayout.LayoutParams.MATCH_PARENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                PopupWindow popupWindow = new PopupWindow(popupView, width, 1000, true);
                popupWindow.setElevation(10);
                popupWindow.showAsDropDown(toolbar);
                popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

                groupSearchView = popupView.findViewById(R.id.userSearchView);
                groupRecyclerView = popupView.findViewById(R.id.userRecyclerView);
                btnDismissPopup = popupView.findViewById(R.id.btnDismissPopup);
                searchUserProgressBar = popupView.findViewById(R.id.searchUserProgressBar);
                newGroup = popupView.findViewById(R.id.newGroup);
                newGroup.setVisibility(View.VISIBLE);

                linearLayoutManager2 = new LinearLayoutManager(context);
                groupRecyclerView.setLayoutManager(linearLayoutManager2);
                searchGroupAdapter = new SearchGroupAdapter(groups, context, email);
                searchGroupAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
                searchUserProgressBar.setVisibility(View.INVISIBLE);
                groupRecyclerView.setAdapter(searchGroupAdapter);

                fetchGroups();

                groupRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(@NonNull @NotNull RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        LinearLayoutManager linearLayoutManager1 = (LinearLayoutManager) recyclerView.getLayoutManager();
                        int lastPosition = linearLayoutManager1.findLastCompletelyVisibleItemPosition();
                        int totalItems = linearLayoutManager1.getItemCount();

                        if ((totalItems -  lastPosition) < 3){
                            if (!isLoading) {
                                isLoading = true;
                                fetchGroups();
                            }
                        }
                    }
                });

                groupSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        keyword = query.toLowerCase();
                        key = null;
                        groups.clear();
                        groupKeys.clear();
                        searchGroupAdapter.notifyDataSetChanged();
                        fetchGroups();
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        keyword = newText.toLowerCase();
                        key = null;
                        groups.clear();
                        groupKeys.clear();
                        searchGroupAdapter.notifyDataSetChanged();
                        fetchGroups();
                        return true;
                    }
                });

                newGroup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, CreateGroup.class);
                        startActivity(intent);
                    }
                });

                btnDismissPopup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void fetchGroups(){
        if (key == null) {
            groupQuery = firebaseDatabase.getReference().child("groups").orderByKey().limitToFirst(100);
        }

        else {
            groupQuery = firebaseDatabase.getReference().child("groups").orderByKey().limitToFirst(100).startAt(key);
        }

        groupQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        Group group = snap.getValue(Group.class);

                        if (group.getType().equals("public") && !groupKeys.contains(group.getGroupId())) {
                            if (keyword == null) {
                                groups.add(group);
                                groupKeys.add(group.getGroupId());
                                searchGroupAdapter.notifyItemInserted(groups.size() - 1);
                            }

                            else {
                                String name = group.getName().toLowerCase();
                                String description = group.getDescription().toLowerCase();

                                if (name.contains(keyword) || description.contains(keyword)) {
                                    groups.add(group);
                                    groupKeys.add(group.getGroupId());
                                    searchGroupAdapter.notifyItemInserted(groups.size() - 1);
                                }
                            }
                        }
                        key = group.getGroupId();
                    }

                    if (groups.isEmpty()){
                        fetchGroups();
                    }
                    isLoading = false;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void fetchChats(){
        Query query = firebaseDatabase.getReference().child("groupConversations").orderByChild("lastMessageT");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()){
                    Conversation conversation = snap.getValue(Conversation.class);
                    Log.e("myTag", conversation.toString());
                    conversation.getGroup(new Conversation.SimpleCallback<Group>() {
                        @Override
                        public void callback(Group group) {
                            group.isJoined(new Group.SimpleCallback<Boolean>() {
                                @Override
                                public void callback(Boolean isJoined) {
                                    if (isJoined) {
                                        if (!keys.contains(conversation.getConvoId())) {
                                            conversations.add(conversation);
                                            keys.add(conversation.getConvoId());
                                            groupChatAdapter.notifyItemInserted(conversations.size() - 1);
                                        }
                                    }
                                }
                            });

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        /*query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Conversation conversation = snapshot.getValue(Conversation.class);
                conversation.getGroup(new Conversation.SimpleCallback<Group>() {
                    @Override
                    public void callback(Group group) {
                        group.isJoined(new Group.SimpleCallback<Boolean>() {
                            @Override
                            public void callback(Boolean isJoined) {
                                if (isJoined) {
                                    if (!keys.contains(conversation.getConvoId())) {
                                        conversations.add(conversation);
                                        keys.add(conversation.getConvoId());
                                        groupChatAdapter.notifyItemInserted(conversations.size() - 1);
                                    }
                                }
                            }
                        });

                    }
                });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Conversation conversation = snapshot.getValue(Conversation.class);
                conversation.getGroup(new Conversation.SimpleCallback<Group>() {
                    @Override
                    public void callback(Group group) {
                        group.isJoined(new Group.SimpleCallback<Boolean>() {
                            @Override
                            public void callback(Boolean isJoined) {
                                if (isJoined) {
                                    for (Conversation conversation1 : conversations) {
                                        if (conversation.getConvoId().equals(conversation1.getConvoId())) {
                                            int index = conversations.indexOf(conversation1);
                                            conversations.remove(index);
                                            groupChatAdapter.notifyItemRemoved(index);
                                            conversations.add(conversation);
                                            groupChatAdapter.notifyItemInserted(conversations.size() - 1);
                                            break;
                                        }
                                    }
                                }

                                else {
                                    int index = conversations.indexOf(conversation);
                                    conversations.remove(index);
                                    groupChatAdapter.notifyItemRemoved(index);
                                }
                            }
                        });

                    }
                });

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Conversation conversation = snapshot.getValue(Conversation.class);
                for(Conversation conversation1 : conversations){
                    if(conversation.getConvoId().equals(conversation1.getConvoId())){
                        int index = conversations.indexOf(conversation1);
                        int index2 = keys.indexOf(conversation1.getConvoId());
                        conversations.remove(index);
                        keys.remove(index2);
                        groupChatAdapter.notifyItemRemoved(index);
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/
    }

}