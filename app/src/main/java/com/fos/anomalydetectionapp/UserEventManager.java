package com.fos.anomalydetectionapp;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.PowerManager;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Optional;

// 유저 이벤트 감지 클래스
public class UserEventManager extends AppCompatActivity {

    Activity activity;
    WhitelistManager whitelistManager;
    private static final HashMap<String, LocalDateTime> lastTouchTime = new HashMap<>();

    public UserEventManager(Activity activity, WhitelistManager whitelistManager) {
        this.activity = activity;
        this.whitelistManager = whitelistManager;
    }

    // 터치 이벤트 추가
    public void addTouchEvent(String processName, LocalDateTime time){
        lastTouchTime.put(processName, time);
    }

    // 위험도 측정
    public int getRisk(Integer uid, String processName){
        if (checkWhitelist(uid)) return 0;

        if (checkScreenOn()){
            if(checkTouchEvent(processName)) return 1;
            else return 2;
        }
        else {
            if(checkTouchEvent(processName)) return 4;
            else return 3;
        }
    }

    // 앱의 화이트리스트 등록 여부 체크 함수
    public boolean checkWhitelist(Integer uid){
        int index = whitelistManager.getIndex(uid);
        if (index == -1) return false;
        return whitelistManager.getAppDetail(index).getIsInWhitelist();
    }

    // 유저 터치 이벤트 체크 함수
    public boolean checkTouchEvent(String processName){
        LocalDateTime time = Optional.ofNullable(lastTouchTime.get(processName))
                .orElse(LocalDateTime.of(1, 1, 1, 1, 1));
        return (int)ChronoUnit.SECONDS.between(LocalDateTime.now(), time) < 20;
    }

//    public boolean checkMicophone(){
//        MediaRecorder recorder = new MediaRecorder();
//        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
//        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
//        recorder.setOutputFile(new File(activity.getCacheDir(), "MediaUtil#micAvailTestFile").getAbsolutePath());
//        boolean available = true;
//        try {
//            recorder.prepare();
//            recorder.start();
//
//        }
//        catch (Exception exception) {
//            available = false;
//        }
//        recorder.release();
//        return available;
//    }

//    // 오디오 작동 여부 체크 함수
//    public boolean checkAudioEvent(){
//        AudioManager manager = (AudioManager)activity.getSystemService(AUDIO_SERVICE);
//        return manager.isMusicActive();
//    }

    // 스크린 켜짐 여부 체크 함수
    public boolean checkScreenOn(){
        PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
        return pm.isInteractive();

    }
}
