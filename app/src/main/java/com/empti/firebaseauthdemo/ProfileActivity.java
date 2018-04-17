package com.empti.firebaseauthdemo;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity  {
    private FirebaseAuth mAth;
    private ViewPager mViewpager;
    private RecyclerView muserlist;
    private DatabaseReference mUserData;
    private DatabaseReference mUserRef;
    private String mCurrentid;
    private DatabaseReference mUserDatabase;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
       mAth = FirebaseAuth.getInstance();
        mCurrentid = mAth.getCurrentUser().getUid();


      if (mAth.getCurrentUser() != null ){
          mUserRef = FirebaseDatabase.getInstance().getReference().child("User").child(mAth.getCurrentUser().getUid());
      }

        mUserData = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrentid);

        muserlist = (RecyclerView)findViewById(R.id.chatdisplaylist);
        muserlist.setHasFixedSize(true);
        muserlist.setLayoutManager(new LinearLayoutManager(this));
        mUserDatabase =FirebaseDatabase.getInstance().getReference().child("User");



            //tabs
        /*mViewpager = (ViewPager)findViewById(R.id.tabpager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewpager.setAdapter(mSectionsPagerAdapter);

       // mUserRef = FirebaseDatabase.getInstance().getReference().child("User").child(mAth.getCurrentUser().getUid());

        mtablayout = (TabLayout)findViewById(R.id.maintabs);
        mtablayout.setupWithViewPager(mViewpager);*/

    }

    private void senttostart(){
        //user not login
        //start login activity

        finish();
        startActivity(new Intent(this,LoginActivity.class));
//        mUserRef.child("online").setValue(ServerValue.TIMESTAMP);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.mainmenu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);

        if (item.getItemId()== R.id.mainlogoutbutton){
            FirebaseAuth.getInstance().signOut();
            senttostart();
        }
        if (item.getItemId()== R.id.mainsettingbutton){
            Intent settingintent = new Intent(ProfileActivity.this,AccountActivity.class);
            startActivity(settingintent);
        }
        if (item.getItemId()== R.id.mainallbutton){
            Intent usersetting = new Intent(ProfileActivity.this,UsersActivity.class);
            startActivity(usersetting);

        }
        if (item.getItemId()== R.id.menufriends){
            Intent userfriends = new Intent(ProfileActivity.this,FriendsMade.class);
            startActivity(userfriends);

        }
        if (item.getItemId()== R.id.menuactivity){
            Intent useractivity = new Intent(ProfileActivity.this,UserActivity.class);
            startActivity(useractivity);

        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser mUser = mAth.getCurrentUser();
        if (mUser == null){

            senttostart();
        }
     else
        {
            mUserRef.child("online").setValue(true);
        }

        FirebaseRecyclerAdapter<ChatDisplay,ChatViewHolder>displayChatViewHolderFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ChatDisplay, ChatViewHolder>(
                ChatDisplay.class,
                R.layout.chatdisplay,
                ChatViewHolder.class,
                mUserData
        ) {
            @Override
            protected void populateViewHolder(final ChatViewHolder viewHolder, ChatDisplay model, int position) {

                final String muserref  = getRef(position).getKey();
                mUserDatabase.child(muserref).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String username = dataSnapshot.child("name").getValue().toString();

                        final String thumbimage = dataSnapshot.child("thumb_img").getValue().toString();

                        viewHolder.setName(username);

                        viewHolder.setThumb_img(thumbimage,getApplicationContext());

                        viewHolder.mChatView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent userprofileIntent = new Intent(ProfileActivity.this,ChatActivity.class);
                                userprofileIntent.putExtra("userid",muserref);
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
        muserlist.setAdapter(displayChatViewHolderFirebaseRecyclerAdapter);



    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser mUser = mAth.getCurrentUser();
        if (mUser !=null) {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder{
        View mChatView;

        public ChatViewHolder(View itemView) {
            super(itemView);
            mChatView = itemView;
        }
        public void setName(String name){
            TextView musernameview = (TextView) mChatView.findViewById(R.id.chatprofiledisplay);
            musernameview.setText(name);

        }
        public void setThumb_img(String thumb_img , Context ctx){
            CircleImageView muserimgeview = (CircleImageView)mChatView.findViewById(R.id.chatprofileimg);

            Picasso.with(ctx).load(thumb_img).placeholder(R.drawable.p).into(muserimgeview);
        }
    }



}
