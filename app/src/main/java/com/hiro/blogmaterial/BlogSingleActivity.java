package com.hiro.blogmaterial;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class BlogSingleActivity extends AppCompatActivity {

    private String mPost_key = null;

    private ImageView singleImg;
    private TextView singleTitle, singleContent;

    private DatabaseReference mDatabase;

    private Button btnRemove;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_single);

        singleImg = (ImageView) findViewById(R.id.singleBlogImg);
        singleTitle = (TextView) findViewById(R.id.singleBlogTitle);
        singleContent = (TextView) findViewById(R.id.singleBlogContent);
        btnRemove = (Button) findViewById(R.id.postRemove);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");

        mAuth = FirebaseAuth.getInstance();

        mPost_key = getIntent().getExtras().getString("blog_id");

        mDatabase.child(mPost_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String post_title = (String) dataSnapshot.child("title").getValue();
                String post_desc = (String) dataSnapshot.child("desc").getValue();
                String post_img = (String) dataSnapshot.child("image").getValue();
                String post_uid = (String) dataSnapshot.child("uid").getValue();

                singleTitle.setText(post_title);
                singleContent.setText(post_desc);

                Picasso.with(BlogSingleActivity.this).load(post_img).into(singleImg);

                if (mAuth.getCurrentUser().getUid().equals(post_uid)) {
                    btnRemove.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.child(mPost_key).removeValue();

                Intent mainIntent = new Intent(BlogSingleActivity.this, MainActivity.class);
                startActivity(mainIntent);
            }
        });
    }
}
