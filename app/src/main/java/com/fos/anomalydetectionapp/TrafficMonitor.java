package com.fos.anomalydetectionapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.NetworkCapabilities;
import android.os.RemoteException;
import android.util.Log;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

// 트래픽 모니터링 클래스
public class TrafficMonitor extends AppCompatActivity {

    private static Map<String, String> appNames;  // 앱의 process name과 이름 매핑
    private static Map<String, Long> lastUsage;  // 앱의 마지막 트래픽 저장
    private static boolean isInitialized;  // lastUsage 리스트 초기화 여부 저장
    TrafficHistory trafficHistory;  // 트래픽 히스토리 내역 리스트
    private final NetworkStatsManager networkStatsManager;  // 어플 별 네트워크 사용 내역 얻을 때 사용
    private final Activity activity;  // 메인 액티비티 context
    private final PackageManager pm;  // 앱 정보들을 얻기 위한 패키지 매니저
    private static LogInternalFileProcessor logFileProcessor;  // 로그 파일 쓰기 위한 객체
    HistoryAdapter historyAdapter;  // 히스토리 리스트뷰 어댑터
    UserEventManager userEventManager;
    Timer timer;
    ListView listViewHistory;

    // Constructor
    public TrafficMonitor(Activity activity, HistoryAdapter historyAdapter, TrafficHistory trafficHistory) {

        // 초기화
        appNames = new HashMap<>();
        lastUsage = new HashMap<>();
        isInitialized = false;
        this.trafficHistory = trafficHistory;
        this.activity = activity;
        this.historyAdapter = historyAdapter;
        logFileProcessor = new LogInternalFileProcessor();
        userEventManager = new UserEventManager(activity);
        timer = new Timer();
        listViewHistory = activity.findViewById(R.id.listViewHistory);

        pm = activity.getPackageManager();
        networkStatsManager =
                (NetworkStatsManager) activity.getApplicationContext().
                        getSystemService(Context.NETWORK_STATS_SERVICE);

    }

    // 디바이스에 설치된 어플들 이름 저장 및 현재까지 사용한 트래픽 초기화
    public void initializeTraffic(){

//         쓰레드 생성
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 디바이스에 설치된 앱들의 app process name, 앱 이름 매핑해서 리스트에 저장
                @SuppressLint("QueryPermissionsNeeded") List<ApplicationInfo> apps = pm.getInstalledApplications(0);
                for (ApplicationInfo app : apps) {
                    String appName = app.loadLabel(pm).toString();
                    String processName = app.processName;

                    appNames.put(processName, appName);
                }

                // 현재까지 앱별로 데이터 사용량 저장
//                updateUsage();

            }
        }).start();

        // 디바이스에 설치된 앱들의 app process name, 앱 이름 매핑해서 리스트에 저장
//        @SuppressLint("QueryPermissionsNeeded") List<ApplicationInfo> apps = pm.getInstalledApplications(0);
//        for (ApplicationInfo app : apps) {
//            String appName = app.loadLabel(pm).toString();
//            String processName = app.processName;
//
//            appNames.put(processName, appName);
//        }
//
//        try {
//
//            // 와이파이를 이용한 앱들의 목록과 사용량 구하기
//            NetworkStats networkStats =
//                    networkStatsManager.querySummary(NetworkCapabilities.TRANSPORT_WIFI,
//                            "",
//                            System.currentTimeMillis() - 20000,
//                            System.currentTimeMillis());
//            do {
//                NetworkStats.Bucket bucket = new NetworkStats.Bucket();
//                networkStats.getNextBucket(bucket);
//
//                final int uid = bucket.getUid();  // 앱 uid
//                final String processName = pm.getNameForUid(uid);
//
//                // 앱 정보 얻기
//                final String appLabel = Optional.ofNullable(appNames.get(processName)).orElse("untitled");  // 앱 레이블(기본 이름)
//                final long txBytes = bucket.getTxBytes();  // 현재까지 보낸 트래픽 총량
//                final long diff = txBytes - Optional.ofNullable(lastUsage.get(processName)).orElse((long) 0);  // 증가한 트래픽 양
//
//                if(diff <= 0){
//                    // 앱 네트워크 사용량에 변동이 없는 경우 continue
//                    continue;
//                }
//
//                // 앱의 마지막 네트워크 사용량 업데이트
//                lastUsage.put(processName, txBytes);
//
//
//            } while (networkStats.hasNextBucket());
//
//            networkStats.close();
//
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//
//        // 한 번이라도 여기까지 진행된다면 초기화가 완료된 것임
//        isInitialized = true;

    }

    // 앱별 네트워크 사용량을 구하고 업데이트 하는 함수
    public void detectTraffic(){

        if(!isInitialized){
            initializeTraffic();
        }

        try {
            Log.v("TrafficMonitor", "Checking - " + LocalDateTime.now().toString());
//            Log.v("TrafficMonitor", "initialized: " + isInitialized);

            // 와이파이를 이용한 앱들의 목록과 사용량 구하기
            NetworkStats networkStats =
                    networkStatsManager.querySummary(NetworkCapabilities.TRANSPORT_WIFI,
                            "",
                            System.currentTimeMillis() - 20000,
                            System.currentTimeMillis());
            do {
                NetworkStats.Bucket bucket = new NetworkStats.Bucket();
                networkStats.getNextBucket(bucket);

                final int uid = bucket.getUid();  // 앱 uid
                final String processName = pm.getNameForUid(uid);

                // 앱 정보 얻기
                final String appLabel = Optional.ofNullable(appNames.get(processName)).orElse("untitled");  // 앱 레이블(기본 이름)
                final long txBytes = bucket.getTxBytes();  // 현재까지 보낸 트래픽 총량
                final long diff = txBytes - Optional.ofNullable(lastUsage.get(processName)).orElse((long) 0);  // 증가한 트래픽 양
                final int risk = userEventManager.getRisk();

                if(diff <= 0){
                    // 앱 네트워크 사용량에 변동이 없는 경우 continue
                    continue;
                }

                // 현재 함수가 lastUsage 컬렉션 초기화를 위해 실행중인 경우에는 히스토리 목록에 넣지 않는다
                if(isInitialized){
                    // 초기화가 이미 이루어진 경우 히스토리 목록 업데이트
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 히스토리 인스턴스 생성 후 히스토리 목록에 추가
                            TrafficDetail trafficDetail = new TrafficDetail(LocalDateTime.now(), appLabel, processName, uid, txBytes, diff, risk);
                            trafficHistory.addTraffic(trafficDetail);

                            activity.runOnUiThread(new Runnable(){
                                @Override
                                public void run() {
                                    // 어댑터 업데이트
//                                    historyAdapter.notifyDataSetChanged();
                                    listViewHistory.setAdapter(historyAdapter);
                                }
                            });


                            // 로그 파일에 저장
                            logFileProcessor.writeLog(activity, trafficDetail);

                            String log = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                            log += "," + uid + "," + txBytes + "," + diff + "," + risk + "," + appLabel + "," + processName;
                            Log.v("TrafficMonitor1", log);

                            if(userEventManager.checkTouchEvent())
                                Log.v("TrafficMonitor2", "Touch Event Detected");
                            else
                                Log.v("TrafficMonitor2", "No Touch Event!!!!");

                            if(userEventManager.checkAudioEvent())
                                Log.v("TrafficMonitor3", "Audio Playing Detected");
                            else
                                Log.v("TrafficMonitor3", "No Audio Playing!!!!");

                        }
                    });
                }

                // 앱의 마지막 네트워크 사용량 업데이트
                lastUsage.put(processName, txBytes);


            } while (networkStats.hasNextBucket());

            networkStats.close();

        } catch (RemoteException e) {
            e.printStackTrace();
        }

//        // 한 번이라도 여기까지 진행된다면 초기화가 완료된 것임
        isInitialized = true;
    }

    public void startMonitoring(){

        final Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                // 쓰레드 생성
//                threadMonitoring = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.v("ServiceManager", "Monitoring... " + LocalDateTime.now().toString());
//
//                        trafficMonitor.updateUsage();
//
//                        activity.runOnUiThread(new Runnable(){
//                            @Override
//                            public void run() {
//
//                                listViewHistory.setAdapter(historyAdapter);
//                            }
//
//                        });
//                    }
//                });
//                threadMonitoring.start();

//                trafficMonitor.initializeTraffic();

                detectTraffic();

//                activity.runOnUiThread(new Runnable(){
//                    @Override
//                    public void run() {
//
//                        historyAdapter.notifyDataSetChanged();
////                        listViewHistory.setAdapter(historyAdapter);
//                    }
//
//                });
            }
        };

        // 타이머들을 각각 20초, 10로 설정하고 작동
        timer.schedule(timerTask, 0, 20000);
    }

    public void stopMonitoring(){
        timer.cancel();

    }
}
