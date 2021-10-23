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
                finish();

            } else {

                startService(new Intent(activity, OverlayService.class));
            }
        }
    }
}
