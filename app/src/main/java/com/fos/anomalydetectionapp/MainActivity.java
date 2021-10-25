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
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    ListView listViewHistory;  // 트래픽 히스토리 목록 리스트뷰
    HistoryAdapter historyAdapter;  // 리스트뷰 어댑터
    String colorRunning = "#41A541";  // 러닝 중일 때 버튼 색상(녹색)
    String colorStopped = "#FFFFFF";  // 중단 됐을 때 버튼 색상(회색)
    Toolbar toolbar;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ServiceManager serviceManager = new ServiceManager();
        serviceManager.setArgs(MainActivity.this);

        // 뷰 id로 불러오기
        buttonStatus = findViewById(R.id.buttonStatus);
        listViewHistory = findViewById(R.id.listViewHistory);
        historyAdapter = serviceManager.getAdapterHistory();

        // actionbar setting
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
//        actionBar.setDisplayHomeAsUpEnabled(true);

        // 마지막 스위치 상태 가져오기
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean isRunning = preferences.getBoolean("isRunning", false);  // 스위치가 켜졌는지 여부

        // 리스트뷰, 스위치, 버튼 세팅
        listViewHistory.setAdapter(historyAdapter);
        buttonStatus.setBackgroundColor(Color.parseColor(isRunning ? colorRunning:colorStopped));
        buttonStatus.setText(isRunning? "Monitoring...":"Start");

        // 트래픽 모니터링 클래스
//        final TrafficMonitor trafficMonitor = new TrafficMonitor(MainActivity.this, adapterHistory);
//        final OverlayController overlayController = new OverlayController(MainActivity.this);

        if(isRunning){
            startService(new Intent(MainActivity.this, ServiceManager.class));
        }

        buttonStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!preferences.getBoolean("isRunning", false)){
                    // 스위치 켰을 때

                    //  권한 확인
                    PermissionChecker permissionChecker = new PermissionChecker(MainActivity.this);
                    if (permissionChecker.checkAllPermissions()) {
                        // 권한 있는 경우

                        // 작동 여부 공유 변수 true로 변경
                        SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
                        editor.putBoolean("isRunning", true); // 스위치 상태 변수 세팅
                        editor.apply(); // 스위치 상태 변수 저장

                        // 모니터링 시작

                        startService(new Intent(MainActivity.this, ServiceManager.class));

                        // 오버레이 생성
//                        overlayController.startOverlay();
//                        startService(new Intent(MainActivity.this, OverlayService.class));
                        startForegroundService(new Intent(MainActivity.this, ServiceManager.class));

//                        trafficMonitor.startTracking();

                        buttonStatus.setBackgroundColor(Color.parseColor(colorRunning));
                        buttonStatus.setText("Monitoring...");

                    }
                }
                else{
                    // 스위치 끄면 모니터링 중지
                    buttonStatus.setBackgroundColor(Color.parseColor(colorStopped));
                    buttonStatus.setText("Start");
                    SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
                    editor.putBoolean("isRunning", false); // 스위치 상태 변수 세팅
                    editor.apply(); // 스위치 상태 변수 저장

                    // 오버레이 제거
//                    overlayController.stopOverlay();
//                    stopService(new Intent(MainActivity.this, OverlayService.class));
                    startService(new Intent(MainActivity.this, ServiceManager.class));
                    stopService(new Intent(MainActivity.this, ServiceManager.class));
                }
            }
        });



//        listViewHistory.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                adapterHistory.notifyDataSetChanged();
//
//            }
//        });
    }
}
