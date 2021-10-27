package com.fos.anomalydetectionapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

public class WhitelistActivity extends AppCompatActivity {

    Toolbar toolbar;
    ActionBar actionBar;
    ListView listViewWhitelist;
    WhitelistAdapter whitelistAdapter;
    WhitelistManager whitelistManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whitelist);

        // actionbar setting
        toolbar = findViewById(R.id.Whitelisttoolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

//        AppsManager appsManager = new AppsManager(WhitelistActivity.this);
        whitelistManager = new WhitelistManager();

//        appsManager.setArgs(WhitelistActivity.this);
//        appsManager.initializeApps();

        listViewWhitelist = findViewById(R.id.listViewWhitelist);
        whitelistAdapter = new WhitelistAdapter(WhitelistActivity.this, whitelistManager);

        listViewWhitelist.setAdapter(whitelistAdapter);

    }

//    public void backHandler(View view){
//
//        finish();
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        whitelistManager.saveWhiteSet();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
                whitelistManager.saveWhiteSet();
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}