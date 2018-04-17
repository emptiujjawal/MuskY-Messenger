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

public class StatusActivity extends AppCompatActivity {
    private TextInputLayout mStatus;
    private Button mstatusbtn;

    //firebase
    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;

    //progress
    private ProgressDialog mprogress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        //firebase
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentuid = mCurrentUser.getUid();
        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("User").child(currentuid);

        String statusvalue = getIntent().getStringExtra("statusvalue");

        mStatus = (TextInputLayout)findViewById(R.id.statusInputLayout);
        mstatusbtn = (Button)findViewById(R.id.statussavebtn);
        mStatus.getEditText().setText(statusvalue);

        mstatusbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //progress
                mprogress = new ProgressDialog(StatusActivity.this);
                mprogress.setTitle("Saving Changes");
                mprogress.setMessage("Please Wait Saving!!");
                mprogress.show();

                String status = mStatus.getEditText().getText().toString();
                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
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
