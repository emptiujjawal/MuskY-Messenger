package com.empti.firebaseauthdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {


    private String mChatUser;
    private Toolbar mtoolbar;
    private DatabaseReference mRootRef;
    private String mChatname;

    private  CircleImageView Chatuserprofile;
    private  TextView ChatUser;
    private TextView chatlastseen;
    private FirebaseAuth mAuth;
    private String mCurrentid;
    private ImageButton addButton;
    private ImageButton sendButton;
    private EditText Messagebox;
    private RecyclerView mChatlist;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mRootRef= FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentid = mAuth.getCurrentUser().getUid();


        mChatUser = getIntent().getStringExtra("userid");
        mChatname = getIntent().getStringExtra("name");
        ChatUser = (TextView)findViewById(R.id.chatuname);
        ChatUser.setText(mChatname);
        Chatuserprofile = (CircleImageView)findViewById(R.id.chatuimg);
        chatlastseen = (TextView)findViewById(R.id.chatulastseen);
        addButton = (ImageButton)findViewById(R.id.chatuaddbtn);
        sendButton = (ImageButton)findViewById(R.id.chatusendbtn);
        Messagebox = (EditText)findViewById(R.id.chatuedit);

        mAdapter = new MessageAdapter(messagesList);
        mChatlist = (RecyclerView)findViewById(R.id.chatulist);
        mLinearLayout = new LinearLayoutManager(this);
        mChatlist.setHasFixedSize(true);
        mChatlist.setLayoutManager(mLinearLayout);

        mChatlist.setAdapter(mAdapter);
        loadMessages();



        //profile of chat user

        mRootRef.child("User").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();
                String img = dataSnapshot.child("image").getValue().toString();

                if (online.equals("true")){
                    chatlastseen.setText("Online");


                }else
                {

                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastseen = Long.parseLong(online);
                    String lastseentime = getTimeAgo.getTimeAgo(lastseen,getApplicationContext());

                    chatlastseen.setText(lastseentime);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chat").child(mCurrentid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(mChatUser)){

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentid + "/" + mChatUser,chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentid,chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {

                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                      if(databaseError != null){

                          Log.d("Chat Log",databaseError.getMessage().toString());

                      }
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



         /*mtoolbar = (Toolbar)findViewById(R.id.appbarchat);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(chatusername);
*/

//button work
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendmessage();
            }
        });

    }

    private void loadMessages() {

        mRootRef.child("messages").child(mCurrentid).child(mChatUser).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = dataSnapshot.getValue(Messages.class);

                messagesList.add(message);
                mAdapter.notifyDataSetChanged();
                mChatlist.scrollToPosition(messagesList.size() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }

    private void sendmessage() {
        String message = Messagebox.getText().toString();
        if (!TextUtils.isEmpty(message)){

            String  currentuserRef = "messages/" + mCurrentid + "/" + mChatUser;
            String chatuserRef ="messages/" + mChatUser + "/" +mCurrentid;

            DatabaseReference usermessagepush = mRootRef.child("messages").child(mCurrentid)
                    .child(mChatUser).push();
            String pushid = usermessagepush.getKey();

            Map messageMap = new HashMap();
            messageMap.put( "message",message );
            messageMap.put( "seen",false);
            messageMap.put ( "type","text");
            messageMap.put( "time", ServerValue.TIMESTAMP);
            messageMap.put("from",mCurrentid);

            Map messageUserMap = new HashMap();
            messageUserMap.put(currentuserRef + "/" + pushid, messageMap);
            messageUserMap.put(chatuserRef + "/" + pushid,messageMap);

            Messagebox.setText(" ");

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError != null){

                        Log.d("Chat Log",databaseError.getMessage().toString());

                    }
                }
            });



        }
    }
}
