package com.fos.anomalydetectionapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.ListView;

import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

public class TrafficMonitorService extends Service {

    @SuppressLint("StaticFieldLeak")
    static Activity activity;
    @SuppressLint("StaticFieldLeak")
    static AdapterHistory adapterHistory;

    ListView listViewHistory;
    final Timer timer = new Timer();

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.v("TrafficMonitorService", "start monitoring...");

        listViewHistory = activity.findViewById(R.id.listViewHistory);

        TrafficHistory trafficHistory = new TrafficHistory();
        adapterHistory = new AdapterHistory(activity, trafficHistory);
        final TrafficMonitor trafficMonitor = new TrafficMonitor(activity, adapterHistory, trafficHistory);


        activity.runOnUiThread(new Runnable(){
            @Override
            public void run() {

                listViewHistory.setAdapter(adapterHistory);
            }

        });

        // 일정 시간 간격으로 앱별 네트워크 사용량 체크하는 타이머

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                // 쓰레드 생성
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        trafficMonitor.updateUsage();
                    }
                }).start();

            }
        };

        // 타이머들을 각각 20초, 10로 설정하고 작동
        timer.schedule(timerTask, 0, 20000);

//        trafficMonitor.startTracking();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        Log.v("TrafficMonitorService", "Stop Monitoring....");

    }

    public void setArgs(Activity activity) {
        TrafficMonitorService.activity = activity;
    }

    public AdapterHistory getAdapterHistory() {
        return adapterHistory;
    }
}
