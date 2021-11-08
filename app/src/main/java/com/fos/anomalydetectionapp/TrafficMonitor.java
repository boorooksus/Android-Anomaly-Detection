package com.fos.anomalydetectionapp;

import android.app.Activity;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.net.NetworkCapabilities;
import android.os.RemoteException;
import android.util.Log;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

// 트래픽 모니터링 클래스
public class TrafficMonitor extends AppCompatActivity {

    private static Map<String, Long> lastUsage;  // 앱의 마지막 트래픽 저장
    private static boolean isInitialized;  // lastUsage 리스트 초기화 여부 저장
    TrafficHistory trafficHistory;  // 트래픽 히스토리 내역 리스트
    private final NetworkStatsManager networkStatsManager;  // 어플 별 네트워크 사용 내역 얻을 때 사용
    private final Activity activity;  // 메인 액티비티 context
    private static LogFileProcessor logFileProcessor;  // 로그 파일 쓰기 위한 객체
    ListView listViewHistory;  // 트래픽 내역 리스트뷰
    TrafficHistoryAdapter trafficHistoryAdapter;  // 트래픽 히스토리 리스트뷰 어댑터
    UserEventManager userEventManager;
    private static Timer timer;  // 모니터링 타이머
    WhitelistManager whitelistManager;
    private long lambda = 20000;  // 트래픽 탐지 시간 범위


    // Constructor
    public TrafficMonitor(Activity activity, TrafficHistoryAdapter trafficHistoryAdapter, TrafficHistory trafficHistory) {

        // 초기화
        lastUsage = new HashMap<>();
        isInitialized = false;
        this.trafficHistory = trafficHistory;
        this.activity = activity;
        this.trafficHistoryAdapter = trafficHistoryAdapter;
        logFileProcessor = new LogFileProcessor();
        whitelistManager = new WhitelistManager();
        userEventManager = new UserEventManager(activity, whitelistManager);
        listViewHistory = activity.findViewById(R.id.listViewHistory);
        networkStatsManager =
                (NetworkStatsManager) activity.getApplicationContext().
                        getSystemService(Context.NETWORK_STATS_SERVICE);
    }

    // 모니터링 시작 함수
    public void startMonitoring(){

        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                // 트래픽 탐지
                detectTraffic();
            }
        };


        new Thread(new Runnable() {
            @Override
            public void run() {

                // 타이머 주기를 20초로 설정하고 작동
                timer.schedule(timerTask, 0, lambda);

            }
        }).start();
    }

    // 모니터링 정지 함수
    public void stopMonitoring(){
        timer.cancel();
    }

    // 앱별 네트워크 사용량을 구하고 업데이트 하는 함수
    public void detectTraffic(){

        try {

            LocalDateTime time = LocalDateTime.now();  // 현재 탐지 시간

            Log.v("TrafficMonitor", "Checking - " + time.toString());

            // 와이파이를 이용한 앱들의 목록과 사용량 구하기
            NetworkStats networkStats =
                    networkStatsManager.querySummary(NetworkCapabilities.TRANSPORT_WIFI,
                            "",
                            System.currentTimeMillis() - lambda,
                            System.currentTimeMillis());
            do {
                NetworkStats.Bucket bucket = new NetworkStats.Bucket();
                networkStats.getNextBucket(bucket);

                // 앱 정보 얻기
                final int uid = bucket.getUid();  // 앱 uid
                int index = whitelistManager.getIndex(uid);
                final String processName = (index != -1 ? whitelistManager.getAppDetail(index).getAppProcessName() : "untitled");
                final long usage = bucket.getTxBytes();  // 현재까지 보낸 트래픽 총량
                final long diff = usage - Optional.ofNullable(lastUsage.get(processName)).orElse((long) 0);  // 증가한 트래픽 양

                // 시스템 기본 앱은 넘김
                if(uid == 0 || uid == 1000) continue;

                // 중복되는 내역은 넘김
                if(diff <= 0) continue;

                // 앱 추가 정보 얻기
                final String appLabel = (index != -1 ? whitelistManager.getAppDetail(index).getAppLabel() : "untitled");
                final int risk = userEventManager.accessRisk(uid, processName);

                // 현재 함수가 lastUsage 컬렉션 초기화를 위해 실행중인 경우에는 히스토리 목록에 넣지 않는다
                if(isInitialized){
//                         초기화가 이미 이루어진 경우 히스토리 목록 업데이트
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 히스토리 인스턴스 생성 후 히스토리 목록에 추가

                            TrafficDetail trafficDetail = new TrafficDetail(time, appLabel, processName, uid, usage, risk);
                            trafficHistory.addTraffic(trafficDetail);

                            listViewHistory.setAdapter(trafficHistoryAdapter);

                            // 로그 파일에 저장
                            logFileProcessor.writeLog(activity, trafficDetail);

                            // 콘솔 로그 출력
                            String log = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                            log += "," + uid + "," + usage + "," + risk + "," + appLabel + "," + processName;
                            Log.e("Traffic Log: ", log);
                            Log.e("App info: ", "whitelist: " + userEventManager.checkWhitelist(uid)
                             + ", touch event: " + userEventManager.checkTouchEvent(processName)
                            + ", screen on: " + userEventManager.checkScreenOn());
                        }
                    });
                }

                // 앱의 마지막 네트워크 사용량 업데이트
                lastUsage.put(processName, usage);


            } while (networkStats.hasNextBucket());

            networkStats.close();

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        // 한 번이라도 여기까지 진행된다면 초기화가 완료된 것임
        // 초기화를 하지 않으면 같은 트래픽 내역이 반복적으로 탐지됨
        isInitialized = true;
    }
}
