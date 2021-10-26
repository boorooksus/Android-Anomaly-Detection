package com.fos.anomalydetectionapp;

import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

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
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;

public class PermissionChecker extends AppCompatActivity {

    private final Activity activity;
    private final NetworkStatsManager networkStatsManager; // 어플 별 네트워크 사용 내역 얻을 때 사용


    public PermissionChecker(Activity activity) {
        this.activity = activity;

        networkStatsManager =
                (NetworkStatsManager) activity.getApplicationContext().
                        getSystemService(Context.NETWORK_STATS_SERVICE);;
    }

    public boolean checkAllPermissions(){
        return checkAccessPermission() && checkOverlayPermission();
    }

    public boolean checkDoNotDisturb(){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // 알림 생성
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage("절전 기능 예외 앱을 설정해주세요.");
                builder.setNegativeButton(
                        "확인",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {

                                activity.startActivity(new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS));

                            }
                        });

                AlertDialog alertDialog = builder.create();

                alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {
                        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#000010"));
                    }
                });

                alertDialog.show();
            }
        });

        return true;

    }

    public boolean checkOverlayPermission() {
        if (!Settings.canDrawOverlays(activity)) {
            // 권한 설정이 안된 경우

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    // 알림 생성
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setMessage("다른 앱 위에 표시 권한을 설정해주세요");
                    builder.setNegativeButton(
                            "확인",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    // 유저를 설정 페이지로 보냄
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:" + activity.getPackageName()));
                                    activity.startActivityForResult(intent, TYPE_APPLICATION_OVERLAY);

//                                    activity.startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                                    //finish();
                                }
                            });

                    AlertDialog alertDialog = builder.create();

                    alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface arg0) {
                            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#000010"));
                        }
                    });

                    alertDialog.show();
                }
            });

            return false;
        } else {

            return true;
        }
    }

    // 스토리지 접근 권한 및 앱의 사용 기록 액세스 권한 체크 함수
    // 권한 있는 경우 true, 없는 경우 유저를 설정 앱으로 보내고 false 리턴
    public boolean checkAccessPermission(){

        try{
            // 아래 코드를 실행해 보고 에러가 없다면 권한이 존재
            // 에러 체크 외에 다른 목적은 없음
            NetworkStats networkStats =
                    networkStatsManager.queryDetailsForUid(
                            NetworkCapabilities.TRANSPORT_WIFI,
                            "",
                            0,
                            System.currentTimeMillis(),
                            0);
            networkStats.close();

            return true;

        } catch(Exception e){

            // 위에서 에러가 존재한다면 권한이 제한되어 있음
            // 유저를 설정 페이지로 보냄
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    // 알림 생성
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setMessage("앱의 사용 기록 액세스를 허용해주세요");
                    builder.setNegativeButton(
                            "확인",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    // 유저를 설정 페이지로 보냄
//                                    Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS,
//                                            Uri.parse("package:" + activity.getPackageName()));
//                                    activity.startActivityForResult(intent, TYPE_APPLICATION_OVERLAY);
                                    activity.startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                                    //ACTION_MANAGE_OVERLAY_PERMISSION
                                    //finish();
                                }
                            });

                    AlertDialog alertDialog = builder.create();

                    alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface arg0) {
                            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#000010"));
                        }
                    });

                    alertDialog.show();
                }
            });
            return false;
        }
    }
}
