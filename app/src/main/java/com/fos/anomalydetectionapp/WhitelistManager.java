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

        HashSet<Integer> appSet = new HashSet<>();  // 네트워크 사용 앱 이름 set
        appDetails = new ArrayList<>();  // 네트워크 사용 앱 정보 배열
        appIndex = new HashMap<>();  // 앱 정보 배열에 저장된 인덱스 번호 맵
        PackageManager packageManager = activity.getPackageManager();
        NetworkStatsManager networkStatsManager = (NetworkStatsManager) activity.getApplicationContext().
                getSystemService(Context.NETWORK_STATS_SERVICE);

        // 디바이스에 설치된 앱들 중에서 네트워크 사용 이력이 있는 앱들 정보 가져오기
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

                // 시스템 어플은 저장하지 않고 넘김
                if (uid == 0 || uid == 1000) continue;

                appSet.add(uid);

            } while (networkStats.hasNextBucket());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        // 디바이스에 설치된 앱들 정보 중에서 네트워크 사용 이력 있는 앱들만 저장
        @SuppressLint("QueryPermissionsNeeded") List<ApplicationInfo> apps = packageManager.getInstalledApplications(0);
        int i = 0;
        for (ApplicationInfo app : apps) {
            String appName = app.loadLabel(packageManager).toString();
            String processName = app.processName;
            int uid = app.uid;

            // 네트워크 사용하지 않는 앱이면 continue
            if (!appSet.contains(uid)) continue;

            boolean isSafe = false;

            // 사용자가 화이트리스트에 등록한 앱인 경우
            if(isInitialized && whiteSet.contains(processName))
                isSafe = true;

            // 사용자의 화이트리스트 세팅한적이 없고 안드로이드 기본 앱인 경우
            else if (!isInitialized && checkDefaultWhitelist(processName)) {
                isSafe = true;
                whiteSet.add(processName);
            }

            AppDetail appDetail = new AppDetail(i, appName, processName, uid, isSafe);
            appDetails.add(appDetail);
            appIndex.put(uid, i);
            i++;
        }
    }

    // 앱 처음 실행 시 기본적으로 화이트리스트에 등록하는 기준
    public boolean checkDefaultWhitelist(String processName){
        // 구글, 안드로이드, lg, 삼성 기본 어플인 경우 화이트리스트에 등록
        return processName.contains("com.android") || processName.contains("com.google")
                || processName.contains("com.lge") || processName.contains("android.process")
                || processName.contains("com.samsung");
    }

    // 앱의 화이트리스트 등록 여부 변경 함수
    public void setAppDetail(int position, boolean isSafe){
        AppDetail appDetail = appDetails.get(position);
        appDetail.setInWhitelist(isSafe);
        appDetails.set(position, appDetail);

        if (isSafe)
            whiteSet.add(appDetail.getAppProcessName());
        else
            whiteSet.remove(appDetail.getAppProcessName());
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
