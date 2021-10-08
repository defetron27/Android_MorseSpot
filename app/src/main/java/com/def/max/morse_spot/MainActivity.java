package com.def.max.morse_spot;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.def.max.morse_spot.Adapters.ChattingUsersRecyclerAdapter;
import com.def.max.morse_spot.Utils.PermissionUtil;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import me.rishabhkhanna.recyclerswipedrag.OnSwipeListener;
import me.rishabhkhanna.recyclerswipedrag.RecyclerHelper;

public class MainActivity extends AppCompatActivity
{
    private String onlineUserId;

    private FirebaseAuth firebaseAuth;

    private ChattingUsersRecyclerAdapter adapter;

    private Set<String> finalizedNumbers = new HashSet<>();

    private RewardedVideoAd mRewardedVideoAd;

    private PermissionUtil permissionUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionUtil = new PermissionUtil(this);

        if (checkPermission(PermissionUtil.READ_ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,android.Manifest.permission.RECORD_AUDIO))
            {
                showPermissionExplanation(PermissionUtil.READ_ACCESS_NETWORK_STATE);
            }
            else if (permissionUtil.checkPermissionPreference(PermissionUtil.PERMISSION_ACCESS_NETWORK_STATE))
            {
                requestPermission(PermissionUtil.READ_ACCESS_NETWORK_STATE);
                permissionUtil.updatePermissionPreference(PermissionUtil.PERMISSION_ACCESS_NETWORK_STATE);
            }
            else
            {
                Toast.makeText(MainActivity.this, "Please allow record audio permission in your app settings", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package",MainActivity.this.getPackageName(),null);
                intent.setData(uri);
                this.startActivity(intent);
            }
        }
        if (checkPermission(PermissionUtil.READ_INTERNET) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,android.Manifest.permission.INTERNET))
            {
                showPermissionExplanation(PermissionUtil.READ_INTERNET);
            }
            else if (permissionUtil.checkPermissionPreference(PermissionUtil.PERMISSION_INTERNET))
            {
                requestPermission(PermissionUtil.READ_INTERNET);
                permissionUtil.updatePermissionPreference(PermissionUtil.PERMISSION_INTERNET);
            }
            else
            {
                Toast.makeText(MainActivity.this, "Please allow internet permission in your app settings", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package",MainActivity.this.getPackageName(),null);
                intent.setData(uri);
                this.startActivity(intent);
            }
        }
        if (checkPermission(PermissionUtil.READ_READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,android.Manifest.permission.READ_CONTACTS))
            {
                showPermissionExplanation(PermissionUtil.READ_READ_CONTACTS);
            }
            else if (permissionUtil.checkPermissionPreference(PermissionUtil.PERMISSION_READ_CONTACTS))
            {
                requestPermission(PermissionUtil.READ_READ_CONTACTS);
                permissionUtil.updatePermissionPreference(PermissionUtil.PERMISSION_READ_CONTACTS);
            }
            else
            {
                Toast.makeText(MainActivity.this, "Please allow write contacts permission in your app settings", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package",MainActivity.this.getPackageName(),null);
                intent.setData(uri);
                this.startActivity(intent);
            }
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.keepSynced(true);

        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null)
        {
            onlineUserId = firebaseUser.getUid();
        }

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        final CircleImageView currentImageCircleImageView = findViewById(R.id.current_image_circle_image_view);

        if (onlineUserId != null)
        {
            usersRef.child(onlineUserId).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if (dataSnapshot.hasChild("user_profile_thumb_img"))
                    {
                        Object objectThumb = dataSnapshot.child("user_profile_thumb_img").getValue();

                        if (objectThumb != null)
                        {
                            final String image = objectThumb.toString();

                            if (!image.equals("default_profile_thumb_img"))
                            {
                                Picasso.with(MainActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(currentImageCircleImageView, new Callback()
                                {
                                    @Override
                                    public void onSuccess()
                                    {

                                    }

                                    @Override
                                    public void onError()
                                    {
                                        Picasso.with(MainActivity.this).load(image).placeholder(R.drawable.user_icon).into(currentImageCircleImageView);
                                    }
                                });
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {
                    Crashlytics.log(databaseError.getMessage());
                }
            });
        }

        final Set<String> chattingUsers = new HashSet<>(getContactNumbers());

        final RecyclerView chattingUsersRecyclerView = findViewById(R.id.chat_users_recycler_view);
        chattingUsersRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));

        ValueEventListener eventListener = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                finalizedNumbers.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    String userKey = dataSnapshot.getKey();

                    if (userKey != null && !userKey.equals(onlineUserId))
                    {
                        Object number =  dataSnapshot.child("user_number").getValue();
                        Object numberWithPlus = dataSnapshot.child("user_number_with_plus").getValue();

                        if (number != null && numberWithPlus != null)
                        {
                            String mobileNumber = number.toString();
                            String mobileNumberWithPlus = numberWithPlus.toString();

                            for (String mobile : chattingUsers)
                            {
                                if (mobile.equals(mobileNumber) || mobile.equals(mobileNumberWithPlus))
                                {
                                    finalizedNumbers.add(userKey);
                                }
                            }
                        }
                    }
                }
                adapter = new ChattingUsersRecyclerAdapter(finalizedNumbers, MainActivity.this);
                chattingUsersRecyclerView.setAdapter(adapter);

                LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(MainActivity.this,R.anim.layout_slide_bottom);

                chattingUsersRecyclerView.setLayoutAnimation(controller);
                chattingUsersRecyclerView.scheduleLayoutAnimation();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Crashlytics.log(databaseError.getMessage());
            }
        };

        usersRef.addValueEventListener(eventListener);

        MobileAds.initialize(this, "ca-app-pub-4443035718642364~5122394004");

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.loadAd("ca-app-pub-4443035718642364/1432890386", new AdRequest.Builder().build());

        if (mRewardedVideoAd.isLoaded())
        {
            mRewardedVideoAd.show();
        }

        final AdView mAdView = findViewById(R.id.main_adView);
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

    private Set<String> getContactNumbers()
    {
        Set<String> mobileNumbers = new HashSet<>();

        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null);

        if (cursor != null)
        {
            while (cursor.moveToNext())
            {
                String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                mobileNumbers.add(number);
            }
        }

        if (cursor != null)
        {
            cursor.close();
        }

        return mobileNumbers;
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null)
        {
            Intent mainIntent = new Intent(MainActivity.this,WelcomeActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
            finish();
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if (mRewardedVideoAd != null)
        {
            mRewardedVideoAd.pause(this);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (mRewardedVideoAd != null)
        {
            mRewardedVideoAd.resume(this);
        }

        if (checkPermission(PermissionUtil.READ_ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,android.Manifest.permission.RECORD_AUDIO))
            {
                showPermissionExplanation(PermissionUtil.READ_ACCESS_NETWORK_STATE);
            }
            else if (permissionUtil.checkPermissionPreference(PermissionUtil.PERMISSION_ACCESS_NETWORK_STATE))
            {
                requestPermission(PermissionUtil.READ_ACCESS_NETWORK_STATE);
                permissionUtil.updatePermissionPreference(PermissionUtil.PERMISSION_ACCESS_NETWORK_STATE);
            }
            else
            {
                Toast.makeText(MainActivity.this, "Please allow record audio permission in your app settings", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package",MainActivity.this.getPackageName(),null);
                intent.setData(uri);
                this.startActivity(intent);
            }
        }
        if (checkPermission(PermissionUtil.READ_INTERNET) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,android.Manifest.permission.INTERNET))
            {
                showPermissionExplanation(PermissionUtil.READ_INTERNET);
            }
            else if (permissionUtil.checkPermissionPreference(PermissionUtil.PERMISSION_INTERNET))
            {
                requestPermission(PermissionUtil.READ_INTERNET);
                permissionUtil.updatePermissionPreference(PermissionUtil.PERMISSION_INTERNET);
            }
            else
            {
                Toast.makeText(MainActivity.this, "Please allow internet permission in your app settings", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package",MainActivity.this.getPackageName(),null);
                intent.setData(uri);
                this.startActivity(intent);
            }
        }
        if (checkPermission(PermissionUtil.READ_READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,android.Manifest.permission.READ_CONTACTS))
            {
                showPermissionExplanation(PermissionUtil.READ_READ_CONTACTS);
            }
            else if (permissionUtil.checkPermissionPreference(PermissionUtil.PERMISSION_READ_CONTACTS))
            {
                requestPermission(PermissionUtil.READ_READ_CONTACTS);
                permissionUtil.updatePermissionPreference(PermissionUtil.PERMISSION_READ_CONTACTS);
            }
            else
            {
                Toast.makeText(MainActivity.this, "Please allow write contacts permission in your app settings", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package",MainActivity.this.getPackageName(),null);
                intent.setData(uri);
                this.startActivity(intent);
            }
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if (mRewardedVideoAd != null)
        {
            mRewardedVideoAd.destroy(this);
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        if (mRewardedVideoAd != null)
        {
            if (mRewardedVideoAd.isLoaded())
            {
                mRewardedVideoAd.show();
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();

        if (mRewardedVideoAd != null)
        {
            if (mRewardedVideoAd.isLoaded())
            {
                mRewardedVideoAd.show();
            }
        }
    }

    private int checkPermission(int permission)
    {
        int status = PackageManager.PERMISSION_DENIED;

        switch (permission)
        {
            case PermissionUtil.READ_ACCESS_NETWORK_STATE:
                status = ContextCompat.checkSelfPermission(MainActivity.this,android.Manifest.permission.ACCESS_NETWORK_STATE);
                break;
            case PermissionUtil.READ_INTERNET:
                status = ContextCompat.checkSelfPermission(MainActivity.this,android.Manifest.permission.INTERNET);
                break;
            case PermissionUtil.READ_READ_CONTACTS:
                status = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS);
                break;
        }
        return status;
    }

    private void requestPermission(int permission)
    {
        switch (permission)
        {
            case PermissionUtil.READ_ACCESS_NETWORK_STATE:
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{android.Manifest.permission.READ_CONTACTS},PermissionUtil.REQUEST_ACCESS_NETWORK_STATE);
                break;
            case PermissionUtil.READ_INTERNET:
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{android.Manifest.permission.INTERNET},PermissionUtil.REQUEST_INTERNET);
                break;
            case PermissionUtil.READ_READ_CONTACTS:
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{android.Manifest.permission.READ_CONTACTS},PermissionUtil.REQUEST_READ_CONTACTS);
                break;
        }
    }

    private void showPermissionExplanation(final int permission)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        switch (permission)
        {
            case PermissionUtil.READ_ACCESS_NETWORK_STATE:
                builder.setMessage("This app need to access your network state..");
                builder.setTitle("Network Permission Needed..");
                break;
            case PermissionUtil.READ_INTERNET:
                builder.setMessage("This app need to access your internet..");
                builder.setTitle("Internet Permission Needed..");
                break;
            case PermissionUtil.READ_READ_CONTACTS:
                builder.setMessage("This app need to access your contacts..");
                builder.setTitle("Contacts Permission Needed..");
                break;
        }

        builder.setPositiveButton("Allow", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                switch (permission)
                {
                    case PermissionUtil.READ_ACCESS_NETWORK_STATE:
                        requestPermission(PermissionUtil.READ_ACCESS_NETWORK_STATE);
                        break;
                    case PermissionUtil.READ_INTERNET:
                        requestPermission(PermissionUtil.READ_INTERNET);
                        break;
                    case PermissionUtil.READ_READ_CONTACTS:
                        requestPermission(PermissionUtil.READ_READ_CONTACTS);
                        break;
                }
            }
        });

        builder.setNegativeButton("Deny", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}