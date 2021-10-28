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

    @SuppressLint("StaticFieldLeak")
    private static Activity activity;
    private static ArrayList<AppDetail> appDetails;
    private static HashMap<Integer, Integer> appIndex;
    private static HashSet<String> whiteSet;
    private boolean isInitialized = false;


    // 디바이스에서 네트워크를 사용하는 앱들 정보 가져오는 함수
    public void initializeApps(Activity activity){

        WhitelistManager.activity = activity;

        // 이미 앱들 정보를 가져와서 세팅한 경우 리턴
        if(appDetails != null)
            return;

        // 사용자가 설정한 화이트리스트 등록 앱 세트 가져오기
        whiteSet = loadWhiteSet();

        if (!whiteSet.isEmpty()){
            // 사용자가 화이트리스트 세팅을 한 적 없는 경우
            isInitialized = true;
        }

        HashSet<Integer> appSet = new HashSet<>();
        appDetails = new ArrayList<>();
        appIndex = new HashMap<>();
        PackageManager pm = activity.getPackageManager();
        NetworkStatsManager networkStatsManager = (NetworkStatsManager) activity.getApplicationContext().
                getSystemService(Context.NETWORK_STATS_SERVICE);

        try {
            // 네트워크 사용 이력이 있는 앱들 정보 가져오기
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


        // 디바이스에 설치된 앱들 정보 가져오기
        @SuppressLint("QueryPermissionsNeeded") List<ApplicationInfo> apps = pm.getInstalledApplications(0);
        int i = 0;
        for (ApplicationInfo app : apps) {
            String appName = app.loadLabel(pm).toString();
            String processName = app.processName;
            int uid = app.uid;

            // 네트워크 사용하지 않는 앱이면 continue
            if (!appSet.contains(uid)) continue;

            boolean isSafe = false;

            // 사용자가 화이트리스트에 등록한 앱인 경우
            if(isInitialized && whiteSet.contains(processName))
                isSafe = true;

            // 사용자의 화이트리스트 세팅한적이 없고 안드로이드 기본 앱인 경우
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

    // 앱의 화이트리스트 등록 여부 변경 함수
    public void setAppDetail(int position, boolean isSafe){
        AppDetail temp = appDetails.get(position);
        temp.setInWhitelist(isSafe);
        appDetails.set(position, temp);

        if (isSafe)
            whiteSet.add(temp.getAppProcessName());
        else
            whiteSet.remove(temp.getAppProcessName());


    }

    // 저장된 히스토리 개수 리턴 함수
    public int getLength(){
        return appDetails.size();
    }

    // 특정 인덱스의 트래픽 내역 리턴 함수
    public AppDetail getAppDetail(int position){
        if (position < 0 || position >= getLength()) return null;
        return appDetails.get(position);
    }

    // uid로 앱의 인덱스 찾는 함수
    public int getIndex(Integer uid){
        return Optional.ofNullable(appIndex.get(uid)).orElse(-1);
    }

    // 사용자가 지정한 화이트리스트 앱 세트 저장 함수
    public void saveWhiteSet() {

        SharedPreferences prefs = activity.getSharedPreferences("whiteSet", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // 기존의 SharedPreferences 파일을 지우고 새로 작성
        editor.clear();
        editor.putStringSet("whiteSetKey", whiteSet);
        editor.apply();
    }

    // 사용자가 지정한 화이트리스트 앱 세트 불러오는 함수
    public HashSet<String> loadWhiteSet() {
        SharedPreferences prefs = activity.getSharedPreferences("whiteSet", Context.MODE_PRIVATE);
        Set<String> result = prefs.getStringSet("whiteSetKey", new HashSet<>());
        return (HashSet<String>) result;
    }
}
