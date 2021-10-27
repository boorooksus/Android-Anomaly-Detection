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
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


// 백그라운드 실행을 위한 서비스 관리 클래스
public class ServiceManager extends Service {

    @SuppressLint("StaticFieldLeak")
    static Activity activity;
    @SuppressLint("StaticFieldLeak")
    static TrafficHistoryAdapter trafficHistoryAdapter;
    TrafficHistory trafficHistory;
    TrafficMonitor trafficMonitor;
    WindowManager wm;
    View mView;
    UserEventManager userEventManager;

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

        setNotification();

        trafficHistory = new TrafficHistory();
        trafficHistoryAdapter = new TrafficHistoryAdapter(activity, trafficHistory);
        WhitelistManager whitelistManager = new WhitelistManager();
        userEventManager = new UserEventManager(activity, whitelistManager);
        trafficMonitor = new TrafficMonitor(activity, trafficHistoryAdapter, trafficHistory);

        // start traffic monitoring
        trafficMonitor.startMonitoring();
        // create overlay for detecting touch event
        createOverlay();

        return Service.START_STICKY;
    }

    // 변수 세팅 함수
    public void setArgs(Activity activity) {
        ServiceManager.activity = activity;
    }

    // 트래픽 내역 리스트뷰 어댑터 리턴 함수
    public TrafficHistoryAdapter getHistoryAdapter() {
        return trafficHistoryAdapter;
    }

    // 오버레이 생성 함수
    public void createOverlay(){
        LayoutInflater inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        // 오버레이 세팅
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

        // 터치가 감지되면 userEventManager 클래스의 마지막 터치 발생 시간 업데이트
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

    // Notification 설정
    // Foreground Service가 실행되면 5초 이내에 notification 정보를 시스템에 보내야함
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
//                .setContentTitle("Monitoring Service s Running")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    // 서비스 종료
    @Override
    public void onDestroy() {
        super.onDestroy();

        // stop monitoring
        trafficMonitor.stopMonitoring();

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
