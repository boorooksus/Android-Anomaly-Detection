package com.fos.anomalydetectionapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class WhitelistActivity extends AppCompatActivity {

    Toolbar toolbar;
    ActionBar actionBar;
    ListView listViewWhitelist;
    WhitelistAdapter whitelistAdapter;
    AppsManager appsManager;


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
        appsManager = new AppsManager();

//        appsManager.setArgs(WhitelistActivity.this);
//        appsManager.initializeApps();

        listViewWhitelist = findViewById(R.id.listViewWhitelist);
        whitelistAdapter = new WhitelistAdapter(WhitelistActivity.this, appsManager);

        listViewWhitelist.setAdapter(whitelistAdapter);

//        Log.v("Apps info test=====", appsManager.getLength() + "");
//        Log.v("Apps info test=====", appsManager.getAppDetail(0).getAppLabel() + "");


    }

    public void backHandler(View view){
        appsManager.updateWhitelist();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
                appsManager.updateWhitelist();
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}