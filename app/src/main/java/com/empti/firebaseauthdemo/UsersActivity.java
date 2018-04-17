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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class UsersActivity extends AppCompatActivity {
    private RecyclerView muserlist;
    private DatabaseReference mUserData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mUserData = FirebaseDatabase.getInstance().getReference().child("User");

        muserlist = (RecyclerView)findViewById(R.id.Userlist);
        muserlist.setHasFixedSize(true);
        muserlist.setLayoutManager(new LinearLayoutManager(this));


    }

    @Override
    protected void onStart() {
        super.onStart();


        FirebaseRecyclerAdapter<Users,UserViewHolder> firebaseRecyclerAdapter= new FirebaseRecyclerAdapter<Users, UserViewHolder>(
                Users.class,
                R.layout.userssinglelayout,
                UserViewHolder.class,
                mUserData

        ) {
            @Override
            protected void populateViewHolder(UserViewHolder viewHolder, Users model, int position) {

                viewHolder.setName(model.getName());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setThumb_img(model.getThumb_img(),getApplicationContext());

                final String userid = getRef(position).getKey();

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent userprofileIntent = new Intent(UsersActivity.this,UserProfileActivity.class);
                        userprofileIntent.putExtra("userid",userid);
                        startActivity(userprofileIntent);
                    }
                });


            }
        };
        muserlist.setAdapter(firebaseRecyclerAdapter);
    }

    public  static class UserViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public UserViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

        }
        public void setName(String name){
            TextView musernameview = (TextView) mView.findViewById(R.id.usersinglename);
            musernameview.setText(name);

        }
        public void setStatus(String status){
            TextView muserstatusview = (TextView) mView.findViewById(R.id.usersinglestatus);
            muserstatusview.setText(status);

        }
        public void setThumb_img(String thumb_img , Context ctx){
            CircleImageView muserimgeview = (CircleImageView)mView.findViewById(R.id.usersingleimage);

            Picasso.with(ctx).load(thumb_img).placeholder(R.drawable.p).into(muserimgeview);
        }


    }
}
