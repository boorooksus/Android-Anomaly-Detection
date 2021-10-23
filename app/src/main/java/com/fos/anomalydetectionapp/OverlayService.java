package com.fos.anomalydetectionapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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

import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

import androidx.core.app.NotificationCompat;

import java.util.concurrent.TimeUnit;

public class OverlayService extends Service {

    WindowManager wm;
    View mView;
    EventManagement eventManagement = new EventManagement();

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @SuppressLint({"RtlHardcoded", "InflateParams"})
    @Override
    public void onCreate() {
        super.onCreate();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(wm != null) {
            if(mView != null) {
                wm.removeView(mView);
                mView = null;
            }
            wm = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        setNotification();

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

                int posX = (int)event.getX();
                int posY = (int)event.getY();

//                Log.v("OverlayService", "TOUCH EVENT OCCURRED! (" + posX + ", " + posY + ")");

                eventManagement.addTouchEvent();

                return true;
            }
        });

        wm.addView(mView, params);


        return Service.START_STICKY;
    }

    public void setNotification() {

        String NOTIFICATION_CHANNEL_ID = "com.fos.anomalydetectionapp";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);

//        Intent notificationIntent = new Intent(this, Activity.class);
//        PendingIntent pendingIntent =
//                PendingIntent.getActivity(this, 0, notificationIntent, 0);

//        NotificationChannel notificationChannel = new NotificationChannel("notification_id","MyApp notification", NotificationManager.IMPORTANCE_HIGH);
//
//        notificationChannel.enableLights(true);
//        notificationChannel.setLightColor(Color.RED);
//        notificationChannel.enableVibration(true);
//        notificationChannel.setDescription("Overlay Tests");
//
//        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
//        notificationManager.createNotificationChannel(notificationChannel);
//
//        Notification notification =
//                new Notification.Builder(this, "OVERLAY")
//                        .setContentTitle("OVERLAY_TITLE")
//                        .setContentText("OVERLAY_MSG")
////                        .setSmallIcon(R.drawable.ic_launcher_foreground)
////                        .setContentIntent(pendingIntent)
////                        .setTicker("OVERLAY_TICKER")
//                        .build();
//
//
//        startForeground(10, notification);
    }
}
