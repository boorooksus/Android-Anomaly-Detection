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
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;

public class AppsManager extends AppCompatActivity {

    Activity activity;
    private static ArrayList<AppDetail> appDetails;
    private static HashMap<Integer, Integer> appIndex;


    public void initializeApps(){

        if(appDetails != null)
            return;

        PackageManager pm = activity.getPackageManager();
        appDetails = new ArrayList<>();
        appIndex = new HashMap<>();
        HashSet<Integer> appSet = new HashSet<>();


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

            if (processName.contains("com.android") || processName.contains("com.google")
                    || processName.contains("com.lge") || processName.contains("android.process"))
                isSafe = true;

            AppDetail appDetail = new AppDetail(i, appName, processName, uid, isSafe);
            appDetails.add(appDetail);
            appIndex.put(uid, i);
            i++;
        }

    }


    public void setArgs(Activity activity){
        this.activity = activity;
    }

    public void setAppDetail(int position, boolean isSafe){
        AppDetail temp = appDetails.get(position);
        temp.setInWhitelist(isSafe);
        appDetails.set(position, temp);

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

//    public void updateSettings(){
//        saveAppDetails(activity, appDetails);
//    }
//
//    // HashMap 저장
//    public void saveAppDetails(Context context, ArrayList<AppDetail> appDetails) {
//        SharedPreferences mmPref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
//        if (mmPref != null) {
//            JSONObject jsonObject = new JSONObject((Map) appDetails);
//            String jsonString = jsonObject.toString();
//            SharedPreferences.Editor editor = mmPref.edit();
//            editor.remove("AppDetails").apply();
//            editor.putString("AppDetails", jsonString);
//            editor.apply();
//        }
//    }
//
//    // HashMap 불러오기
//    public ArrayList<AppDetail> loadAppDetails(Context context) {
//        ArrayList<AppDetail> outputList = new ArrayList<AppDetail>();
//        SharedPreferences mmPref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
//        try {
//            if (mmPref != null) {
//                String jsonString = mmPref.getString("AppDetails", (new JSONObject()).toString());
//                JSONObject jsonObject = new JSONObject(jsonString);
//
//                Iterator<String> keysItr = jsonObject.keys();
//                while (keysItr.hasNext()) {
//                    String key = keysItr.next();
//                    AppDetail value = (AppDetail) jsonObject.get(key);
//                    outputList.add(value);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return outputList;
//    }
}
