package com.hiro.blogmaterial;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class PostActivity extends AppCompatActivity {

    private ImageButton imgSelectImg;
    private static final int GALLERY_REQUEST = 1;
    private EditText btnTitle, btnDesc;
    private Button btnSubmit;

    private Uri imageUri = null;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;
    private DatabaseReference databaseReference;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    private DatabaseReference mDatabaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAuth = FirebaseAuth.getInstance();

        mCurrentUser = mAuth.getCurrentUser();

        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());

        btnTitle = (EditText) findViewById(R.id.title);
        btnDesc = (EditText) findViewById(R.id.desc);
        imgSelectImg = (ImageButton) findViewById(R.id.imgBtn);
        btnSubmit = (Button) findViewById(R.id.submitBtn);

        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Blog");

        progressDialog = new ProgressDialog(this);

        imgSelectImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPosting();
            }
        });
    }

    private void startPosting() {

        progressDialog.setMessage("Posting to Blog...");


        final String title_val = btnTitle.getText().toString().trim();
        final String desc_val = btnDesc.getText().toString().trim();
        if(!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(desc_val) && imageUri != null)
        {
            progressDialog.show();

            StorageReference filepath = storageReference.child("Blog_images").child(imageUri.getLastPathSegment());

            filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    final DatabaseReference newPost = databaseReference.push();

                    mDatabaseUser.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            newPost.child("title").setValue(title_val);
                            newPost.child("desc").setValue(desc_val);
                            newPost.child("image").setValue(downloadUrl.toString());
                            newPost.child("uid").setValue(mCurrentUser.getUid());
                            newPost.child("username").setValue(dataSnapshot.child("name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {
                                        startActivity(new Intent(PostActivity.this, MainActivity.class));
                                    }

                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    progressDialog.dismiss();
                }
            });
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            imageUri = data.getData();
            imgSelectImg.setImageURI(imageUri);
        }

    }
}
