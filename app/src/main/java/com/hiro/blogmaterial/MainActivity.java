package com.hiro.blogmaterial;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DatabaseReference databaseReference;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private DatabaseReference mDatabaseUser;

    private DatabaseReference mDatabaseProfile;

    private boolean mProcessLike = false;

    private DatabaseReference mDatabaseLike;

    private Query mQueryUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.blog_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        mAuth = FirebaseAuth.getInstance();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Blog");
        databaseReference.keepSynced(true);

        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseLike.keepSynced(true);

        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUser.keepSynced(true);

        mDatabaseProfile = FirebaseDatabase.getInstance().getReference().child("Blog");

        String profileUserId = mAuth.getCurrentUser().getUid();

        mQueryUser = mDatabaseProfile.orderByChild("uid").equalTo(profileUserId);

        mDatabaseProfile.keepSynced(true);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };
        checkUserExist();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(authStateListener);

        FirebaseRecyclerAdapter<Blog, BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(
                Blog.class,
                R.layout.blog_row,
                BlogViewHolder.class,
                mQueryUser) {

            @Override
            protected void populateViewHolder(BlogViewHolder viewHolder, Blog model, int position) {

                final String post_key = getRef(position).getKey();

                viewHolder.setTitle(model.getTitle());
                viewHolder.setDesc(model.getDesc());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setUsername(model.getUsername());
                viewHolder.setmLike(post_key);

                viewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent singleIntent = new Intent(MainActivity.this, BlogSingleActivity.class);
                        singleIntent.putExtra("blog_id", post_key);
                        startActivity(singleIntent);
                    }
                });

                viewHolder.mLike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mProcessLike = true;

                        mDatabaseLike.addValueEventListener(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (mProcessLike) {

                                        if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                                            mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                            mProcessLike = false;
                                        } else {
                                            mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue("RandomValue");
                                            mProcessLike = false;
                                        }
                                    }
                                }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }
        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    private void checkUserExist() {

        if (mAuth.getCurrentUser() != null) {

            final String user_id = mAuth.getCurrentUser().getUid();

            mDatabaseUser.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(user_id)) {
                        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {

        View view;

        ImageButton mLike;

        DatabaseReference mDatabaseLike;

        FirebaseAuth mAuth;

        public BlogViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            mLike = (ImageButton) view.findViewById(R.id.btnLike);
            mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
            mAuth = FirebaseAuth.getInstance();
            mDatabaseLike.keepSynced(true);
        }

        public void setmLike(final String post_key) {
            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if(dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                        mLike.setImageResource(R.drawable.like_red);
                    } else {
                        mLike.setImageResource(R.drawable.like_grey);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setTitle(String title) {

            TextView post_title = (TextView) view.findViewById(R.id.postTitle);
            post_title.setText(title);
        }

        public void setDesc(String desc) {
            TextView post_desc = (TextView) view.findViewById(R.id.postDesc);
            post_desc.setText(desc);
        }

        public void setUsername(String username) {
            TextView post_username = (TextView) view.findViewById(R.id.author);
            post_username.setText(username);
        }

        public void setImage(Context ctx, String image) {
            ImageView post_img = (ImageView) view.findViewById(R.id.postImage);
            Picasso.with(ctx).load(image).into(post_img);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_add) {
            startActivity(new Intent(MainActivity.this, PostActivity.class));
        }

        if (item.getItemId() == R.id.action_logout) {
            logout();
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mAuth.signOut();
    }
}
