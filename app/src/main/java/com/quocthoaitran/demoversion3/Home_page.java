package com.quocthoaitran.demoversion3;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.Locale;

public class Home_page extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth firebaseAuth;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TextView txtUserEmail, txtUserName;
    private ImageView img_nav_avatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);




        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        tabLayout= (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.mesage));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.search));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

         viewPager= (ViewPager) findViewById(R.id.view_pager);
        final  PageAdapter adapter= new PageAdapter(getSupportFragmentManager(),
                tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        firebaseAuth= FirebaseAuth.getInstance();
        FirebaseUser user= firebaseAuth.getCurrentUser();

        View view = navigationView.getHeaderView(0);
        txtUserEmail= (TextView) view.findViewById(R.id.txtUserEmail);
        txtUserName= (TextView) view.findViewById(R.id.txtUserName);
        img_nav_avatar= (ImageView) view.findViewById(R.id.img_nav_avatar);
        txtUserEmail.setText(user.getEmail());
        txtUserName.setText(user.getDisplayName());
        Picasso.with(this).load(user.getPhotoUrl()).into(img_nav_avatar);
        String s = R.string.welcom +"";
        Toast.makeText(this, R.string.welcom + user.getEmail(),Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

         if (id == R.id.nav_notification) {

        } else if (id == R.id.nav_profile) {
            finish();
            startActivity(new Intent(this, ProfileActivity.class));

        } else if (id == R.id.nav_language) {
            AlertDialog.Builder alerDialogBuilder = new AlertDialog.Builder(this);
             alerDialogBuilder.setMessage(R.string.language);
             alerDialogBuilder.setPositiveButton("VietNam", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                    ganNgonNgu("vi");
                 }
             });
             alerDialogBuilder.setNegativeButton("English", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     ganNgonNgu("en");
                 }
             });
             AlertDialog alertDialog = alerDialogBuilder.create();
             alertDialog.show();

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
        firebaseAuth.signOut();
        finish();
        startActivity(new Intent(this, LoginActivity.class));
    }
}
