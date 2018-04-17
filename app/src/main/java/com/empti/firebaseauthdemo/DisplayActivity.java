package com.empti.firebaseauthdemo;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DisplayActivity extends AppCompatActivity {
    private TextInputLayout mdisplayname;
    private Button mnamebtn;

    //firebase
    private DatabaseReference mDisplaynameDatabase;
    private FirebaseUser mCurrentUser;

    //progress
    private ProgressDialog mprogress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        //firebase
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentuid = mCurrentUser.getUid();
        mDisplaynameDatabase = FirebaseDatabase.getInstance().getReference().child("User").child(currentuid);

        String namevalue = getIntent().getStringExtra("namevalue");

        mdisplayname = (TextInputLayout)findViewById(R.id.nameInputLayout);
        mnamebtn = (Button)findViewById(R.id.displaynamesavebtn);
        mdisplayname.getEditText().setText(namevalue);

        mnamebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //progress
                mprogress = new ProgressDialog(DisplayActivity.this);
                mprogress.setTitle("Saving Changes");
                mprogress.setMessage("Please Wait Saving!!");
                mprogress.show();

                String name = mdisplayname.getEditText().getText().toString();
                mDisplaynameDatabase.child("name").setValue(name).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            mprogress.dismiss();

                        }
                        else {
                            Toast.makeText(getApplicationContext(),"There was some error in Saving Changes",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
