package com.empti.firebaseauthdemo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsMade extends AppCompatActivity {
    private RecyclerView mfriendmadelist;
    private DatabaseReference mfriendData;
    private FirebaseAuth mAuth;
    private String mCurrentuserid;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mUserRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_made);

        mfriendmadelist = (RecyclerView )findViewById(R.id.friendmadelist);
        mfriendmadelist.setHasFixedSize(true);
        mfriendmadelist.setLayoutManager(new LinearLayoutManager(this));

        mAuth = FirebaseAuth.getInstance();

      if (mAuth.getCurrentUser() !=null ){
          mUserRef = FirebaseDatabase.getInstance().getReference().child("User").child(mAuth.getCurrentUser().getUid());
        }

        mCurrentuserid = mAuth.getCurrentUser().getUid();

        mfriendData = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentuserid);
        mfriendData.keepSynced(true);
        mUserDatabase =FirebaseDatabase.getInstance().getReference().child("User");
        mUserDatabase.keepSynced(true);


    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser mUser = mAuth.getCurrentUser();

        if (mUser !=null) {

            mUserRef.child("online").setValue(true);
       }

        FirebaseRecyclerAdapter<friends,friendsViewHolder>friendsRecyclerAdapter = new FirebaseRecyclerAdapter<friends,
                        friendsViewHolder>(

                friends.class,
                R.layout.friendsinglelayout,
                friendsViewHolder.class,
                mfriendData


        ) {
            @Override
            protected void populateViewHolder(final friendsViewHolder viewHolder, final friends model, int position) {

                viewHolder.setDate(model.getDate());

                final String userid = getRef(position).getKey();

                mUserDatabase.child(userid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String username = dataSnapshot.child("name").getValue().toString();
                        String userstatus = dataSnapshot.child("status").getValue().toString();
                        final String thumbimage = dataSnapshot.child("thumb_img").getValue().toString();
                        if (dataSnapshot.hasChild("online")){
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(userOnline);
                        }


                        viewHolder.setName(username);
                        viewHolder.setStatus(userstatus);
                        viewHolder.setThumb(thumbimage,getApplicationContext());


                       viewHolder.mfriendView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                              /* CharSequence option[] = new CharSequence[]{"Open Profile","Send Message"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                                builder.setTitle("Select Options");
                                builder.setItems(option, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        //click event for each item
                                       if (which ==0){

                                            Intent userprofileIntent = new Intent(FriendsMade.this,UserProfileActivity.class);
                                            userprofileIntent.putExtra("userid",userid);
                                            startActivity(userprofileIntent);

                                        }
                                        if (which ==1 ){

                                            Intent userprofileIntent = new Intent(FriendsMade.this,ChatActivity.class);
                                            userprofileIntent.putExtra("userid",userid);
                                            startActivity(userprofileIntent);
                                            
                                        }


                                    }
                                });
                                builder.show();*/
                                Intent userprofileIntent = new Intent(FriendsMade.this,ChatActivity.class);
                                userprofileIntent.putExtra("userid",userid);
                                userprofileIntent.putExtra("name",username);
                                startActivity(userprofileIntent);


                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {


                    }
                });
            }
        };

        mfriendmadelist.setAdapter(friendsRecyclerAdapter);


    }

    public static class friendsViewHolder extends RecyclerView.ViewHolder {

        View mfriendView;
       // TextView mDate;
       // Button sendMessage;

        public friendsViewHolder(View itemView) {
            super(itemView);

            mfriendView = itemView;
           // mDate = (TextView)itemView.findViewById(R.id.friendsingledate);
            //sendMessage = (Button)itemView.findViewById(R.id.sendmessagebtn);
        }
        public void setDate(String date){
            TextView mDate = (TextView)mfriendView.findViewById(R.id.friendsingledate);
            mDate.setText(date);
            Button sendMessage = (Button)mfriendView.findViewById(R.id.sendmessagebtn);
            if (date.equals(null)){
                sendMessage.setVisibility(View.INVISIBLE);

            }else{
                sendMessage.setVisibility(View.VISIBLE);
            }



        }
        public void setName(String name){
            TextView musername = (TextView)mfriendView.findViewById(R.id.friendsinglename);
            musername.setText(name);
        }
        public  void  setStatus(String status){
            TextView muserstatus = (TextView)mfriendView.findViewById(R.id.friendsinglestatus);
            muserstatus.setText(status);
        }
        public  void setThumb(final String thumb_img, final Context ctx){
            final CircleImageView muserimgeview = (CircleImageView)mfriendView.findViewById(R.id.friendsingleimage);

            //Picasso.with(ctx).load(thumb_img).placeholder(R.drawable.p).into(muserimgeview);
            Picasso.with(ctx).load(thumb_img).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.p).into(muserimgeview, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(ctx).load(thumb_img).placeholder(R.drawable.p).into(muserimgeview);

                }
            });
        }
        public void setUserOnline(String online_status){
            ImageView useronline = (ImageView)mfriendView.findViewById(R.id.onlineimage);
            if (online_status.equals(true)){
                useronline.setVisibility(View.VISIBLE);
            }else{
                useronline.setVisibility(View.INVISIBLE);
            }

        }
        

    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser mUser = mAuth.getCurrentUser();
        if(mUser != null ){
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }


    }


}
