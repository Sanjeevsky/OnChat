package com.example.sanjeev.onchat;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.zip.CheckedOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {
    private View PrivateChatView;
    private RecyclerView chatLists;
    private DatabaseReference ChatsRef,UsersRef;
    private FirebaseAuth mAuth;
    private String currrentUserID;




    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PrivateChatView= inflater.inflate(R.layout.fragment_chats, container, false);
        mAuth=FirebaseAuth.getInstance();
        currrentUserID=mAuth.getCurrentUser().getUid();
        UsersRef=FirebaseDatabase.getInstance().getReference().child("Users");
        ChatsRef=FirebaseDatabase.getInstance().getReference().child("Contacts").child(currrentUserID);
        chatLists=(RecyclerView)PrivateChatView.findViewById(R.id.chats_list);
        chatLists.setLayoutManager(new LinearLayoutManager(getContext()));
        return PrivateChatView;
    }


    @Override
    public void onStart() {
        super.onStart();

       FirebaseRecyclerOptions<Contacts> options=
               new FirebaseRecyclerOptions.Builder<Contacts>()
               .setQuery(ChatsRef,Contacts.class)
               .build();

        FirebaseRecyclerAdapter<Contacts,ChatViewHolder> adapter=new FirebaseRecyclerAdapter<Contacts, ChatViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatViewHolder holder, int position, @NonNull Contacts model) {
                final String userIDs=getRef(position).getKey();
                final String[] retImage = {"default_image"};
                UsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild("image"))
                        {
                            retImage[0] =dataSnapshot.child("image").getValue().toString();
                            Picasso.get().load(retImage[0]).into(holder.profileImage);
                        }
                        final String retName=dataSnapshot.child("name").getValue().toString();
                        String retStatus=dataSnapshot.child("status").getValue().toString();
                        holder.userName.setText(retName);
                        holder.userStatus.setText("Last Seen: "+"\n"+"Date"+" Time");
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent chatIntent=new Intent(getContext(),ChatActivity.class);
                                chatIntent.putExtra("visit_user_id",userIDs);
                                chatIntent.putExtra("visit_user_name",retName);
                                chatIntent.putExtra("visit_user_image", retImage[0]);
                                        startActivity(chatIntent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                ChatViewHolder holder=new ChatViewHolder(view);
                return holder;

            }
        };
        chatLists.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView userName,userStatus;
        CircleImageView profileImage;

        public ChatViewHolder(View itemView) {
            super(itemView);
            userName=(TextView)itemView.findViewById(R.id.users_profile_name);
            userStatus=(TextView)itemView.findViewById(R.id.user_status);
            profileImage=(CircleImageView)itemView.findViewById(R.id.users_profile_image);
        }
    }
}
