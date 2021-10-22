package com.fos.anomalydetectionapp;

import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

public class EventChecker  extends AppCompatActivity {

    private static final Stack<LocalDateTime> touchEvents = new Stack<>();  // 터치 이벤트 저장

    // 터치 이벤트 추가
    public void addTouchEvent(){
        Log.v("EventChecker", "cur time: " + LocalDateTime.now().toString());
        touchEvents.add(LocalDateTime.now());
        Log.v("EventChecker", "add current time");
    }

    public void checkTouchEvent(){
        LocalDateTime cur = LocalDateTime.now();
        LocalDateTime latest = touchEvents.peek();

        ChronoUnit.SECONDS(latest, cur);
        Log.v("EventChecker", "========================sec: " + diff);

    }

//    public boolean checkTouch(){
//
//
//    }

}
