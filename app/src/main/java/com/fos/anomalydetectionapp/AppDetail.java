package com.fos.anomalydetectionapp;


// 앱 네트워크 사용 정보 구조체
public class AppDetail {
    private int index;
    private String appLabel;  // 앱 레이블(기본 이름)
    private String appProcessName;  // 앱 이름(상세 이름)
    private int uid;
    private boolean isInWhitelist;

    public AppDetail(int index, String appLabel, String appProcessName, int uid, boolean isInWhitelist) {
        this.index = index;
        this.appLabel = appLabel;
        this.uid = uid;
        this.appProcessName = appProcessName;
        this.isInWhitelist = isInWhitelist;
    }

    public String getAppLabel() {
        return appLabel;
    }

    public String getAppProcessName() {
        return appProcessName;
    }

    public int getUid() { return uid; }

    public boolean getIsInWhitelist() {
        return isInWhitelist;
    }

    public void setInWhitelist(boolean isInWhitelist) {
        this.isInWhitelist = isInWhitelist;
    }
}
