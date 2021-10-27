package com.fos.anomalydetectionapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.app.ActionBar;


public class MainActivity extends AppCompatActivity {

    Button buttonStatus;  // 목록 새로고침 버튼
    Button buttonWhitelist;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    ListView listViewHistory;  // 트래픽 히스토리 목록 리스트뷰
    HistoryAdapter historyAdapter;  // 리스트뷰 어댑터
    String colorRunning = "#41A541";  // 러닝 중일 때 버튼 색상(녹색)
    String colorStopped = "#FFFFFF";  // 중단 됐을 때 버튼 색상(회색)
    Toolbar toolbar;
    ActionBar actionBar;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        PermissionChecker permissionChecker = new PermissionChecker(MainActivity.this);

        ServiceManager serviceManager = new ServiceManager();
        serviceManager.setArgs(MainActivity.this);

        AppsManager appsManager = new AppsManager();

//        if(permissionChecker.checkAllPermissions()) {
//            appsManager.setArgs(MainActivity.this);
//            appsManager.initializeApps();
//        }

        // 뷰 id로 불러오기
        buttonStatus = findViewById(R.id.buttonStatus);
        buttonWhitelist = findViewById(R.id.buttonWhiteList);
        listViewHistory = findViewById(R.id.listViewHistory);
        historyAdapter = serviceManager.getHistoryAdapter();

        // actionbar setting
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        // 마지막 스위치 상태 가져오기
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean isRunning = preferences.getBoolean("isRunning", false);  // 스위치가 켜졌는지 여부

        // 리스트뷰, 스위치, 버튼 세팅
        listViewHistory.setAdapter(historyAdapter);
        buttonStatus.setBackgroundColor(Color.parseColor(isRunning ? colorRunning:colorStopped));
        buttonStatus.setText(isRunning? "Monitoring":"Start");

        if(isRunning){
            appsManager.initializeApps(MainActivity.this, context);
            startForegroundService(new Intent(MainActivity.this, ServiceManager.class));
        }

        buttonStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){

                if (!preferences.getBoolean("isRunning", false)) {
                    // 스위치 켰을 때
//                      권한 확인
                    if (permissionChecker.checkAllPermissions()) {
                        // 권한 있는 경우

                        appsManager.initializeApps(MainActivity.this, context);

//                        buttonWhitelist.setEnabled(true);

                        // 작동 여부 공유 변수 true로 변경
                        SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
                        editor.putBoolean("isRunning", true); // 스위치 상태 변수 세팅
                        editor.apply(); // 스위치 상태 변수 저장

                        // 모니터링 시작
                        startForegroundService(new Intent(MainActivity.this, ServiceManager.class));

                        buttonStatus.setBackgroundColor(Color.parseColor(colorRunning));
                        buttonStatus.setText("MONITORING");
                    }
                } else {
                    // 스위치 끄면 모니터링 중지
                    buttonStatus.setBackgroundColor(Color.parseColor(colorStopped));
                    buttonStatus.setText("Start");
                    SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
                    editor.putBoolean("isRunning", false); // 스위치 상태 변수 세팅
                    editor.apply(); // 스위치 상태 변수 저장

                    // 오버레이 제거
//                    startService(new Intent(MainActivity.this, ServiceManager.class));
                    stopService(new Intent(MainActivity.this, ServiceManager.class));
                }
            }
        });

        buttonWhitelist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(permissionChecker.checkAllPermissions()) {

                    appsManager.initializeApps(MainActivity.this, context);

                    Intent intent = new Intent(getApplicationContext(), WhitelistActivity.class);
                    startActivity(intent);
                }
            }
        });
    }
}
