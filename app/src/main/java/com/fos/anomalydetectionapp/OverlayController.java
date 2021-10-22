package com.fos.anomalydetectionapp;

import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class OverlayController extends AppCompatActivity {

    Activity activity;
    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 1;

    public OverlayController(Activity activity) {
        this.activity = activity;
    }

    public boolean checkPermission() {
        if (!Settings.canDrawOverlays(activity)) {
            // 권한 설정이 안된 경우

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    // 알림 생성
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setMessage("다른 앱 위에 표시 권한은 설정해주세요");
                    builder.setPositiveButton(
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
                    alertDialog.show();
                }
            });

            return false;
        } else {

            return true;
        }
    }

//    public void startOverlay(){
//        // 오버레이 생성
//
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//
////                Log.v("OverlayController", "before overlay");
//                startService(new Intent(activity, OverlayService.class));
//
//            }
//
//        });
//    }

//    public void stopOverlay(){
//        // 오버레이 제거
//        stopService(new Intent(activity, OverlayService.class));
//    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                // TODO 동의를 얻지 못했을 경우의 처리

            } else {

//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        startService(new Intent(activity, OverlayService.class));
//
//                    }
//
//                });
                startService(new Intent(activity, OverlayService.class));
            }
        }
    }
}
