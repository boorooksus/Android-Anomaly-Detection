package com.fos.anomalydetectionapp;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.PowerManager;
import androidx.appcompat.app.AppCompatActivity;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

// 유저 이벤트 감지 클래스
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

    // 위험도 측정
    public int getRisk(Integer uid){
        if (checkWhitelist(uid)) return 0;
        else if (checkTouchEvent()) return 1;
        else if (checkAudioEvent()) return 2;
        else if (checkScreenOn()) return 3;
        else return 4;
    }

    // 앱의 화이트리스트 등록 여부 체크 함수
    public boolean checkWhitelist(Integer uid){
        int index = whitelistManager.getIndex(uid);
        if (index == -1) return false;
        return whitelistManager.getAppDetail(index).getIsInWhitelist();
    }

    // 유저 터치 이벤트 체크 함수
    public boolean checkTouchEvent(){
        // 20초 이내에 터치 기록이 있었는지 확인
        int diff = (int)ChronoUnit.SECONDS.between(lastTouchTime, LocalDateTime.now());
        return diff >= 0 && diff < 20;
    }

    // 오디오 작동 여부 체크 함수
    public boolean checkAudioEvent(){
        AudioManager manager = (AudioManager)activity.getSystemService(AUDIO_SERVICE);
        return manager.isMusicActive();
    }

    // 스크린 켜짐 여부 체크 함수
    public boolean checkScreenOn(){
        PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
        return pm.isInteractive();

    }
}
