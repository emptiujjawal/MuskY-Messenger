package com.empti.firebaseauthdemo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserActivity extends AppCompatActivity {
    private RecyclerView mrequestlist;
    private DatabaseReference mrequestData;
    private FirebaseAuth mAuth;
    private String mCurrentuserid;
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_useractivity);

        mrequestlist = (RecyclerView )findViewById(R.id.activitylist);
        mrequestlist.setHasFixedSize(true);
        mrequestlist.setLayoutManager(new LinearLayoutManager(this));


        mAuth = FirebaseAuth.getInstance();
        mCurrentuserid = mAuth.getCurrentUser().getUid();
        mrequestData = FirebaseDatabase.getInstance().getReference().child("MakeFriends").child(mCurrentuserid);
        mrequestData.keepSynced(true);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("User");
        mUserDatabase.keepSynced(true);

    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Request,RequestViewHolder> RequestAdapter = new FirebaseRecyclerAdapter<Request, RequestViewHolder>(

                Request.class,
                R.layout.friendsinglelayout,
                RequestViewHolder.class,
                mrequestData


        ) {
            @Override
            protected void populateViewHolder(final RequestViewHolder viewHolder, Request model, int position) {

                viewHolder.setRequestType(model.getRequest_type());
                final String userid = getRef(position).getKey();

                mUserDatabase.child(userid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String username = dataSnapshot.child("name").getValue().toString();
                        String userstatus = dataSnapshot.child("status").getValue().toString();
                        String thumbimage = dataSnapshot.child("thumb_img").getValue().toString();

                        viewHolder.setName(username);
                        viewHolder.setStatus(userstatus);
                        viewHolder.setThumb(thumbimage,getApplicationContext());

                        viewHolder.mRequestview.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent userprofileIntent = new Intent(UserActivity.this,UserProfileActivity.class);
                                userprofileIntent.putExtra("userid",userid);
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


mrequestlist.setAdapter(RequestAdapter);


    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder{
        View mRequestview;


        public RequestViewHolder(View itemView) {
            super(itemView);

            mRequestview = itemView;
        }
        public void setRequestType(String request_type){
            TextView mRequest = (TextView)mRequestview.findViewById(R.id.friendsingledate);
            mRequest.setText(request_type);

        }
        public void setName(String name){
            TextView mRequestname = (TextView)mRequestview.findViewById(R.id.friendsinglename);
            mRequestname.setText(name);
        }
        public  void  setStatus(String status){
            TextView mrequeststatus = (TextView)mRequestview.findViewById(R.id.friendsinglestatus);
            mrequeststatus.setText(status);
        }
        public  void setThumb(final String thumb_img, final Context ctx){
            final CircleImageView muserimgeview = (CircleImageView)mRequestview.findViewById(R.id.friendsingleimage);

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




    }
}
