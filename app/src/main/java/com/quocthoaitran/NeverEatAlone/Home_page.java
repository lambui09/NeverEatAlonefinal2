package com.quocthoaitran.NeverEatAlone;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Home_page extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemSelectedListener {

    private static  String TAG = Home_page.class.getSimpleName();
    @BindView(R.id.progress_bar_user)
    ProgressBar mProgressBarForUsers;
    @BindView(R.id.recycler_view_user)
    RecyclerView mUsersRecyclerView;
    private String mCurrentUserUid;
    private List<String> mUsersKeyList;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mUserRefDatabase;
    private ChildEventListener mChildEventListener;
    private UsersChatAdapter mUsersChatAdapter;
    private TextView txtUserEmail, txtUserName;
    private ImageView img_nav_avatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String lang = sp.getString("language", "");
        if(lang.equals("")){
            lang = "vi";
        }
        setLanguage(lang);
        setContentView(R.layout.activity_home_page);

        startService(new Intent(this, FirebaseNotificationService.class));
        mUserRefDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        bindButterKnife();

        firebaseAuth= FirebaseAuth.getInstance();
        mUserRefDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        setUserRecyclerView();
        mUsersKeyList = new ArrayList<String>();
        setAuthListener();

        final FirebaseUser user= firebaseAuth.getCurrentUser();
        View view = navigationView.getHeaderView(0);
        txtUserEmail= (TextView) view.findViewById(R.id.txtUserEmail);
        txtUserName= (TextView) view.findViewById(R.id.txtUserName);
        img_nav_avatar= (ImageView) view.findViewById(R.id.img_nav_avatar);
        txtUserEmail.setText(user.getEmail());
        mUserRefDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = (String) dataSnapshot.child(user.getUid()).child("displayName").getValue();
                txtUserName.setText(name);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        String s = String.valueOf(user.getPhotoUrl());
        if(s == "null"){
            img_nav_avatar.setImageResource(R.drawable.profile);
        }else{
            Picasso.with(this).load(user.getPhotoUrl()).into(img_nav_avatar);
        }
        //Picasso.with(this).load(user.getPhotoUrl()).into(img_nav_avatar);
    }
    @SuppressWarnings("deprecation")
    private void setLanguage(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration configuration = getResources().getConfiguration();
        configuration.locale = locale;
        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());

        //save
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("language", lang);
        editor.commit();
    }

    private void setAuthListener() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                hideProgressBarForUsers();
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    setUserData(user);
                    queryAllUsers();
                }else{
                    gotoLogin();
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        showProgressBarForUser();
        firebaseAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        clearCurrentUsers();
        if(mChildEventListener != null){
            mUserRefDatabase.removeEventListener(mChildEventListener);
        }
        if(mAuthListener != null){
            firebaseAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void clearCurrentUsers() {
        mUsersChatAdapter.clear();
        mUsersKeyList.clear();
    }

    private void showProgressBarForUser() {
        mProgressBarForUsers.setVisibility(View.VISIBLE);
    }

    private void gotoLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void queryAllUsers() {
        mChildEventListener = getChildEventListener();
        mUserRefDatabase.limitToFirst(50).addChildEventListener(mChildEventListener);
    }

    private ChildEventListener getChildEventListener() {
        return  new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.exists()){
                    String userUid = dataSnapshot.getKey();
                    if(dataSnapshot.getKey().equals(mCurrentUserUid)){
                        User currentUser = dataSnapshot.getValue(User.class);
                        mUsersChatAdapter.setCurrentUserInfo(userUid, currentUser.getEmail(), currentUser.getCreatedAt());
                    }
                    else{
                        User recipient = dataSnapshot.getValue(User.class);
                        recipient.setRecipientId(userUid);
                        mUsersKeyList.add(userUid);
                        mUsersChatAdapter.refill(recipient);
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.exists()){
                    String userUid = dataSnapshot.getKey();
                    if(!userUid.equals(mCurrentUserUid)){
                        User user = dataSnapshot.getValue(User.class);
                        int index = mUsersKeyList.indexOf(userUid);
                        if(index > -1){
                            mUsersChatAdapter.changeUser(index, user);
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void setUserData(FirebaseUser user) {
        mCurrentUserUid = user.getUid();
    }

    private void hideProgressBarForUsers() {
        if(mProgressBarForUsers.getVisibility() == View.VISIBLE){
            mProgressBarForUsers.setVisibility(View.GONE);
        }
    }

    private void setUserRecyclerView() {
        mUsersChatAdapter = new UsersChatAdapter(this, new ArrayList<User>());
        mUsersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mUsersRecyclerView.setHasFixedSize(true);
        mUsersRecyclerView.setAdapter(mUsersChatAdapter);
    }

    private void bindButterKnife() {
            ButterKnife.bind(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_notification) {

        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));

        } else if (id == R.id.nav_language) {
//            AlertDialog.Builder alerDialogBuilder = new AlertDialog.Builder(this);
//             alerDialogBuilder.setMessage(R.string.language);
//             alerDialogBuilder.setPositiveButton("VietNam", new DialogInterface.OnClickListener() {
//                 @Override
//                 public void onClick(DialogInterface dialog, int which) {
//                    ganNgonNgu("vi");
//                 }
//             });
//             alerDialogBuilder.setNegativeButton("English", new DialogInterface.OnClickListener() {
//                 @Override
//                 public void onClick(DialogInterface dialog, int which) {
//                     ganNgonNgu("en");
//                 }
//             });
//             AlertDialog alertDialog = alerDialogBuilder.create();
//             alertDialog.show();
            String[] langueges = getResources().getStringArray(R.array.languages);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.language)
                    .setItems(langueges, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String lang = "";
                            switch (which){
                                case 0:
                                    lang = "en";
                                    break;
                                case 1:
                                    lang = "vi";
                                    break;
                            }
                            setLanguage(lang);
                            setContentView(R.layout.activity_home_page);
                            Intent intent = new Intent(Home_page.this, Home_page.class);
                            startActivity(intent);
                        }
                    })
                    .create()
                    .show();
        } else if (id == R.id.nav_logout) {
             AlertDialog.Builder alerDialigBuilder = new AlertDialog.Builder(this);
             alerDialigBuilder.setMessage(R.string.do_Login);
             alerDialigBuilder.setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     xuLyDangXuat();
                 }
             });
             alerDialigBuilder.setNegativeButton(R.string.NO, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {

                 }
             });
             AlertDialog alertDialog = alerDialigBuilder.create();
             alertDialog.show();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void ganNgonNgu(String language) {
        Locale locale = new Locale(language);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
        Intent intent = new Intent(Home_page.this, Home_page.class);
        startActivity(intent);
    }

    private void xuLyDangXuat() {
        setUserOffline();
        firebaseAuth.signOut();
        finish();
        startActivity(new Intent(this, LoginActivity.class));
    }

    private void setUserOffline() {
        if(firebaseAuth.getCurrentUser() != null){
            String userUid = firebaseAuth.getCurrentUser().getUid();
            mUserRefDatabase.child(userUid).child("connection").setValue(UsersChatAdapter.OFFLINE);
        }
    }
}
