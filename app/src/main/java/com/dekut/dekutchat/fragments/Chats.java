package com.dekut.dekutchat.fragments;

import android.content.Context;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dekut.dekutchat.R;
import com.dekut.dekutchat.adapters.ChatAdapter;
import com.dekut.dekutchat.adapters.SearchUserAdapter;
import com.dekut.dekutchat.utils.Conversation;
import com.dekut.dekutchat.utils.GetTime;
import com.dekut.dekutchat.utils.HomePost;
import com.dekut.dekutchat.utils.Message;
import com.dekut.dekutchat.utils.Student;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Chats#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Chats extends Fragment {

    SearchView userSearchView;
    RecyclerView chatRecyclerView, userRecyclerView;
    FloatingActionButton btnAddChat;
    PopupWindow popupWindow;
    ImageButton btnDismissPopup;
    ProgressBar mainProgressBar,searchUserProgressBar;
    Toolbar toolbar;
    TextView tvNoChats, tvStartChat;

    Context context;
    LinearLayoutManager linearLayoutManager;
    LinearLayoutManager linearLayoutManager2;
    ChatAdapter chatAdapter;
    String email;
    SearchUserAdapter searchUserAdapter;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    List<Conversation> conversations = new ArrayList<>();
    List<String> keys = new ArrayList<>();
    List<String> userKeys = new ArrayList<>();
    String key = null;
    String keyword = null;
    boolean isLoading = false;
    List<Student> students = new ArrayList<>();
    Query userQuery;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Chats() {
        // Required empty public constructor
    }

    public Chats(String email){
        this.email = email;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Chats.
     */
    // TODO: Rename and change types and number of parameters
    public static Chats newInstance(String param1, String param2) {
        Chats fragment = new Chats();
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
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        toolbar = getActivity().findViewById(R.id.toolbar);
        btnAddChat = view.findViewById(R.id.btnAddChat);
        mainProgressBar = view.findViewById(R.id.mainProgressBar);
        tvNoChats = view.findViewById(R.id.tvNoChats);
        tvStartChat = view.findViewById(R.id.tvStartChat);

        linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(linearLayoutManager);
        chatAdapter = new ChatAdapter(conversations, context, email);
        chatAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        chatRecyclerView.setAdapter(chatAdapter);

        fetchChats();

        btnAddChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.search_popup, null);
                int width = LinearLayout.LayoutParams.MATCH_PARENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                popupWindow = new PopupWindow(popupView, width, 1000, true);
                popupWindow.setElevation(10);
                popupWindow.showAsDropDown(toolbar);
                popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

                userSearchView = popupView.findViewById(R.id.userSearchView);
                userRecyclerView = popupView.findViewById(R.id.userRecyclerView);
                btnDismissPopup = popupView.findViewById(R.id.btnDismissPopup);
                searchUserProgressBar = popupView.findViewById(R.id.searchUserProgressBar);

                linearLayoutManager2 = new LinearLayoutManager(context);
                userRecyclerView.setLayoutManager(linearLayoutManager2);
                searchUserAdapter = new SearchUserAdapter(students, context, popupWindow);
                searchUserAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
                searchUserProgressBar.setVisibility(View.GONE);
                userRecyclerView.setAdapter(searchUserAdapter);

                isLoading = true;
                searchUsers();

                userRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(@NonNull @NotNull RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        LinearLayoutManager linearLayoutManager1 = (LinearLayoutManager) recyclerView.getLayoutManager();
                        int lastPosition = linearLayoutManager1.findLastCompletelyVisibleItemPosition();
                        int totalItems = linearLayoutManager1.getItemCount();

                        if ((totalItems -  lastPosition) < 3){
                            if (!isLoading) {
                                isLoading = true;
                                searchUsers();
                            }
                        }
                    }
                });

                userSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        keyword = query.toLowerCase();
                        key = null;
                        students.clear();
                        userKeys.clear();
                        searchUserAdapter.notifyDataSetChanged();
                        searchUsers();
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        keyword = newText.toLowerCase();
                        key = null;
                        students.clear();
                        userKeys.clear();
                        searchUserAdapter.notifyDataSetChanged();
                        searchUsers();
                        return true;
                    }
                });

                btnDismissPopup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });

                popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        userQuery.removeEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });
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

    public void fetchChats(){
        Query query = firebaseDatabase.getReference().child("conversations").orderByChild("lastMessageT");
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Conversation conversation = snapshot.getValue(Conversation.class);
                if(!keys.contains(conversation.getConvoId())) {
                    if (conversation.getConvoId().replace("_", ".").contains(email)) {
                        conversations.add(conversation);
                        keys.add(conversation.getConvoId());
                        chatAdapter.notifyItemInserted(conversations.size() - 1);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Conversation conversation = snapshot.getValue(Conversation.class);
                for(Conversation conversation1 : conversations){
                    if(conversation.getConvoId().equals(conversation1.getConvoId())){
                        if (conversation.getLastMessageT() > conversation1.getLastMessageT()) {
                            int index = conversations.indexOf(conversation1);
                            conversations.remove(index);
                            chatAdapter.notifyItemRemoved(index);
                            conversations.add(conversation);
                            chatAdapter.notifyItemInserted(conversations.size() - 1);
                            break;
                        }

                        else {
                            int index = conversations.indexOf(conversation1);
                            conversations.remove(index);
                            conversations.add(index, conversation);
                            chatAdapter.notifyItemChanged(index);
                            break;
                        }
                    }
                }


            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
               Conversation conversation = snapshot.getValue(Conversation.class);
                for(Conversation conversation1 : conversations){
                    if(conversation.getConvoId().equals(conversation1.getConvoId())){
                        int index = conversations.indexOf(conversation1);
                        conversations.remove(index);
                        chatAdapter.notifyItemRemoved(index);
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
        });

    }

    public void searchUsers() {
        if (key == null) {
            userQuery = firebaseDatabase.getReference().child("students").orderByKey().limitToFirst(100);
        }

        else {
            userQuery = firebaseDatabase.getReference().child("students").orderByKey().limitToFirst(100).startAt(key);
        }

        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        Student student = snap.getValue(Student.class);

                        if (student.getEmail().equals(email) || userKeys.contains(student.getId())) {
                            continue;
                        }

                        else {
                            if (keyword == null) {
                                students.add(student);
                                userKeys.add(student.getId());
                                searchUserAdapter.notifyItemInserted(students.size() - 1);

                            }

                            else {
                                String name = student.getUserName().toLowerCase();
                                String email = student.getEmail().toLowerCase();

                                if (name.contains(keyword) || email.contains(keyword)) {
                                    students.add(student);
                                    userKeys.add(student.getId());
                                    searchUserAdapter.notifyItemInserted(students.size() - 1);
                                }
                            }
                        }
                        key = student.getId();
                    }

                    if (students.isEmpty()){
                        searchUsers();
                    }
                    else {
                        isLoading = false;
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}