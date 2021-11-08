package com.fos.anomalydetectionapp;

import static java.lang.Thread.sleep;

import android.app.Activity;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.PowerManager;

import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

// 유저 이벤트 감지 클래스
public class UserEventManager extends AppCompatActivity {

    Activity activity;
    WhitelistManager whitelistManager;
    private static final HashMap<String, LocalDateTime> lastTouchTime = new HashMap<>();

    public UserEventManager(Activity activity, WhitelistManager whitelistManager) {
        this.activity = activity;
        this.whitelistManager = whitelistManager;
    }

    // 포어그라운드 앱 터치 이벤트 기록
    public void addTouchEvent(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 현재 시간, 터치 발생 포어그라운드 앱 업데이트
                LocalDateTime time = LocalDateTime.now();
                String processName = getForegroundApp();
                lastTouchTime.put(processName, time);
//                Log.e("Touch event: ", processName + time);
            }
        }).start();


    }

    // 포어그라운드 앱 패키지 네임 리턴 함수
    public String getForegroundApp(){

        // 앱 초기화 시간을 고려하여 1초 지연
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 현재 포어그라운드 앱 얻기
        UsageStatsManager usm = (UsageStatsManager) activity.getSystemService(Context.USAGE_STATS_SERVICE);
        String curApp = null;  // 포어그라운드 앱 패키지 네임
        long curTime = System.currentTimeMillis();
        List<UsageStats> applist = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, curTime - 1000 * 1000, curTime);

        if (applist != null && applist.size() > 0) {
            SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
            for (UsageStats usageStats : applist) {
                mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
            }
            if (!mySortedMap.isEmpty()) {
                curApp = Objects.requireNonNull(mySortedMap.get(mySortedMap.lastKey())).getPackageName();
            }
        }

        return curApp;

    }

    // 위험도 평가
    public int accessRisk(Integer uid, String processName){
        // 화이트리스트 여부
        if (checkWhitelist(uid)) return 0;

//        // 스크린 온오프 여부
//        if (checkScreenOn()){
//            // 터치 이벤트 발생 여부
//            if(checkTouchEvent(processName)) return 1;
//            else return 2;
//        }
//        else return 3;

        if(checkTouchEvent(processName)) return 1;
        else{
            if (checkScreenOn()) return 2;
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
        // 앱의 마지막 터치 이벤트 발생 시각이 현재 시각 20초 내에 있으면 true

        LocalDateTime time = Optional.ofNullable(lastTouchTime.get(processName))
                .orElse(LocalDateTime.of(1, 1, 1, 1, 1));
        return (int)ChronoUnit.SECONDS.between(LocalDateTime.now(), time) < 20;
    }

    // 스크린 켜짐 여부 체크 함수
    public boolean checkScreenOn(){
        PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
        return pm.isInteractive();

    }
}
