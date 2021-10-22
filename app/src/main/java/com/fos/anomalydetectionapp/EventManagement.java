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

public class EventManagement extends AppCompatActivity {

    private static final Stack<LocalDateTime> touchEvents = new Stack<>();  // 터치 이벤트 저장

    // 터치 이벤트 추가
    public void addTouchEvent(){
        touchEvents.add(LocalDateTime.now());
    }

    public boolean checkTouchEvent(){
        LocalDateTime cur = LocalDateTime.now();
        LocalDateTime latest = touchEvents.peek();

        int diff = (int)ChronoUnit.SECONDS.between(latest, cur);

        // 이벤트 탐색 범위 5분으로 설정
        return diff < 300;

    }


}
