package com.fos.anomalydetectionapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class TrafficMonitorService extends Service {

    @SuppressLint("StaticFieldLeak")
    static Activity activity;
    @SuppressLint("StaticFieldLeak")
    static AdapterHistory adapterHistory;

    public void setArgs(Activity activity, AdapterHistory adapterHistory) {
        TrafficMonitorService.activity = activity;
        TrafficMonitorService.adapterHistory = adapterHistory;
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();

        final TrafficMonitor trafficMonitor = new TrafficMonitor(activity, adapterHistory);

        trafficMonitor.startTracking();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }
}
