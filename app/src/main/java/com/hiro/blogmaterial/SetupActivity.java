package com.hiro.blogmaterial;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class SetupActivity extends AppCompatActivity {

    private ImageButton imgPickAva;
    private EditText nameDisplay;
    private Button btnApply;

    private Uri mImageUri = null;

    private static final int GALLERY_REQUEST = 1;

    private DatabaseReference mDatabaseUser;

    private FirebaseAuth mAuth;

    private StorageReference mStoregeImg;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        imgPickAva = (ImageButton) findViewById(R.id.pickImgAva);
        nameDisplay = (EditText) findViewById(R.id.nameDisplay);
        btnApply = (Button) findViewById(R.id.btnApply);

        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();

        mStoregeImg = FirebaseStorage.getInstance().getReference().child("Profile_images");

        mProgress = new ProgressDialog(this);

        imgPickAva.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickAva = new Intent();
                pickAva.setAction(Intent.ACTION_GET_CONTENT);
                pickAva.setType("image/*");
                startActivityForResult(pickAva, GALLERY_REQUEST);
            }
        });

        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSetupAccount();
            }
        });
    }

    @NonNull
    private void startSetupAccount() {

        final String name = nameDisplay.getText().toString().trim();
        final String user_id = mAuth.getCurrentUser().getUid();

        if (!TextUtils.isEmpty(name) && mImageUri != null) {

            mProgress.setMessage("Finishing Setup...");
            mProgress.show();

            StorageReference filepath = mStoregeImg.child(mImageUri.getLastPathSegment());

            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    String downloadUri = taskSnapshot.getDownloadUrl().toString();

                    mDatabaseUser.child(user_id).child("name").setValue(name);
                    mDatabaseUser.child(user_id).child("image").setValue(downloadUri);

                    mProgress.dismiss();

                    Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mImageUri = result.getUri();
                imgPickAva.setImageURI(mImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }

    }
}
