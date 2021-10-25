package com.fos.anomalydetectionapp;

import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;

public class ServiceManager extends Service {

    @SuppressLint("StaticFieldLeak")
    static Activity activity;
    @SuppressLint("StaticFieldLeak")
    static HistoryAdapter historyAdapter;

    ListView listViewHistory;
    static Timer timer;
    Thread threadMonitoring;
    TrafficHistory trafficHistory;
    TrafficMonitor trafficMonitor;

    WindowManager wm;
    View mView;
    UserEventManager userEventManager;
    SharedPreferences preferences;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint({"RtlHardcoded", "InflateParams"})
    @Override
    public void onCreate() {
        super.onCreate();


    }

    @SuppressLint({"RtlHardcoded", "InflateParams"})
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        Log.v("Service", "=======Service is Started...");

//        if (intent.getAction().equals("stop")) {
//            stopForeground(true);
//            stopSelf();
//        }

        setNotification();

        trafficHistory = new TrafficHistory();
        historyAdapter = new HistoryAdapter(activity, trafficHistory);
//        AppsManager appsManager = new AppsManager(activity);
        AppsManager appsManager = new AppsManager();
        userEventManager = new UserEventManager(activity, appsManager);
        trafficMonitor = new TrafficMonitor(activity, historyAdapter, trafficHistory);

        startTrafficMonitoring();
//        trafficMonitor.startMonitoring();
        createOverlay();


        return Service.START_STICKY;
    }

    public void setArgs(Activity activity) {
        ServiceManager.activity = activity;
    }

    public HistoryAdapter getHistoryAdapter() {
        return historyAdapter;
    }

    public void startTrafficMonitoring(){
        listViewHistory = activity.findViewById(R.id.listViewHistory);

        // 일정 시간 간격으로 앱별 네트워크 사용량 체크하는 타이머

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                if(trafficMonitor != null)
                    trafficMonitor.detectTraffic();

                activity.runOnUiThread(new Runnable(){
                    @Override
                    public void run() {

                        listViewHistory.setAdapter(historyAdapter);
                    }

                });

//                if (preferences != null && !preferences.getBoolean("isRunning", false))
//                    timer.cancel();
            }
        };


        timer = new Timer();
        // 타이머들을 각각 20초, 10로 설정하고 작동
        timer.schedule(timerTask, 0, 20000);
    }

    public void createOverlay(){
        LayoutInflater inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);



        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                /*ViewGroup.LayoutParams.MATCH_PARENT*/300,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                30, 30, // X, Y 좌표
                TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.LEFT | Gravity.BOTTOM;

        mView = inflate.inflate(R.layout.overlay_view, null);

        mView.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                userEventManager.addTouchEvent();

                return true;
            }
        });

        wm.addView(mView, params);
    }

    public void setNotification() {

        // Set Notification Channel
        String NOTIFICATION_CHANNEL_ID = "com.fos.anomalydetectionapp";
        String channelName = "Anomaly Detection Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        // set Notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

//    public void stopForegroundService(){
//
//        stopForeground(true);
//        stopSelf();
//
//        if(timer != null)
//            timer.cancel();
//
//        if(wm != null) {
//            if(mView != null) {
//                wm.removeView(mView);
//                mView = null;
//            }
//            wm = null;
//        }
//
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // stop monitoring
//        threadMonitoring.interrupt();

//        trafficMonitor.stopMonitoring();
        timer.cancel();


        // terminate overlay
        if(wm != null) {
            if(mView != null) {
                wm.removeView(mView);
                mView = null;
            }
            wm = null;
        }

        stopForeground(true);
        stopSelf();
    }
}
