package com.empti.firebaseauthdemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class AccountActivity extends AppCompatActivity {

    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;

    //android layout
    private CircleImageView mDisplayImage;
    private TextView mDisplaystatus;
    private Button mChangeProfile;
    private Button mChangestatus;
    private Button mChangeDisplayname;
    private TextView mDisplayname;
    private static final int GALLERYPICK = 1;
    //Storage
    private StorageReference mImageStorage;
    //progress
    private ProgressDialog mprogress;
    private DatabaseReference mUserRef;
    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        mDisplayImage = (CircleImageView)findViewById(R.id.circularimage);
        mDisplayname =(TextView)findViewById(R.id.settingdisplay);
        mDisplaystatus= (TextView)findViewById(R.id.settingstatus);
        mChangeDisplayname= (Button)findViewById(R.id.displaynamebutton);
        mChangeProfile= (Button)findViewById(R.id.picbutton);
        mChangestatus = (Button)findViewById(R.id.statusbutton);

        mImageStorage = FirebaseStorage.getInstance().getReference();

        mAuth =FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null ) {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("User").child(mAuth.getCurrentUser().getUid());
        }

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentuid = mCurrentUser.getUid();


        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("User").child(currentuid);
        mUserDatabase.keepSynced(true);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                final String image =dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_img = dataSnapshot.child("thumb_img").getValue().toString();

                mDisplaystatus.setText(status);
                mDisplayname.setText(name);

                if (!image.equals("default")){


                    //Picasso.with(AccountActivity.this).load(image).placeholder(R.drawable.p).into(mDisplayImage);
                    Picasso.with(AccountActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.p).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                            Picasso.with(AccountActivity.this).load(image).placeholder(R.drawable.p).into(mDisplayImage);

                        }

                });
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mChangestatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String statusvalue = mDisplaystatus.getText().toString();

                Intent mchangestatusintent = new Intent(AccountActivity.this,StatusActivity.class);
                mchangestatusintent.putExtra("statusvalue",statusvalue);
                startActivity(mchangestatusintent);
            }
        });

        mChangeDisplayname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String namevalue = mDisplayname.getText().toString();
                Intent mchangenameintent = new Intent(AccountActivity.this,DisplayActivity.class);
                mchangenameintent.putExtra("namevalue",namevalue);
                startActivity(mchangenameintent);

            }
        });
        mChangeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
/*
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERYPICK);
                */
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(AccountActivity.this);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERYPICK && requestCode== RESULT_OK){
            Uri imageUri =data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .setMinCropWindowSize(500,500)
                    .start(this);

            //Toast.makeText(AccountActivity.this,imageUri,Toast.LENGTH_LONG).show();

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mprogress = new ProgressDialog(AccountActivity.this);
                mprogress.setTitle("Uploading Image.....");
                mprogress.setMessage("Please wait while we uploding the image....");
                mprogress.setCanceledOnTouchOutside(false);
                mprogress.show();

                Uri resultUri = result.getUri();

                File thumbfilepath = new File(resultUri.getPath());

                String currentuserid = mCurrentUser.getUid();

                Bitmap thumbimage = null;
                try {
                    thumbimage = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(75)
                            .compressToBitmap(thumbfilepath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumbimage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    final byte[] thumbbyte = baos.toByteArray();




                StorageReference filepath = mImageStorage.child("profileimgs").child(currentuserid+".jpg");
                final StorageReference thumbfilepaths = mImageStorage.child("profileimgs").child("thumb").child(currentuserid+".jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                   if (task.isSuccessful()){

                       final String downloadurl = task.getResult().getDownloadUrl().toString();

                       UploadTask uploadTask = thumbfilepaths.putBytes(thumbbyte);
                       uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                           @Override
                           public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumbtask) {

                               String thumbdownloadurl = thumbtask.getResult().getDownloadUrl().toString();

                               if (thumbtask.isSuccessful()){

                                   Map updatehashmap = new HashMap();
                                   updatehashmap.put("image",downloadurl);
                                   updatehashmap.put("thumb_img",thumbdownloadurl);

                                   mUserDatabase.updateChildren(updatehashmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task) {
                                           if (task.isSuccessful()){
                                               mprogress.dismiss();
                                               Toast.makeText(AccountActivity.this,"Successfully Uploaded!!",Toast.LENGTH_LONG).show();

                                           }
                                       }
                                   });




                               }else{
                                   Toast.makeText(AccountActivity.this,"Bitmap Image cant upload!!",Toast.LENGTH_LONG).show();
                                   mprogress.dismiss();
                               }

                           }
                       });



                   }else{
                       Toast.makeText(AccountActivity.this,"Image cant upload!!",Toast.LENGTH_LONG).show();
                       mprogress.dismiss();
                   }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }
        }
    }
   /* public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }*/


    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() !=null) {
            mUserRef.child("online").setValue(true);
        }

    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mAuth.getCurrentUser() != null) {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

  /*  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.accountmenu, menu);

        return true;
    }

  @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
        if (item.getItemId()== R.id.deleteaccount){
  /*          mDelete = FirebaseDatabase.getInstance().getReference().child("User").child(currentuid);
            mDelete.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    startActivity(new Intent(AccountActivity.this,MainActivity.class));
                    Toast.makeText(AccountActivity.this,"Account Deleted",Toast.LENGTH_LONG).show();
                }
            });


        }
        return true;
    }*/
}
