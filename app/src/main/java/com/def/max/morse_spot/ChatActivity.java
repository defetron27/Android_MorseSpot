package com.def.max.morse_spot;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.def.max.morse_spot.Adapters.MessageRecyclerAdapter;
import com.def.max.morse_spot.Models.MessageModel;
import com.def.max.morse_spot.Utils.CodeConverter;
import com.def.max.morse_spot.Utils.NetworkStatus;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.angmarch.views.NiceSpinner;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity
{
    private static final String TAG = ChatActivity.class.getSimpleName();

    private DatabaseReference chattingRef;

    private String onlineUserId,receiverUserId;

    private ArrayList<MessageModel> friendsMessages = new ArrayList<>();

    private MessageRecyclerAdapter adapter;

    private AppCompatEditText messageEditText;
    private CardView senderEnterMessageCardView;
    private AppCompatTextView senderEnterMessage;

    private NiceSpinner optionSpinner;

    private ArrayList<String> categories;

    private String convertedText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();

        if (intent.getExtras() != null)
        {
            receiverUserId = intent.getExtras().getString("receiver_id");
        }

        Toolbar chatToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);

        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser user = firebaseAuth.getCurrentUser();

        chattingRef = FirebaseDatabase.getInstance().getReference();
        chattingRef.keepSynced(true);

        if (user != null)
        {
            onlineUserId = user.getUid();
        }

        final CircleImageView chatUserCircleImageView = findViewById(R.id.chat_user_circle_image_view);
        final AppCompatTextView chatUserNameTextView = findViewById(R.id.chat_user_name_text_view);
        messageEditText = findViewById(R.id.chatting_friend_edit_text);
        AppCompatImageView friendChatSendImageView = findViewById(R.id.friend_chat_send_image_view);
        optionSpinner = findViewById(R.id.option_spinner);
        senderEnterMessageCardView = findViewById(R.id.sender_enter_message_card_view);
        senderEnterMessage = findViewById(R.id.sender_enter_message);

        categories = new ArrayList<>();

        categories.add("Binary");
        categories.add("Morse");

        optionSpinner.attachDataSource(categories);

        optionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                messageEditText.setText("");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                messageEditText.setText("");
            }
        });

        chattingRef.child("Users").child(receiverUserId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                Object objectName = dataSnapshot.child("user_name").getValue();
                Object objectThumb = dataSnapshot.child("user_profile_thumb_img").getValue();

                if (objectName != null)
                {
                    String userName = objectName.toString();

                    chatUserNameTextView.setText(userName);
                }

                if (objectThumb != null)
                {
                    final String userThumb = objectThumb.toString();

                    Picasso.with(ChatActivity.this).load(userThumb).networkPolicy(NetworkPolicy.OFFLINE).into(chatUserCircleImageView, new Callback()
                    {
                        @Override
                        public void onSuccess()
                        {

                        }

                        @Override
                        public void onError()
                        {
                            Picasso.with(ChatActivity.this).load(userThumb).placeholder(R.drawable.user_icon).into(chatUserCircleImageView);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        final RecyclerView messageRecyclerView = findViewById(R.id.friends_chatting_recycler_view);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this, LinearLayoutManager.VERTICAL,false);
        messageRecyclerView.setLayoutManager(linearLayoutManager);
        adapter = new MessageRecyclerAdapter(ChatActivity.this,friendsMessages,onlineUserId);
        messageRecyclerView.setAdapter(adapter);

        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(ChatActivity.this,R.anim.layout_slide_bottom);

        messageRecyclerView.setLayoutAnimation(controller);
        messageRecyclerView.scheduleLayoutAnimation();

        chattingRef.child("Messages").child(onlineUserId).child(receiverUserId).addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                MessageModel model = dataSnapshot.getValue(MessageModel.class);
                friendsMessages.add(model);

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
            {
                MessageModel model = dataSnapshot.getValue(MessageModel.class);
                friendsMessages.remove(model);

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Crashlytics.log(databaseError.getMessage());
            }
        });

        messageEditText.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (hasFocus)
                {
                    linearLayoutManager.setStackFromEnd(true);
                }
            }
        });

        messageEditText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (count > 0)
                {
                    String word = s.toString().toLowerCase();

                    String convertTo = categories.get(optionSpinner.getSelectedIndex());

                    if (convertTo.equals("Binary"))
                    {
                        convertedText = CodeConverter.alphaToBinary(word);

                        senderEnterMessageCardView.setVisibility(View.VISIBLE);

                        senderEnterMessage.setText(convertedText);
                    }
                    else if (convertTo.equals("Morse"))
                    {
                        convertedText = CodeConverter.alphaToMorse(word);

                        senderEnterMessageCardView.setVisibility(View.VISIBLE);

                        senderEnterMessage.setText(convertedText);
                    }
                }
                else
                {
                    senderEnterMessageCardView.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });

        friendChatSendImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (NetworkStatus.isConnected(ChatActivity.this) && NetworkStatus.isConnectedFast(ChatActivity.this))
                {
                    String message = messageEditText.getText().toString();
                    String convertedText = senderEnterMessage.getText().toString();

                    if (TextUtils.isEmpty(message) && !message.equals(" "))
                    {
                        Toast.makeText(ChatActivity.this, "Please enter any text", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        messageEditText.setText("");
                        senderEnterMessage.setText("");

                        if (checkDate(ChatActivity.this, onlineUserId, receiverUserId))
                        {
                            storeTextMessageDetails(ChatActivity.this, onlineUserId, receiverUserId,convertedText);
                        }
                        else
                        {
                            storeTextMessageDetails(ChatActivity.this, onlineUserId, receiverUserId, convertedText);
                        }
                    }
                }
                else
                {
                    Toast.makeText(ChatActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

        final AdView mAdView = findViewById(R.id.chat_adView);
        final AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                AdRequest adRequest2 = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest2);

                handler.postDelayed(this, 560 * 1000);
            }
        }, 1000);
    }

    private String getTodayDate()
    {
        DateFormat todayDate = new SimpleDateFormat("d MMM yyyy", Locale.US);

        return todayDate.format(Calendar.getInstance().getTime());
    }

    private String formatToYesterdayOrToday(String date) throws ParseException
    {
        Date dateTime = new SimpleDateFormat("d MMM yyyy", Locale.US).parse(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateTime);
        Calendar today = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -1);

        if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR))
        {
            return "Today";
        }
        else if (calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR))
        {
            return "Yesterday";
        }
        else
        {
            return date;
        }
    }

    private void storeDateRef(Context context, String online_key, String friend_key)
    {
        SharedPreferences preferences = context.getSharedPreferences(online_key,MODE_PRIVATE);

        if (preferences != null)
        {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(friend_key,getTodayDate());
            editor.apply();
        }
    }

    private String readDateRef(Context context, String online_key, String friend_key)
    {
        String todayDate,checkDate = null;

        SharedPreferences date = context.getSharedPreferences(online_key,MODE_PRIVATE);

        if (date != null)
        {
            checkDate = date.getString(friend_key,null);
        }

        if (date == null)
        {
            todayDate = null;
        }
        else
        {
            todayDate = checkDate;
        }

        return todayDate;
    }

    private boolean checkDate(Context context, String online, String friend)
    {
        String today = readDateRef(context,online,friend);

        boolean result = false;

        if (today == null)
        {
            updateDateRef(context,online,friend);

            result = true;
        }
        else
        {
            try
            {
                String checkDate = formatToYesterdayOrToday(today);

                if (!checkDate.equals("Today"))
                {
                    updateDateRef(context,online,friend);

                    result = true;
                }
                else
                {
                    result = false;
                }
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        }

        return result;
    }

    private void updateDateRef(final Context context, final String online_key, final String friend_key)
    {
        storeDateRef(context,online_key,friend_key);

        DatabaseReference dateRef = chattingRef.child("Messages").child(onlineUserId).child(friend_key).push();

        final String date_push_id = dateRef.getKey();

        final Map<String,Object> messageDate = new HashMap<>();

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",Locale.US);
        Date date = new Date();

        String todayDate = formatter.format(date);

        messageDate.put("type", "date");
        messageDate.put("today_date", getTodayDate());
        messageDate.put("from",online_key);
        messageDate.put("key",date_push_id);
        messageDate.put("date", todayDate);

        if (date_push_id != null)
        {
            chattingRef.child("Messages").child(onlineUserId).child(friend_key).child(date_push_id).updateChildren(messageDate).addOnCompleteListener(new OnCompleteListener<Void>()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if (task.isSuccessful())
                    {
                        chattingRef.child("Messages").child(friend_key).child(onlineUserId).child(date_push_id).updateChildren(messageDate).addOnFailureListener(new OnFailureListener()
                        {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
                                Toast.makeText(context, "Error while storing documents", Toast.LENGTH_SHORT).show();
                                Log.e(TAG,e.toString());
                                Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    Toast.makeText(context, "Error while storing documents", Toast.LENGTH_SHORT).show();
                    Log.e(TAG,e.toString());
                    Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                }
            });
        }
    }

    private void storeTextMessageDetails(final Context context, final String onlineUserId, final String friend_key, String message)
    {
        DatabaseReference messageRef = chattingRef.child("Messages").child(onlineUserId).child(friend_key).push();

        final String messageKey = messageRef.getKey();

        DateFormat df = new SimpleDateFormat("h:mm a",Locale.US);
        final String time = df.format(Calendar.getInstance().getTime());

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US);
        Date date = new Date();

        String todayDate = formatter.format(date);

        final Map<String,Object> chatMessage = new HashMap<>();

        chatMessage.put("message",message);
        chatMessage.put("from",onlineUserId);
        chatMessage.put("type","text");
        chatMessage.put("key",messageKey);
        chatMessage.put("time",time);
        chatMessage.put("date",todayDate);

        if (messageKey != null)
        {
            chattingRef.child("Messages").child(onlineUserId).child(friend_key).child(messageKey).updateChildren(chatMessage).addOnCompleteListener(new OnCompleteListener<Void>()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if (task.isSuccessful())
                    {
                        chattingRef.child("Messages").child(friend_key).child(onlineUserId).child(messageKey).updateChildren(chatMessage).addOnFailureListener(new OnFailureListener()
                        {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
                                Toast.makeText(context, "Error while storing documents", Toast.LENGTH_SHORT).show();
                                Log.e(TAG,e.toString());
                                Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    Toast.makeText(context, "Error while storing documents", Toast.LENGTH_SHORT).show();
                    Log.e(TAG,e.toString());
                    Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                }
            });
        }
    }
}
