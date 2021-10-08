package com.def.max.morse_spot.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.def.max.morse_spot.ChatActivity;
import com.def.max.morse_spot.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChattingUsersRecyclerAdapter extends RecyclerView.Adapter<ChattingUsersRecyclerAdapter.ChattingUsersViewHolder>
{

    private ArrayList<String> userIds;
    private Activity activity;

    public ChattingUsersRecyclerAdapter(Set<String> userIds, Activity activity)
    {
        this.userIds = new ArrayList<>(userIds);
        this.activity = activity;
    }

    @NonNull
    @Override
    public ChattingUsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(activity).inflate(R.layout.chat_items,parent,false);
        return new ChattingUsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChattingUsersViewHolder holder, int position)
    {
        final String userKey = userIds.get(position);

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.keepSynced(true);

        usersRef.child(userKey).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                Object objectThumb = dataSnapshot.child("user_profile_thumb_img").getValue();
                Object objectName = dataSnapshot.child("user_name").getValue();

                if (objectName != null && objectThumb != null)
                {
                    String thumb = objectThumb.toString();
                    String userName = objectName.toString();

                    holder.userName.setText(userName);
                    holder.setUserCircleImage(thumb);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Crashlytics.log(databaseError.getMessage());
            }
        });

        holder.view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent chatIntent = new Intent(activity, ChatActivity.class);
                chatIntent.putExtra("receiver_id",userKey);
                activity.startActivity(chatIntent);
                activity.overridePendingTransition(R.anim.slide_in_up,R.anim.slide_out_up);
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return userIds.size();
    }

    class ChattingUsersViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName;

        View view;

        ChattingUsersViewHolder(View itemView)
        {
            super(itemView);

            view = itemView;

            userName = view.findViewById(R.id.chatting_user_name);
        }

        private void setUserCircleImage(final String thumbImg)
        {
            final CircleImageView thumb = view.findViewById(R.id.chatting_user_circle_image_view);

            Picasso.with(activity).load(thumbImg).networkPolicy(NetworkPolicy.OFFLINE).into(thumb, new Callback()
            {
                @Override
                public void onSuccess()
                {

                }

                @Override
                public void onError()
                {
                    Picasso.with(activity).load(thumbImg).placeholder(R.drawable.user_icon).into(thumb);
                }
            });
        }
    }
}
