package com.empti.firebaseauthdemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button register;
    private EditText useremail;
    private EditText userpassword;
    private TextView usersignin;
    private EditText displayname;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {

            finish();
            //start profile activity
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));

        }
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("User");
        //mUserRef = FirebaseDatabase.getInstance().getReference().child("User").child(mAuth.getCurrentUser().getUid());

        progressDialog = new ProgressDialog(this);

        displayname = (EditText) findViewById(R.id.displayname);
        register = (Button) findViewById(R.id.register);
        useremail = (EditText) findViewById(R.id.email);
        userpassword = (EditText) findViewById(R.id.password);

        usersignin = (TextView) findViewById(R.id.usersignin);


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
        usersignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,LoginActivity.class));

            }
        });
    }

    private void registerUser() {
        String email = useremail.getText().toString().trim();
        String password = userpassword.getText().toString().trim();
        final String display = displayname.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            //email is empty
            Toast.makeText(this, "Enter your email", Toast.LENGTH_SHORT).show();
            //stop the functions execution
            return;
        }
        if (TextUtils.isEmpty(password)) {
            //password is empty
            Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show();
            //stop the functions execution
            return;
        }
        if (TextUtils.isEmpty(display)) {
            //password is empty
            Toast.makeText(this, "Enter Display Name", Toast.LENGTH_SHORT).show();
            //stop the functions execution
            return;
        }

        //if validation are ok
        //we will first show a progress
        progressDialog.setMessage("Registering User....");
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = currentUser.getUid();

                            mDatabase = FirebaseDatabase.getInstance().getReference().child("User").child(uid);

                            HashMap<String, String> userMap = new HashMap<String, String>();
                            userMap.put("name", display);
                            userMap.put("status", "Hi there I'm using MUSKY");
                            userMap.put("image", "default");
                            userMap.put("thumb_img", "default");
                            userMap.put("online","true");

                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        String mCurrentuserid = mAuth.getCurrentUser().getUid();
                                        String devicetoken = FirebaseInstanceId.getInstance().getToken();
                                        mUserDatabase.child(mCurrentuserid).child("device_token").setValue(devicetoken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                finish();
                                                //start profile activity
                                                startActivity(new Intent(MainActivity.this, ProfileActivity.class));


                                                //user is successfully registered and login
                                                //we will start the profile actitvity
                                                Toast.makeText(MainActivity.this, "Registered Successfully", Toast.LENGTH_SHORT).show();

                                            }
                                        });

                                    }
                                }
                            });

                        } else {
                            Toast.makeText(MainActivity.this, "Registration is Failed! Try Again Later...", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });

    }

    @Override
    public void onClick(View v) {
        if (v == register) {
            registerUser();
        }
        if (v == usersignin) {
            //we open login activity
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }

    }


    @Override
    protected void onStop() {
        super.onStop();

       /* if (mAuth.getCurrentUser() != null) {
            mUserRef.child("online").setValue(false);
        }*/

    }

    @Override
    protected void onStart() {
        super.onStart();

       /* if (mAuth.getCurrentUser() == null) {
            mUserRef.child("online").setValue(true);
        }else {
            finish();
            //start profile activity
            startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
        }*/
    }

}