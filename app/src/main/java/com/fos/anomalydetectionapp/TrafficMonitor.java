package com.fos.anomalydetectionapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.NetworkCapabilities;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;

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
    AdapterHistory adapterHistory;  // 히스토리 리스트뷰 어댑터

    // Constructor
    public TrafficMonitor(Activity activity, AdapterHistory adapterHistory) {

        // 초기화
        appNames = new HashMap<>();
        lastUsage = new HashMap<>();
        isInitialized = false;
        trafficHistory = new TrafficHistory();
        this.activity = activity;
        this.adapterHistory = adapterHistory;
        logFileProcessor = new LogInternalFileProcessor();

        pm = activity.getPackageManager();
        networkStatsManager =
                (NetworkStatsManager) activity.getApplicationContext().
                        getSystemService(Context.NETWORK_STATS_SERVICE);

    }

    // 디바이스에 설치된 어플들 이름 저장 및 현재까지 사용한 트래픽 초기화
    public void initializeTraffic(){

        // 쓰레드 생성
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
                updateUsage();

            }
        }).start();

    }

    // 스토리지 접근 권한 및 앱의 사용 기록 액세스 권한 체크 함수
    // 권한 있는 경우 true, 없는 경우 유저를 설정 앱으로 보내고 false 리턴
    public boolean checkPermission(){

        try{
            // 아래 코드를 실행해 보고 에러가 없다면 권한이 존재
            // 에러 체크 외에 다른 목적은 없음
            NetworkStats networkStats =
                    networkStatsManager.queryDetailsForUid(
                            NetworkCapabilities.TRANSPORT_WIFI,
                            "",
                            0,
                            System.currentTimeMillis(),
                            0);
            networkStats.close();

            return true;

        } catch(Exception e){

            // 위에서 에러가 존재한다면 권한이 제한되어 있음
            // 유저를 설정 페이지로 보냄
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    // 알림 생성
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setMessage("앱의 사용 기록 액세스를 허용해주세요");
                    builder.setPositiveButton(
                            "확인",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    // 유저를 설정 페이지로 보냄
                                    activity.startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                                    //finish();
                                }
                            });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            });
            return false;
        }
    }

    // 일정 시간마다 앱 트래픽 모니터링하는 함수
    public void startTracking() {

        // 디바이스에 설치된 어플들 이름 저장 및 현재까지 사용한 트래픽 초기화
        if(!isInitialized) {
            initializeTraffic();
        }

        // 일정 시간 간격으로 앱별 네트워크 사용량 체크하는 타이머
        final Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                // 쓰레드 생성
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        updateUsage();
                    }
                }).start();

            }
        };

        // 네트워크 사용량 체크 타이머를 컨트롤하는 타이머
        final Timer timerController = new Timer();
        TimerTask timerControllerTask = new TimerTask() {
            @Override
            public void run() {
                // 작동 여부 공유 변수 가져오기
                SharedPreferences preferences = activity.getPreferences(MODE_PRIVATE);
                boolean isRunning = preferences.getBoolean("isRunning", false);

                if(!isRunning){
                    // 모니터링 중지시킨 경우, 현재 타이머와 네트워크 사용량 체크 타이머 캔슬
                    timer.cancel();
                    timerController.cancel();
                    System.out.println(" Tracking is stopped");
                }
            }
        };

        // 타이머들을 각각 20초, 10로 설정하고 작동
        timer.schedule(timerTask, 0, 20000);
        timerController.schedule(timerControllerTask, 0, 10000);

    }

    // 앱별 네트워크 사용량을 구하고 업데이트 하는 함수
    public void updateUsage(){

        try {
            Log.v("TrafficMonitor", LocalDateTime.now().toString());

            // 와이파이를 이용한 앱들의 목록과 사용량 구하기
            NetworkStats networkStats =
                    networkStatsManager.querySummary(NetworkCapabilities.TRANSPORT_WIFI,
                            "",
                            0,
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
                            TrafficDetail trafficDetail = new TrafficDetail(LocalDateTime.now(), appLabel, processName, uid, txBytes, diff);
                            trafficHistory.addTraffic(trafficDetail);
                            // 어댑터 업데이트
                            adapterHistory.notifyDataSetChanged();

                            // 로그 파일에 저장
                            logFileProcessor.writeLog(activity, trafficDetail);

                            String log = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                            log += "," + uid + "," + txBytes + "," + diff + "," + appLabel + "," + processName;
                            Log.v("TrafficMonitor", log);
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

        // 한 번이라도 여기까지 진행된다면 초기화가 완료된 것임
        isInitialized = true;
    }
}
