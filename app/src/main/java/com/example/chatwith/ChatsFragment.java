package com.example.chatwith;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
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

public class ChatsFragment extends Fragment {

    private View PrivateChatsView;
    private RecyclerView chatList;

    private DatabaseReference ChatRef,UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PrivateChatsView= inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        ChatRef=FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        UsersRef=FirebaseDatabase.getInstance().getReference().child("Users");


        chatList=(RecyclerView) PrivateChatsView.findViewById(R.id.chats_list);
        chatList.setLayoutManager(new LinearLayoutManager(getContext()));

        return  PrivateChatsView;
    }


    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options=
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(ChatRef, Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter=new FirebaseRecyclerAdapter<Contacts,ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, final int position, @NonNull Contacts model)
            {
                final String userIDs=getRef(position).getKey();
                final String[] retImage = {"default_image"};
                UsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                       if(dataSnapshot.exists())
                       {
                           if(dataSnapshot.hasChild("image"))
                           {
                               retImage[0] =dataSnapshot.child("image").getValue().toString();
                               Picasso.get().load(retImage[0]).into(holder.profileImage);
                           }
                           final String retName=dataSnapshot.child("name").getValue().toString();
                           final String retStatus=dataSnapshot.child("status").getValue().toString();

                           holder.userName.setText(retName);
                           holder.userStatus.setText("Last Seen:"+"\n"+"Date"+"Time");

                           holder.itemView.setOnClickListener(new View.OnClickListener() {
                               @Override
                               public void onClick(View v)
                               {
                                    Intent chatIntent=new Intent(getContext(),ChatActivity.class);
                                    chatIntent.putExtra("visit_user_id",userIDs);
                                    chatIntent.putExtra("visit_user_name",retName);
                                    chatIntent.putExtra("visit_user_image", retImage[0]);
                                    startActivity(chatIntent);
                               }
                           });
                       }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType)
            {
                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                ChatsViewHolder viewHolder =new ChatsViewHolder(view);
                return  viewHolder;
            }
        };
        chatList.setAdapter(adapter);

        adapter.startListening();
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName,userStatus;
        CircleImageView profileImage;
        public ChatsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);
        }
    }














  /*  @Override
    public void onStart() {
        super.onStart();
        Log.d("messagess","mmmmd");

        FirebaseRecyclerOptions<Contacts> options=
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatRef,Contacts.class)
                .build();


        FirebaseRecyclerAdapter<Contacts,ChatsViewHolder> adapter=
                new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts contacts)
                    {
                        final String userIDs=getRef(position).getKey();

                        UsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if(dataSnapshot.hasChild("image"))
                                {
                                    final String retImage=dataSnapshot.child("image").getValue().toString();
                                    Picasso.get().load(retImage).into(holder.profileImage);
                                }
                                final String retName=dataSnapshot.child("name").getValue().toString();
                                final String retStatus=dataSnapshot.child("status").getValue().toString();

                                holder.userName.setText(retName);
                                holder.userStatus.setText("Last Seen:"+"\n"+"Date"+"Time");
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType)
                    {
                        View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                        return  new ChatsViewHolder(view);

                    }
                };
        chatList.setAdapter(adapter);
        adapter.startListening();
    }*/

 /*   public static class ChatsViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView profileImage;
        TextView userStatus,userName;

        public ChatsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            profileImage=itemView.findViewById(R.id.users_profile_image);
            userStatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.user_profile_name);
        }


    }*/


}