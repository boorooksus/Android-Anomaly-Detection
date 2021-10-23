package com.fos.anomalydetectionapp;

import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    Button buttonStatus;  // 목록 새로고침 버튼
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch switchTracking;  // 모니터링 온오프 스위치
    ListView listViewHistory;  // 트래픽 히스토리 목록 리스트뷰
    AdapterHistory adapterHistory;  // 리스트뷰 어댑터
    String colorRunning = "#41A541";  // 러닝 중일 때 버튼 색상(녹색)
    String colorStopped = "#808080";  // 중단 됐을 때 버튼 색상(회색)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TrafficMonitorService trafficMonitorService = new TrafficMonitorService();
        trafficMonitorService.setArgs(MainActivity.this);

        // 뷰 id로 불러오기
        buttonStatus = findViewById(R.id.buttonStatus);
        switchTracking = findViewById(R.id.switchTracking);
        listViewHistory = findViewById(R.id.listViewHistory);
        adapterHistory = trafficMonitorService.getAdapterHistory();

        // 마지막 스위치 상태 가져오기
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean isRunning = preferences.getBoolean("isRunning", false);  // 스위치가 켜졌는지 여부

        // 리스트뷰, 스위치, 버튼 세팅
        listViewHistory.setAdapter(adapterHistory);
        switchTracking.setChecked(isRunning);
        buttonStatus.setBackgroundColor(Color.parseColor(isRunning ? colorRunning:colorStopped));

        // 트래픽 모니터링 클래스
//        final TrafficMonitor trafficMonitor = new TrafficMonitor(MainActivity.this, adapterHistory);
//        final OverlayController overlayController = new OverlayController(MainActivity.this);

        buttonStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                EventManagement eventManagement = new EventManagement();
//
//                AudioManager manager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
//
//                if (eventManagement.checkAudioEvent(MainActivity.this)){
//
//                    Log.v("Main - Audio", "=-=======Audio is Playing");
//                } else{
//                    Log.v("Main - Audio", "=-=======Audio is NOT Playing");
//
//                }
            }
        });

        // 모니터링 온오프 스위치 이벤트 리스터
        switchTracking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
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

                        startService(new Intent(MainActivity.this, TrafficMonitorService.class));

                        // 오버레이 생성
//                        overlayController.startOverlay();
                        startService(new Intent(MainActivity.this, OverlayService.class));

//                        trafficMonitor.startTracking();

                        buttonStatus.setBackgroundColor(Color.parseColor(colorRunning));
                        buttonStatus.setText("모니터링 작동 중");

                    } else{
                        // 앱 사용 기록 엑세스 권한 없는 경우 스위치 다시 끄기
                        switchTracking.setChecked(false);
                    }
                }
                else{
                    // 스위치 끄면 모니터링 중지
                    buttonStatus.setBackgroundColor(Color.parseColor(colorStopped));
                    buttonStatus.setText("모니터링 정지");
                    SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
                    editor.putBoolean("isRunning", false); // 스위치 상태 변수 세팅
                    editor.apply(); // 스위치 상태 변수 저장

                    // 오버레이 제거
//                    overlayController.stopOverlay();
                    stopService(new Intent(MainActivity.this, OverlayService.class));
                    stopService(new Intent(MainActivity.this, TrafficMonitorService.class));
                }
            }
        });
    }


}
