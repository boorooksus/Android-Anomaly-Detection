package com.fos.anomalydetectionapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

// 화이트리스트 관리 액티비티
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

        whitelistManager = new WhitelistManager();
        listViewWhitelist = findViewById(R.id.listViewWhitelist);
        whitelistAdapter = new WhitelistAdapter(WhitelistActivity.this, whitelistManager);
        listViewWhitelist.setAdapter(whitelistAdapter);

    }

    // 디바이스 back키 누른 경우 동작 설정
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // 화이트 리스트 세팅 업데이트
        whitelistManager.saveWhiteSet();
    }

    //toolbar의 back키 눌렀을 때 동작
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // 화이트 리스트 세팅 업데이트
            whitelistManager.saveWhiteSet();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}