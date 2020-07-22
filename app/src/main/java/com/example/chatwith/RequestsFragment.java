package com.example.chatwith;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

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


public class RequestsFragment extends Fragment {

    private View RequestFragmentView;
    private RecyclerView myRequestList;

    private DatabaseReference ChatRequestsRef,UsersRef,ContactsRef,RemoveChatRequestsRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    public RequestsFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestFragmentView= inflater.inflate(R.layout.fragment_requests, container, false);


        myRequestList=(RecyclerView) RequestFragmentView.findViewById(R.id.chat_requests_list);
        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        UsersRef=FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestsRef=FirebaseDatabase.getInstance().getReference().child("Chat Requests").child(currentUserID);
        RemoveChatRequestsRef=FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");



        return RequestFragmentView;
    }


    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options=
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(ChatRequestsRef, Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, RequestViewHolder> adapter=new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder requestViewHolder, final int position, @NonNull Contacts contacts)
            {
                requestViewHolder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                requestViewHolder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);

                final String list_user_id=getRef(position).getKey();

                DatabaseReference getTypeRef=getRef(position).child("request_type").getRef();

               getTypeRef.addValueEventListener(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                   {
                       if(dataSnapshot.exists())
                       {
                           String type=dataSnapshot.getValue().toString();
                           if(type.equals("received"))
                           {
                               Log.d("reciveddata","receivde");
                               UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                   @Override
                                   public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                   {
                                       if(dataSnapshot.hasChild("image"))
                                       {

                                           final String requestProfileImage=dataSnapshot.child("image").getValue().toString();

                                           Picasso.get().load(requestProfileImage).into(requestViewHolder.profileImage);
                                       }

                                       final String requestUserName=dataSnapshot.child("name").getValue().toString();
                                       final String requestUserStatus=dataSnapshot.child("status").getValue().toString();

                                       Log.d("requestStatus",requestUserStatus);
                                       requestViewHolder.userName.setText(requestUserName);
                                       requestViewHolder.userStatus.setText("Want to connect with you.");


                                       //Accept and cancel button work --till the code worked success

                                       //click Accept Button
                                       requestViewHolder.AcceptButton.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v)
                                           {
                                               ContactsRef.child(currentUserID).child(list_user_id)
                                                       .child("contacts").setValue("Saved")
                                                       .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                           @Override
                                                           public void onComplete(@NonNull Task<Void> task)
                                                           {
                                                               if(task.isSuccessful())
                                                               {
                                                                   ContactsRef.child(list_user_id).child(currentUserID)
                                                                           .child("contacts").setValue("Saved")
                                                                           .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                               @Override
                                                                               public void onComplete(@NonNull Task<Void> task)
                                                                               {
                                                                                   if(task.isSuccessful())
                                                                                   {
                                                                                       Log.d("currentUserID",currentUserID);
                                                                                       Log.d("list_user_id",currentUserID);
                                                                                       RemoveChatRequestsRef.child(currentUserID).child(list_user_id)
                                                                                               .removeValue()
                                                                                               .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                   @Override
                                                                                                   public void onComplete(@NonNull Task<Void> task)
                                                                                                   {
                                                                                                       if(task.isSuccessful())
                                                                                                       {
                                                                                                           RemoveChatRequestsRef.child(list_user_id).child(currentUserID)
                                                                                                                   .removeValue()
                                                                                                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                       @Override
                                                                                                                       public void onComplete(@NonNull Task<Void> task)
                                                                                                                       {
                                                                                                                           if(task.isSuccessful())
                                                                                                                           {
                                                                                                                               Toast.makeText(getContext(), "Contact Added", Toast.LENGTH_SHORT).show();
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
                                       });


                                       //Click Cancel button
                                       requestViewHolder.CancelButton.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v)
                                           {
                                               Log.d("currentUserID",currentUserID);
                                               Log.d("list_user_id",currentUserID);
                                               RemoveChatRequestsRef.child(currentUserID).child(list_user_id)
                                                       .removeValue()
                                                       .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                           @Override
                                                           public void onComplete(@NonNull Task<Void> task)
                                                           {
                                                               if(task.isSuccessful())
                                                               {
                                                                   RemoveChatRequestsRef.child(list_user_id).child(currentUserID)
                                                                           .removeValue()
                                                                           .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                               @Override
                                                                               public void onComplete(@NonNull Task<Void> task)
                                                                               {
                                                                                   if(task.isSuccessful())
                                                                                   {
                                                                                       Toast.makeText(getContext(), "Contact Removed", Toast.LENGTH_SHORT).show();
                                                                                   }
                                                                               }
                                                                           });
                                                               }
                                                           }
                                                       });
                                           }
                                       });



                      /*                 requestViewHolder.AcceptButton.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v)
                                           {
                                               ContactsRef.child(currentUserID).child(list_user_id).child("Contact")
                                                       .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                   @Override
                                                   public void onComplete(@NonNull Task<Void> task)
                                                   {
                                                       if(task.isSuccessful())
                                                       {
                                                           //after add contact list user delete from chat request
                                                           ChatRequestsRef.child(currentUserID).child(list_user_id)
                                                                   .removeValue()
                                                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                       @Override
                                                                       public void onComplete(@NonNull Task<Void> task)
                                                                       {
                                                                            if(task.isSuccessful())
                                                                            {
                                                                                Toast.makeText(getContext(), "New Contact Added", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                       }
                                                                   });
                                                       }
                                                   }
                                               });
                                           }
                                       });*/
                                   }

                                   @Override
                                   public void onCancelled(@NonNull DatabaseError databaseError) {

                                   }
                               });

                           }

                           //hide recyclerView  manual tusk please fixed later--*********
                           //if type sent then also show RecyclerView to fixed this use this things please fixed later...
                           else{
                           myRequestList.setVisibility(View.INVISIBLE);
                       }
                       }
                   }

                   @Override
                   public void onCancelled(@NonNull DatabaseError databaseError)
                   {

                   }
               });



            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType)
            {
                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                RequestViewHolder viewHolder =new RequestViewHolder(view);
                return  viewHolder;
            }
        };
        myRequestList.setAdapter(adapter);

        adapter.startListening();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName,userStatus;
        CircleImageView profileImage;
        Button AcceptButton,CancelButton;
        public RequestViewHolder(@NonNull View itemView)
        {
            super(itemView);
            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);

            AcceptButton=itemView.findViewById(R.id.request_accept_btn);
            CancelButton=itemView.findViewById(R.id.request_cancel_btn);
        }
    }















  /*  @Override
    public void onStart() {
        Log.d("ReceivedActype","Receivedtype");
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options=
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(ChatRequestsRef,Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts,RequestViewHolder> adapter= new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder requestViewHolder, int position, @NonNull Contacts contacts)
            {
                requestViewHolder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                requestViewHolder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);

                final String list_user_id=getRef(position).getKey();

                DatabaseReference getTypeRef=getRef(position).child("request_type").getRef();

                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {

                            String type=dataSnapshot.getValue().toString();

                            if(type.equals("received"))
                            {
                                Log.d("ReceivedAc","Received");
                                Log.wtf("ReceivedActf","ReceivedActf");
                                UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                    {
                                        if(dataSnapshot.hasChild("image"))
                                        {

                                            final String requestProfileImage=dataSnapshot.child("image").getValue().toString();

                                            Picasso.get().load(requestProfileImage).into(requestViewHolder.profileImage);
                                        }

                                        final String requestUserName=dataSnapshot.child("name").getValue().toString();
                                        final String requestUserStatus=dataSnapshot.child("status").getValue().toString();

                                        Log.d("requestStatus",requestUserStatus);
                                        requestViewHolder.userName.setText(requestUserName);
                                        requestViewHolder.userStatus.setText("Want to connect with you.");


                                        //Accept and cancel button work
                                        requestViewHolder.itemView.setOnClickListener(new View.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(View view)
                                            {
                                                CharSequence options[]=new CharSequence[]
                                                        {
                                                                "Accept",
                                                                "Cancel"
                                                        };

                                                AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                                builder.setTitle(requestUserName + " Chat Request");
                                               builder.setItems(options, new DialogInterface.OnClickListener() {
                                                   @Override
                                                   public void onClick(DialogInterface dialog, int i)
                                                   {
                                                       if(i==0)//means Click Accept button
                                                       {
                                                           Log.d("AcceptButton","AcceptButtonWorks");
                                                           //add contact both side
                                                           ContactsRef.child(currentUserID).child(list_user_id).child("Contact")
                                                                    .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>()
                                                            {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task)
                                                                {
                                                                    if(task.isSuccessful())
                                                                    {
                                                                        ContactsRef.child(list_user_id).child(currentUserID).child("Contact")
                                                                                .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>()
                                                                        {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                            {
                                                                                if(task.isSuccessful())
                                                                                {
                                                                                    //after add contact delete from chat Request..
                                                                                    ChatRequestsRef.child(currentUserID).child(list_user_id)
                                                                                            .removeValue()
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                            {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                                {
                                                                                                    if(task.isSuccessful())
                                                                                                    {
                                                                                                        ChatRequestsRef.child(list_user_id).child(currentUserID)
                                                                                                                .removeValue()
                                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                                                {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                                                    {
                                                                                                                        if(task.isSuccessful())
                                                                                                                        {
                                                                                                                            Toast.makeText(getContext(), "New Contact Added", Toast.LENGTH_SHORT).show();
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
                                                      if(i==1)//means Click Cancel button
                                                      {
                                                          Log.d("CancelButton","CancelWork");
                                                          ChatRequestsRef.child(currentUserID).child(list_user_id)
                                                                  .removeValue()
                                                                  .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                      @Override
                                                                      public void onComplete(@NonNull Task<Void> task)
                                                                      {
                                                                          if(task.isSuccessful())
                                                                          {
                                                                              ChatRequestsRef.child(list_user_id).child(currentUserID)
                                                                                      .removeValue()
                                                                                      .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                      {
                                                                                          @Override
                                                                                          public void onComplete(@NonNull Task<Void> task)
                                                                                          {
                                                                                              if(task.isSuccessful())
                                                                                              {
                                                                                                  Toast.makeText(getContext(), "Contact Removed", Toast.LENGTH_SHORT).show();
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
                            //hide recyclerView  manual tusk please fixed later--*********
                            //if type sent then also show RecyclerView to fixed this use this things please fixed later...
                          *//*  else{
                                myRequestList.setVisibility(View.INVISIBLE);
                            }*//*
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType)
            {
                View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                RequestViewHolder holder=new RequestViewHolder(view);
                return  holder;
            }
        };

        myRequestList.setAdapter(adapter);
        adapter.startListening();
    }
    public static class RequestViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName,userStatus;
        CircleImageView profileImage;
        Button AcceptButton,CancelButton;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);

            AcceptButton=itemView.findViewById(R.id.request_accept_btn);
            CancelButton=itemView.findViewById(R.id.request_cancel_btn);
        }
    }*/
}