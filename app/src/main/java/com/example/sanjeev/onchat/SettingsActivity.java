package com.example.sanjeev.onchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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
    private Button UpdateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private static final int GalleryPic=1;
    private StorageReference UserProfileImageRef;
    private ProgressDialog loadingBar;
    private String downloadUrl;
    private Toolbar settingsToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        settingsToolbar=(Toolbar)findViewById(R.id.settings_toolbar) ;
        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        InitializeFields();
        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        rootRef=FirebaseDatabase.getInstance().getReference();
        UserProfileImageRef=FirebaseStorage.getInstance().getReference().child("Profile Images");
        loadingBar=new ProgressDialog(this);

        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });
        RetrieveUserInfo();
        userName.setVisibility(View.INVISIBLE);

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GalleryPic); ;

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GalleryPic && resultCode==RESULT_OK && data!=null)
        {
            Uri imageUri=data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK)
            {
                loadingBar.setTitle("Uploading Image");
                loadingBar.setMessage("Please Wait");
                loadingBar.show();
                final Uri resultUri=result.getUri();
                final StorageReference filePath=UserProfileImageRef.child(currentUserID+".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(SettingsActivity.this,"Uploaded Successfully",Toast.LENGTH_LONG).show();
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(final Uri uri) {
                                    downloadUrl=uri.toString();
                                    rootRef.child("Users").child(currentUserID).child("image").setValue(uri.toString())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful())
                                                    {loadingBar.dismiss();
                                                        Toast.makeText(SettingsActivity.this,"Image Uploaded To database",Toast.LENGTH_LONG).show();
                                                        downloadUrl=uri.toString();
                                                    }
                                                    else
                                                    {loadingBar.dismiss();
                                                        Toast.makeText(SettingsActivity.this,"Image Failed to Upload To database",Toast.LENGTH_LONG).show();

                                                    }
                                                }
                                            });

                                }
                            });

                        }
                        else
                        {
                            String error=task.getException().toString();
                            loadingBar.dismiss();
                            Toast.makeText(SettingsActivity.this,"Error: "+error,Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        }

    }

    private void InitializeFields() {
        UpdateAccountSettings=(Button)findViewById(R.id.update_settings_button);
        userName=(EditText)findViewById(R.id.set_user_name);
        userStatus=(EditText)findViewById(R.id.set_profile_status);
        userProfileImage=(CircleImageView)findViewById(R.id.set_profile_image);
    }
    private void UpdateSettings() {
        String setUserName=userName.getText().toString();
        String setUserStatus=userStatus.getText().toString();
        if(TextUtils.isEmpty(setUserName))
        {
            Toast.makeText(SettingsActivity.this,"Please Provide Name",Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(setUserStatus))
        {
            Toast.makeText(SettingsActivity.this,"Please Provide Status",Toast.LENGTH_LONG).show();
        }
        else
        {
            HashMap<String,Object> profileMap=new HashMap<>();
            profileMap.put("uid",currentUserID);
            profileMap.put("name",setUserName);
            profileMap.put("status",setUserStatus);
            rootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            SendUserToMainActivity();
                            Toast.makeText(SettingsActivity.this,"User Data Uploaded Successfully",Toast.LENGTH_LONG).show();

                        }
                        else
                        {
                            String error=task.getException().toString();
                            Toast.makeText(SettingsActivity.this,"Error:"+error,Toast.LENGTH_LONG).show();
                        }
                        }
                    });
        }

    }

    private void SendUserToMainActivity() {
        Intent mainIntent=new Intent(SettingsActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
    }

    private void RetrieveUserInfo() {
        rootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if((dataSnapshot.exists())&&(dataSnapshot.hasChild("name"))&&(dataSnapshot.hasChild("image")))
                {
                    String retrieveUserName=dataSnapshot.child("name").getValue().toString();
                    String retrieveUserStatus=dataSnapshot.child("status").getValue().toString();
                    String retrieveProfileImage=dataSnapshot.child("image").getValue().toString();
                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveUserStatus);
                    Picasso.get().load(retrieveProfileImage).into(userProfileImage);
                }
                else if((dataSnapshot.exists())&&(dataSnapshot.hasChild("name")))
                {  String retrieveUserName=dataSnapshot.child("name").getValue().toString();
                    String retrieveUserStatus=dataSnapshot.child("status").getValue().toString();
                    userName.setText(retrieveUserName);
                   userStatus.setText(retrieveUserStatus);
                    Picasso.get().load(R.drawable.default_image).into(userProfileImage);
                }
                else
                {
                    userName.setVisibility(View.VISIBLE);
                    Toast.makeText(SettingsActivity.this,"Please Update Your Profile Information",Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


}
