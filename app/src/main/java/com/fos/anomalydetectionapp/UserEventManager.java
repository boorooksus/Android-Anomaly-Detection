package com.fos.anomalydetectionapp;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.PowerManager;

import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class UserEventManager extends AppCompatActivity {

    Activity activity;
    WhitelistManager whitelistManager;
    private static LocalDateTime lastTouchTime = LocalDateTime.now();  // 터치 이벤트 저장

    public UserEventManager(Activity activity, WhitelistManager whitelistManager) {
        this.activity = activity;
        this.whitelistManager = whitelistManager;
    }

    // 터치 이벤트 추가
    public void addTouchEvent(){
        lastTouchTime = LocalDateTime.now();
    }

    public int getRisk(Integer uid){
        if (checkWhitelist(uid)) return 0;
        else if (checkTouchEvent()) return 1;
        else if (checkAudioEvent()) return 2;
        else if (checkScreenOn()) return 3;
        return 4;
    }

    public boolean checkWhitelist(Integer uid){
        int index = whitelistManager.getIndex(uid);
        if (index == -1) return false;
        return whitelistManager.getAppDetail(index).getIsInWhitelist();
    }


    public boolean checkTouchEvent(){
        // 30초 이내에 터치 기록이 있었는지 확인

        int diff = (int)ChronoUnit.SECONDS.between(lastTouchTime, LocalDateTime.now());
        return diff >= 0 && diff < 20;
    }

    public boolean checkAudioEvent(){
        AudioManager manager = (AudioManager)activity.getSystemService(AUDIO_SERVICE);
        return manager.isMusicActive();
    }

    public boolean checkScreenOn(){
//        DisplayManager dm = (DisplayManager) activity.getSystemService(Context.DISPLAY_SERVICE);
//        boolean screenOn = false;
//        for (Display display : dm.getDisplays()) {
//            if (display.getState() != Display.STATE_OFF) {
//                screenOn = true;
//            }
//        }
//        return screenOn;

        PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
        return pm.isInteractive();

    }

}
