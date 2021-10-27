package com.fos.anomalydetectionapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.NetworkCapabilities;
import android.os.RemoteException;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class WhitelistManager extends AppCompatActivity {

    private static Activity activity;
    private static ArrayList<AppDetail> appDetails;
    private static HashMap<Integer, Integer> appIndex;
    private static HashSet<String> whiteSet;
    private boolean isInitialized = false;


    public void initializeApps(Activity activity){

        WhitelistManager.activity = activity;

        if(appDetails != null)
            return;

        whiteSet = loadWhiteSet();

        if (!whiteSet.isEmpty()){
            isInitialized = true;
        }

        HashSet<Integer> appSet = new HashSet<>();
        appDetails = new ArrayList<>();
        appIndex = new HashMap<>();
        PackageManager pm = activity.getPackageManager();

        NetworkStatsManager networkStatsManager = (NetworkStatsManager) activity.getApplicationContext().
                getSystemService(Context.NETWORK_STATS_SERVICE);

        try {
            NetworkStats networkStats =
                    networkStatsManager.querySummary(NetworkCapabilities.TRANSPORT_WIFI,
                            "",
                            0,
                            System.currentTimeMillis());
            do {
                NetworkStats.Bucket bucket = new NetworkStats.Bucket();
                networkStats.getNextBucket(bucket);

                int uid = bucket.getUid();

                if (uid == 0 || uid == 1000) continue;

                appSet.add(uid);

            } while (networkStats.hasNextBucket());
        } catch (RemoteException e) {
            e.printStackTrace();
        }


        @SuppressLint("QueryPermissionsNeeded") List<ApplicationInfo> apps = pm.getInstalledApplications(0);
        int i = 0;
        for (ApplicationInfo app : apps) {
            String appName = app.loadLabel(pm).toString();
            String processName = app.processName;
            int uid = app.uid;

            if (!appSet.contains(uid)) continue;

            boolean isSafe = false;

            if(isInitialized && whiteSet.contains(processName))
                isSafe = true;

            else if (!isInitialized && (processName.contains("com.android") || processName.contains("com.google")
                    || processName.contains("com.lge") || processName.contains("android.process"))) {
                isSafe = true;
                whiteSet.add(processName);
            }

            AppDetail appDetail = new AppDetail(i, appName, processName, uid, isSafe);
            appDetails.add(appDetail);
            appIndex.put(uid, i);
            i++;
        }
    }

    public void setAppDetail(int position, boolean isSafe){
        AppDetail temp = appDetails.get(position);
        temp.setInWhitelist(isSafe);
        appDetails.set(position, temp);

        if (isSafe)
            whiteSet.add(temp.getAppProcessName());
        else
            whiteSet.remove(temp.getAppProcessName());


    }

    // 저장된 히스토리 개수 리턴
    public int getLength(){
        return appDetails.size();
    }

    // 특정 인덱스의 트래픽 내역 리턴
    public AppDetail getAppDetail(int position){
        if (position < 0 || position >= getLength()) return null;
        return appDetails.get(position);
    }

    public int getIndex(Integer uid){
        return Optional.ofNullable(appIndex.get(uid)).orElse(-1);
    }

    // HashMap 저장
    public void saveWhiteSet() {

        SharedPreferences prefs = activity.getSharedPreferences("whiteSet", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.clear();

        editor.putStringSet("whiteSetKey", whiteSet);
        editor.apply();
    }

    // HashMap 불러오기
    public HashSet<String> loadWhiteSet() {
        SharedPreferences prefs = activity.getSharedPreferences("whiteSet", Context.MODE_PRIVATE);
        Set<String> result = prefs.getStringSet("whiteSetKey", new HashSet<>());

        return (HashSet<String>) result;
    }
}
