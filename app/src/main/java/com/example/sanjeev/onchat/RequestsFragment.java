package com.example.sanjeev.onchat;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {
    private View RequestsFragmentView;
    private RecyclerView myRequestList;
    private DatabaseReference ChatRequestRef, UserRef,ContactsRef;
    private FirebaseAuth mAuth;
    private String currentUserID;


    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestsFragmentView = inflater.inflate(R.layout.fragment_requests, container, false);
        UserRef=FirebaseDatabase.getInstance().getReference().child("Users");
        myRequestList = (RecyclerView) RequestsFragmentView.findViewById(R.id.chat_requests_list);
        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        return RequestsFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatRequestRef.child(currentUserID), Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,RequestsViewHolder> adapter=new FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull Contacts model) {
                holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);

                final String list_user_id=getRef(position).getKey();

                DatabaseReference getTypeRef=getRef(position).child("request_type").getRef();

                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String type = dataSnapshot.getValue().toString();
                            if (type.equals("received"))
                            {


                                UserRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.hasChild("image"))
                                        {

                                           final   String requestProfileImage = dataSnapshot.child("image").getValue().toString();
                                            Picasso.get().load(requestProfileImage).placeholder(R.drawable.default_image).into(holder.profileImage);

                                        }
                                             final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                             final String requestUserStatus = dataSnapshot.child("status").getValue().toString();
                                              holder.userName.setText(requestUserName);
                                              holder.userStatus.setText("Wants To Be Friend");


                                             holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence options[]=new CharSequence[]
                                                        {
                                                                "Accept",
                                                                "Cancel"
                                                        };
                                                AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                                builder.setTitle(requestUserName+" Chat Request");
                                                 builder.setItems(options, new DialogInterface.OnClickListener() {
                                                     @Override
                                                     public void onClick(DialogInterface dialog, int which) {
                                                         if(which==0)
                                                         {
                                                            ContactsRef.child(currentUserID).child(list_user_id).child("Contact").setValue("Saved")
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful())
                                                                            {
                                                                                ContactsRef.child(list_user_id).child(currentUserID).child("Contact").setValue("Saved")
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if(task.isSuccessful())
                                                                                                {
                                                                                                    ChatRequestRef.child(currentUserID).child(list_user_id).removeValue()
                                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                @Override
                                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                                  if(task.isSuccessful())
                                                                                                                  {
                                                                                                                      ChatRequestRef.child(list_user_id).child(currentUserID).removeValue()
                                                                                                                              .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                  @Override
                                                                                                                                  public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                      if(task.isSuccessful())
                                                                                                                                      {
                                                                                                                                        Toast.makeText(getContext(),"New Contact Saved",Toast.LENGTH_LONG).show();
                                                                                                                                      }
                                                                                                                                  }
                                                                                                                              });
                                                                                                                  }
                                                                                                                }
                                                                                                            });

                                                                                                }
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    });
                                                         }
                                                         if(which==1)
                                                         {
                                                             ChatRequestRef.child(currentUserID).child(list_user_id).removeValue()
                                                                     .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                         @Override
                                                                         public void onComplete(@NonNull Task<Void> task) {
                                                                             if(task.isSuccessful())
                                                                             {
                                                                                 ChatRequestRef.child(list_user_id).child(currentUserID).removeValue()
                                                                                         .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                             @Override
                                                                                             public void onComplete(@NonNull Task<Void> task) {
                                                                                                 if(task.isSuccessful())
                                                                                                 {
                                                                                                     Toast.makeText(getContext(),"Contact Deleted",Toast.LENGTH_LONG).show();
                                                                                                 }
                                                                                             }
                                                                                         });
                                                                             }
                                                                         }
                                                                     });

                                                         }

                                                     }
                                                 });
                                                 builder.show();
                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
               View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
               RequestsViewHolder holder=new RequestsViewHolder(view);
               return holder;
            }
        };
        myRequestList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder {

        TextView userName,userStatus;
        CircleImageView profileImage;
        Button AcceptButton, CancelButton;

        public RequestsViewHolder(View itemView) {
            super(itemView);

            userName=(TextView)itemView.findViewById(R.id.users_profile_name);
            userStatus=(TextView)itemView.findViewById(R.id.user_status);
            profileImage=(CircleImageView)itemView.findViewById(R.id.users_profile_image);
            AcceptButton=(Button)itemView.findViewById(R.id.request_accept_btn);
            CancelButton=(Button)itemView.findViewById(R.id.request_cancel_btn);
        }
    }

}