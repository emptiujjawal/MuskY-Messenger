package com.empti.firebaseauthdemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView mProfileimage;
    private TextView mProfilename;
    private TextView mProfileStatus;
    private TextView mProfilefriends;
    private Button mProfileSend;
    private Button mProfileClose;
    private ProgressDialog mprogress;
    private String mcurrentstate;
    private TextView mProfileSendtext;
    private TextView mProfileCanceltext;

    //DATABASE REF
    private DatabaseReference mprofileDatabase;
    private DatabaseReference mfriendDatabase;
    private DatabaseReference mMakeFriendDatabase;
    private FirebaseUser mCurrentuser;
    private DatabaseReference mNotificationDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        final String userid = getIntent().getStringExtra("userid");

        mprofileDatabase = FirebaseDatabase.getInstance().getReference().child("User").child(userid);
        mfriendDatabase = FirebaseDatabase.getInstance().getReference().child("MakeFriends");
        mMakeFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mCurrentuser = FirebaseAuth.getInstance().getCurrentUser();

        mProfileimage = (ImageView)findViewById(R.id.profileuserimage);
        mProfilename =(TextView)findViewById(R.id.profileusername);
        mProfileStatus = (TextView)findViewById(R.id.profileuserstatus);
        mProfilefriends = (TextView)findViewById(R.id.profileuserfriends);
        mProfileSend = (Button)findViewById(R.id.profileusersend);
        mProfileClose = (Button)findViewById(R.id.profileuserclose);
        mProfileCanceltext = (TextView)findViewById(R.id.profileusercanceltext);
        mProfileSendtext = (TextView)findViewById(R.id.profileusersendtext);

        mcurrentstate = "notFriends";

        mprogress = new ProgressDialog(this);
        mprogress.setTitle("Loading the User Data");
        mprogress.setMessage("Please wait while we loading the Profile...");
        mprogress.setCanceledOnTouchOutside(false);
        mprogress.show();

        mprofileDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String displayname = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfilename.setText(displayname);
                mProfileStatus.setText(status);

                Picasso.with(UserProfileActivity.this).load(image).placeholder(R.drawable.p).into(mProfileimage);

                //-------- Friends List/Requests Feature
                mfriendDatabase.child(mCurrentuser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(userid)) {

                            String requesttype = dataSnapshot.child(userid).child("request_type").getValue().toString();
                            if (requesttype.equals("received")) {
                                mcurrentstate = "requestreceived";
                                mProfileSendtext.setText("Accept");
                                mProfileCanceltext.setText("Decline");

                            } else if (requesttype.equals("sent")) {

                                mcurrentstate = "requestsend";
                                mProfileSendtext.setText("Cancel Friend Request");

                            }
                            mprogress.dismiss();
                        }    else {
                                mMakeFriendDatabase.child(mCurrentuser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild(userid)){

                                            mcurrentstate = "Friends";
                                            mProfileSendtext.setText("Friend");
                                            mProfileCanceltext.setText("UnFriend");
                                        }
                                        mprogress.dismiss();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        mprogress.dismiss();

                                    }
                                });
                            }

                        }


                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProfileSend.setEnabled(false);

                //NOT FRIENDS STATE

                if (mcurrentstate.equals("notFriends")){

                    mfriendDatabase.child(mCurrentuser.getUid()).child(userid).child("request_type").setValue("sent")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){

                                mfriendDatabase.child(userid).child(mCurrentuser.getUid()).child("request_type")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        HashMap<String,String> notificationData = new HashMap<String, String>();
                                        notificationData.put("from",mCurrentuser.getUid());
                                        notificationData.put("type","request");

                                        mNotificationDatabase.child(userid).push().setValue(notificationData)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                mProfileSend.setEnabled(true);
                                                mcurrentstate = "requestsend";
                                                mProfileSendtext.setText("Cancel Sent Request");

                                                Toast.makeText(UserProfileActivity.this,"Friend Request Send",Toast.LENGTH_LONG).show();

                                            }
                                        });



                                    }
                                });

                            }else{

                                Toast.makeText(UserProfileActivity.this,"Friend Request Not Send",Toast.LENGTH_LONG).show();

                            }
                        }
                    });

                }

                // SEND STATE

                if (mcurrentstate.equals("requestsend")){

                    mfriendDatabase.child(mCurrentuser.getUid()).child(userid).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            mfriendDatabase.child(userid).child(mCurrentuser.getUid()).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    mProfileSend.setEnabled(true);
                                    mcurrentstate = "notFriends";
                                    mProfileSendtext.setText("Send Friend Request ");

                                    Toast.makeText(UserProfileActivity.this,"Friend Request Cancel",Toast.LENGTH_LONG).show();

                                }
                            });
                        }
                    });

                }
                //---------------Received State/Accept Request

                if (mcurrentstate.equals("requestreceived")){

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    mMakeFriendDatabase.child(mCurrentuser.getUid()).child(userid).child("date").setValue(currentDate)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mMakeFriendDatabase.child(userid).child(mCurrentuser.getUid()).child("date").setValue(currentDate)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mfriendDatabase.child(userid).child(mCurrentuser.getUid()).removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    mfriendDatabase.child(mCurrentuser.getUid()).child(userid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            mProfileSend.setEnabled(true);
                                                            mcurrentstate = "Friends";
                                                            mProfileSendtext.setText("Friend");
                                                            mProfileCanceltext.setText("UnFriend");

                                                            Toast.makeText(UserProfileActivity.this,"Got new Friend",Toast.LENGTH_LONG).show();

                                                        }
                                                    });


                                                }
                                            });

                                }
                            });
                        }
                    });

                }

            }
        });
        mProfileClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProfileClose.setEnabled(false);

               //unFriend
                if (mcurrentstate.equals("Friends")){
               mMakeFriendDatabase.child(mCurrentuser.getUid()).child(userid).removeValue()
                       .addOnCompleteListener(new OnCompleteListener<Void>() {
                   @Override
                   public void onComplete(@NonNull Task<Void> task) {
                       mMakeFriendDatabase.child(userid).child(mCurrentuser.getUid()).removeValue()
                               .addOnCompleteListener(new OnCompleteListener<Void>() {
                                   @Override
                                   public void onComplete(@NonNull Task<Void> task) {

                                       mfriendDatabase.child(mCurrentuser.getUid()).child(userid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                           @Override
                                           public void onComplete(@NonNull Task<Void> task) {
                                               mfriendDatabase.child(userid).child(mCurrentuser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                   @Override
                                                   public void onComplete(@NonNull Task<Void> task) {
                                                       mProfileClose.setEnabled(true);
                                                       mcurrentstate = "notFriends";
                                                       mProfileSendtext.setText("Send Friend Request");
                                                       mProfileCanceltext.setText("Back");

                                                       Toast.makeText(UserProfileActivity.this,"Friend is Gone",Toast.LENGTH_LONG).show();



                                                   }
                                               });

                                           }
                                       });

                                   }
                               });

                   }
               });
                }
                // DEcline friend Request
                else if (mcurrentstate.equals("requestreceived")){

                    mfriendDatabase.child(mCurrentuser.getUid()).child(userid).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mfriendDatabase.child(userid).child(mCurrentuser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileClose.setEnabled(true);
                                    mcurrentstate = "notFriends";
                                    mProfileSendtext.setText("Send Friend Request");
                                    mProfileCanceltext.setText("Back");

                                }
                            });

                        }
                    });



                }

                else {
                    startActivity(new Intent(UserProfileActivity.this,UsersActivity.class));
                }




            }
        });

    }
}
