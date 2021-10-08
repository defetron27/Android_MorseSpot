package com.def.max.morse_spot.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.def.max.morse_spot.Models.MessageModel;
import com.def.max.morse_spot.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MessageRecyclerAdapter extends RecyclerView.Adapter<MessageRecyclerAdapter.MessageViewHolder>
{
    private Context context;
    private ArrayList<MessageModel> friendsMessages;
    private String onlineUserId;

    public MessageRecyclerAdapter(Context context, ArrayList<MessageModel> friendsMessages, String onlineUserId)
    {
        this.context = context;
        this.friendsMessages = friendsMessages;
        this.onlineUserId = onlineUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.message_layout_items,parent,false);

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position)
    {
        final MessageModel model = friendsMessages.get(position);

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        userRef.keepSynced(true);

        switch (model.getType())
        {
            case "text":

                if (model.getFrom().equals(onlineUserId))
                {
                    holder.receiverTextCardView.setVisibility(GONE);

                    holder.senderTextCardView.setVisibility(VISIBLE);
                    holder.senderMessageText.setText(model.getMessage());
                    holder.senderMessageTextTime.setText(model.getTime());

                    holder.senderTextCardView.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_TEXT,model.getMessage());
                            context.startActivity(Intent.createChooser(shareIntent,"Share Encrypted With "));
                        }
                    });
                }
                else
                {
                    holder.senderTextCardView.setVisibility(GONE);

                    holder.receiverTextCardView.setVisibility(VISIBLE);
                    holder.receiverMessageText.setText(model.getMessage());
                    holder.receiverMessageTextTime.setText(model.getTime());

                    holder.receiverTextCardView.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_TEXT,model.getMessage());
                            context.startActivity(Intent.createChooser(shareIntent,"Share Encrypted With "));
                        }
                    });
                }

                break;
        }
    }

    @Override
    public int getItemCount() {
        return friendsMessages.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder
    {
        private AppCompatTextView receiverMessageText,receiverMessageTextTime,senderMessageText,senderMessageTextTime;
        private CardView senderTextCardView,receiverTextCardView;

        private View view;

        MessageViewHolder(View itemView)
        {
            super(itemView);

            view = itemView;

            senderMessageText = view.findViewById(R.id.sender_message_text);
            senderMessageTextTime = view.findViewById(R.id.sender_message_text_time);
            receiverMessageText = view.findViewById(R.id.receiver_message_text);
            receiverMessageTextTime = view.findViewById(R.id.receiver_message_text_time);
            senderTextCardView = view.findViewById(R.id.sender_text_card_view);
            receiverTextCardView = view.findViewById(R.id.receiver_text_card_view);
        }
    }

    private String getTodayDate()
    {
        DateFormat todayDate = new SimpleDateFormat("d MMM yyyy", Locale.US);

        return todayDate.format(Calendar.getInstance().getTime());
    }
}
