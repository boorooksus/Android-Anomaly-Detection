package com.fos.anomalydetectionapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;


// 앱의 권한 체크 클래스
public class PermissionChecker extends AppCompatActivity {

    @SuppressLint("StaticFieldLeak")
    private static Activity activity;

    // Constructor
    public PermissionChecker(Activity activity) {
        PermissionChecker.activity = activity;
    }

    // 앱에 필요한 모든 권한 체크 함수
    public boolean checkAllPermissions(){
        return checkIgnoreBatteryOptimization() && checkAccessPermission() && checkOverlayPermission();
    }

    // 절전 기능 제외 앱 설정 체크 함수
    @SuppressLint("BatteryLife")
    public boolean checkIgnoreBatteryOptimization(){

        PowerManager powerManager = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);

        if (!powerManager.isIgnoringBatteryOptimizations(activity.getPackageName())) {
            // 절전 기능 제외 앱이 아닌 경우

            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            openSettingPage("Disable Battery Optimization\nof this app", intent);

            return false;
        }

        return true;

    }

    // 다른 앱 위에 그리기 권한 체크 함수
    public boolean checkOverlayPermission() {
        if (!Settings.canDrawOverlays(activity)) {
            // 권한 설정이 안된 경우

            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:" + activity.getPackageName()));
            openSettingPage("Allow Manage Overlay permission", intent);

            return false;
        }

        return true;
    }

    // 스토리지 접근 권한 및 앱의 사용 기록 액세스 권한 체크 함수
    public boolean checkAccessPermission(){

        NetworkStatsManager networkStatsManager =
                (NetworkStatsManager) activity.getApplicationContext().
                        getSystemService(Context.NETWORK_STATS_SERVICE);

        try{
            // 아래 코드를 실행해 보고 에러가 없다면 권한이 존재
            // 에러 체크 외에 다른 목적은 없음
            NetworkStats networkStats =
                    networkStatsManager.queryDetailsForUid(
                            NetworkCapabilities.TRANSPORT_WIFI,
                            "",
                            System.currentTimeMillis() - 1000,
                            System.currentTimeMillis(),
                            0);
            networkStats.close();

            return true;

        } catch(Exception e){

            // 위에서 에러가 존재한다면 권한이 제한되어 있음
            // 유저를 설정 페이지로 보냄

            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            openSettingPage("Allow Usage Access permission", intent);

            return false;
        }
    }

    // 설정 페이지로 보내는 함수
    public void openSettingPage(String msg, Intent intent){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // 알림 생성
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage(msg);

                // 알림의 확인 버튼 기능 생성
                // 'Negative Button'이지만 확인 버튼으로 설정
                builder.setNegativeButton(
                        "OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                activity.startActivity(intent);
                            }
                        });

                AlertDialog alertDialog = builder.create();

                // 알림의 확인 버튼 색상 변경
                // 기본 테마 변경으로 인해 설정 안하면 확인 버튼이 흰색이라 안보임
                alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {
                        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#000010"));
                    }
                });
                alertDialog.show();
            }
        });
    }
}
