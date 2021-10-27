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
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;

public class AppsManager extends AppCompatActivity {

    private static Activity activity;
    private static ArrayList<AppDetail> appDetails;
    private static HashMap<Integer, Integer> appIndex;
    private static HashSet<Integer> appSet;
    private static HashSet<String> whiteSet;
    private boolean isInitialized = false;
    Context context;


    public void initializeApps(Activity activity, Context context){

        this.activity = activity;
        this.context = context;

        if(appDetails != null)
            return;

        whiteSet = loadWhiteSet(context, "whiteSet");

        if (!whiteSet.isEmpty()){
            isInitialized = true;
        }

        appSet = new HashSet<>();
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

    public void updateWhitelist(){
        saveWhiteSet(context, "whiteSet", whiteSet);
    }

    // HashMap 저장
    public void saveWhiteSet(Context context, String key, HashSet<String> whiteSet) {

        SharedPreferences prefs = activity.getSharedPreferences("whiteSet", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();


//        SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
        Log.v("=====================", "+++++++++++++++++++++++++++++");
        editor.putStringSet(key, whiteSet);
        editor.apply(); // 스위치 상태 변수 저장

//        JSONArray a = new JSONArray();
//
//        // Iterator 사용
//        for (String s : whiteSet) {
//            a.put(s);
//        }
//
//        if (!whiteSet.isEmpty()) {
//            editor.putString(key, a.toString());
//        } else {
//            editor.putString(key, null);
//        }


        editor.apply();

        Log.v("===============Save whiteSet", "save Success!!!");
    }

    // HashMap 불러오기
    public HashSet<String> loadWhiteSet(Context context, String key) {

//        SharedPreferences prefs = context.getSharedPreferences(key, MODE_PRIVATE);

//        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        SharedPreferences prefs = activity.getSharedPreferences("whiteSet", Context.MODE_PRIVATE);

        Set<String> result = prefs.getStringSet(key, new HashSet<>());

        return (HashSet<String>) result;


//        String json = prefs.getString(key, null);
//        HashSet<String> urls = new HashSet<>();
//        if (json != null) {
//            try {
//                JSONArray a = new JSONArray(json);
//                for (int i = 0; i < a.length(); i++) {
//                    String url = a.optString(i);
//                    urls.add(url);
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//
//        if (urls.isEmpty())
//            Log.v("==============Load whiteSet", "Load Fail!!!");
//        else
//            Log.v("==============Load whiteSet", "Load Success!!!");
//
//
//        return urls;
    }
}
