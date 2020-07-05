package com.example.chatwith;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private Button  UpdateAccountSettings;
    private EditText userName,userStatus;
    private CircleImageView userProfileImage;

    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private static final int GalleryPick=1;

    private StorageReference UserProfileImageRef;

    private ProgressDialog loadingBar;
    private Toolbar SettingToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        InitializeFields();

        userName.setVisibility(View.INVISIBLE);//first time invisible.if new user then visible below.

        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();

        UserProfileImageRef= FirebaseStorage.getInstance().getReference().child("Profile Images");

        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

        RetrievUserInformation();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GalleryPick);
            }
        });
    }


    private void InitializeFields() {
        UpdateAccountSettings=(Button) findViewById(R.id.update_settings_button);
        userName=(EditText) findViewById(R.id.set_user_name);
        userStatus=(EditText) findViewById(R.id.set_profile_status);
        userProfileImage=(CircleImageView) findViewById(R.id.set_profile_image);
        loadingBar=new ProgressDialog(this);

        //toolbar
        SettingToolBar=(Toolbar) findViewById(R.id.setting_toolBar);
        setSupportActionBar(SettingToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");
    }


    //select crop images;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GalleryPick && resultCode==RESULT_OK && data!=null)
        {
            Uri ImageUri=data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK)
            {
                loadingBar.setTitle("set profile image");
                loadingBar.setMessage("Please wait,your profile image is uploading...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();
                Uri resultUri=result.getUri();//file:///data/user/0/com.example.chatwith/cache/cropped223879496.jpg
                final StorageReference filePath=UserProfileImageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {

                        Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                        task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                               final String downloadUrl = uri.toString();
                               // Log.d("photoLink",downloadUrl);
//                                RootRef.child("Users").child(currentUserID).child("image")
//                                        .setValue(downloadUrl)
//                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<Void> task)
//                                            {
//                                                if(task.isSuccessful())
//                                                {
//                                                    loadingBar.dismiss();
//                                                }
//                                                else
//                                                {
//                                                    loadingBar.dismiss();
//                                                    String message=task.getException().toString();
//                                                    Toast.makeText(SettingsActivity.this, "Erro: "+ message, Toast.LENGTH_SHORT).show();
//                                                }
//                                            }
//                                        });

                                RootRef.child("Users").child(currentUserID).child("image")
                                    .setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                loadingBar.dismiss();
                                            }
                                            else
                                            {
                                                loadingBar.dismiss();
                                                String message=task.getException().toString();
                                                Toast.makeText(SettingsActivity.this, "Error: "+ message, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            }
                        });
                    }
                });

//                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
//                    {
//                        if(task.isSuccessful())
//                        {
//                            Toast.makeText(SettingsActivity.this, "Profile image Uploaded successfully..", Toast.LENGTH_SHORT).show();
//                            final String downloadUrl=task.getResult().getStorage().getDownloadUrl().toString();
//
//                            RootRef.child("Users").child(currentUserID).child("image")
//                                    .setValue(downloadUrl)
//                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task)
//                                        {
//                                            if(task.isSuccessful())
//                                            {
//                                                loadingBar.dismiss();
//                                            }
//                                            else
//                                            {
//                                                loadingBar.dismiss();
//                                                String message=task.getException().toString();
//                                                Toast.makeText(SettingsActivity.this, "Error: "+ message, Toast.LENGTH_SHORT).show();
//                                            }
//                                        }
//                                    });
//
//                        }
//                        else
//                        {
//                            loadingBar.dismiss();
//                            String message=task.getException().toString();
//                            Toast.makeText(SettingsActivity.this, "Error : "+ message, Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
            }
        }
    }

    private void UpdateSettings() {
        String setUserName=userName.getText().toString();
        String setStatus=userStatus.getText().toString();
        if(TextUtils.isEmpty(setUserName)){
            Toast.makeText(this, "Please write your name..", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(setStatus)){
            Toast.makeText(this, "Please write your status..", Toast.LENGTH_SHORT).show();
        }
        else{
            //save data to firebase
            HashMap<String,Object> profileMap=new HashMap<>();
            profileMap.put("uid",currentUserID);
            profileMap.put("name",setUserName);
            profileMap.put("status",setStatus);
            RootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                SendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                String message=task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }


    }
    private void SendUserToMainActivity()
    {
        Intent mainIntent=new Intent(SettingsActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);    //user do not go back if press back button
        startActivity(mainIntent);
        finish();
    }

    private void RetrievUserInformation() {
      RootRef.child("Users").child(currentUserID)
              .addValueEventListener(new ValueEventListener() {
                  @Override
                  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                      if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image"))))
                      {
                          String retrieveUserName=dataSnapshot.child("name").getValue().toString();
                          String retrieveStatus=dataSnapshot.child("status").getValue().toString();
                          String retrieveProfileImage=dataSnapshot.child("image").getValue().toString();

                          userName.setText(retrieveUserName);
                          userStatus.setText(retrieveStatus);

                          Picasso.get().load(retrieveProfileImage).into(userProfileImage);
                      }
                      else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")))
                      {
                          String retrieveUserName=dataSnapshot.child("name").getValue().toString();
                          String retrieveStatus=dataSnapshot.child("status").getValue().toString();

                          userName.setText(retrieveUserName);
                          userStatus.setText(retrieveStatus);
                      }
                      else {
                          userName.setVisibility(View.VISIBLE);//if new user then visible text field
                          Toast.makeText(SettingsActivity.this, "Please set & update your profile information..", Toast.LENGTH_SHORT).show();
                       }
                  }

                  @Override
                  public void onCancelled(@NonNull DatabaseError databaseError) {

                  }
              });
    }


}