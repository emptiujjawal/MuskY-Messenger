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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button signin;
    private EditText luseremail;
    private EditText luserpassword;
    private TextView usersignup;
    private FirebaseAuth mAuth;
    /*private FirebaseAuth.AuthStateListener mAuthListener;*/
    private DatabaseReference mUserDatabase;
    private ProgressDialog mLoginprocess;
    private DatabaseReference mUserRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("User");
       // mUserRef = FirebaseDatabase.getInstance().getReference().child("User").child(mAuth.getCurrentUser().getUid());
        /*mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null ){
                    startActivity(new Intent(LoginActivity.this,ProfileActivity.class));
                }
            }
        };*/

        mLoginprocess = new ProgressDialog(this);
        signin = (Button) findViewById(R.id.signin);
        luseremail = (EditText) findViewById(R.id.lemail);
        luserpassword = (EditText) findViewById(R.id.passwordl);

        usersignup = (TextView) findViewById(R.id.usersignup);

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = luseremail.getText().toString();
                String password = luserpassword.getText().toString();

                if (!TextUtils.isEmpty(email) | !TextUtils.isEmpty(password)) {

                    mLoginprocess.setTitle("Logging IN");
                    mLoginprocess.setMessage("Please Wait...Updating Your Data");
                    mLoginprocess.setCanceledOnTouchOutside(false);
                    mLoginprocess.show();
                    userLogin(email, password);

                } else {
                    Toast.makeText(LoginActivity.this, "Fill the Fields", Toast.LENGTH_SHORT).show();
                    return;
                }


            }
        });
        usersignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));

            }
        });

    }

    /*@Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }*/

    private void userLogin(String email, String password) {


        //if validation are ok
        //we will first show a progress

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            mLoginprocess.dismiss();

                            String mCurrentuserid = mAuth.getCurrentUser().getUid();
                            String devicetoken = FirebaseInstanceId.getInstance().getToken();
                            mUserDatabase.child(mCurrentuserid).child("device_token").setValue(devicetoken)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            Intent mainIntent = new Intent(LoginActivity.this, ProfileActivity.class);
                                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(mainIntent);
                                            finish();

                                            Toast.makeText(LoginActivity.this, "Login In", Toast.LENGTH_SHORT).show();

                                        }
                                    });

                        } else {
                            mLoginprocess.hide();

                            Toast.makeText(LoginActivity.this, "LoginIn is Failed! Try Again Later...", Toast.LENGTH_SHORT).show();

                        }

                    }
                });

    }


    @Override
    public void onClick(View v) {




    }

   /* @Override
    protected void onStart() {
        super.onStart();

       if (mAuth.getCurrentUser() != null) {
            mUserRef.child("online").setValue(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

       if (mAuth.getCurrentUser() != null) {
            mUserRef.child("online").setValue(false);
        }
    }
    */
}
