package com.example.user.heartbeatapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Button mEmail = (Button) findViewById(R.id.btn_send);



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","s910316910316@gmail.com",null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT,"屏東大學 脈波量測APP");
                emailIntent.putExtra(Intent.EXTRA_TEXT,"請給予您的寶貴建議:");
                startActivity(Intent.createChooser(emailIntent,"請選擇email..."));
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /*mEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","s910316910316@gmail.com",null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT,"屏東大學 心律感測APP");
                emailIntent.putExtra(Intent.EXTRA_TEXT,"請給予您的寶貴建議:");
                startActivity(Intent.createChooser(emailIntent,"請選擇email..."));
            }
        });*/
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
        getMenuInflater().inflate(R.menu.main, menu);
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
            Intent it = new Intent(this, SettingActivity.class);
            startActivity(it);
            return true;
        }

        if (id == R.id.stepcount) {
            Intent it = new Intent(this, StepCountActivity.class);
            startActivity(it);
            return true;
        }

        if (id == R.id.memorandum_book) {
            Intent it = new Intent(this, MemorandumActivity.class);
            startActivity(it);
            return true;
        }

        if (id == R.id.bmi) {
            Intent it = new Intent(this, BMIActivity.class);
            startActivity(it);
            return true;
        }

        if (id == R.id.exit) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("離開此程式")
                    .setMessage("你確定要離開？")
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton("否", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        }


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        //here is the main place where we need to work on.
        int id = item.getItemId();

            FragmentManager fragmentManager = getSupportFragmentManager();

            if (id == R.id.nav_home) {
                Intent h = new Intent(MainActivity.this, MainActivity.class);
                startActivity(h);
                Toast.makeText(MainActivity.this, "主頁面", Toast.LENGTH_SHORT).show();
                // Handle the camera action
            } else if (id == R.id.nav_explain) {
                fragmentManager.beginTransaction().replace(R.id.content_main,new ExplainFragment()).commit();

            } else if (id == R.id.nav_suggest) {
                fragmentManager.beginTransaction().replace(R.id.content_main,new SuggestFragment()).commit();

            } else if (id == R.id.nav_information) {
                fragmentManager.beginTransaction().replace(R.id.content_main,new InformationFragment()).commit();

            } else if (id == R.id.nav_about) {
                fragmentManager.beginTransaction().replace(R.id.content_main,new AboutFragment()).commit();

            }
            /*
            //側拉式選單用Activity實作不是用Fragment
            switch (id) {

            case R.id.nav_home:
                Intent h = new Intent(MainActivity.this, MainActivity.class);
                startActivity(h);
                break;
            case R.id.nav_explain:
                Intent i = new Intent(MainActivity.this, Explain.class);
                startActivity(i);
                break;
            case R.id.nav_suggest:
                Intent g = new Intent(MainActivity.this, Suggest.class);
                startActivity(g);
                break;
            case R.id.nav_information:
                Intent s = new Intent(MainActivity.this, Information.class);
                startActivity(s);
                break;
            case R.id.nav_about:
                Intent t = new Intent(MainActivity.this, About.class);
                startActivity(t);
                break;
            // this is done, now let us go and intialise the home page.
            // after this lets start copying the above.
            // FOLLOW MEEEEE>>>
            }
            */

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("確認視窗")
                    .setMessage("確定要結束應用程式？")
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton("否", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        }
        return true;
    }



}
