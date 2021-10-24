package com.fos.anomalydetectionapp;

import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
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

public class UserEventManager extends AppCompatActivity {

    Activity activity;
    private static LocalDateTime lastTouchTime = LocalDateTime.now();  // 터치 이벤트 저장

    public UserEventManager(Activity activity) {
        this.activity = activity;
    }

    // 터치 이벤트 추가
    public void addTouchEvent(){
        lastTouchTime = LocalDateTime.now();
    }

    public int getRisk(){
        if (checkTouchEvent()) return 1;
        else if (checkAudioEvent()) return 2;

        return 4;
    }

    public boolean checkTouchEvent(){
        // 30초 이내에 터치 기록이 있었는지 확인

        int diff = (int)ChronoUnit.SECONDS.between(lastTouchTime, LocalDateTime.now());
        return diff < 30;
    }

    public boolean checkAudioEvent(){
        AudioManager manager = (AudioManager)activity.getSystemService(AUDIO_SERVICE);
        return manager.isMusicActive();
    }

}
