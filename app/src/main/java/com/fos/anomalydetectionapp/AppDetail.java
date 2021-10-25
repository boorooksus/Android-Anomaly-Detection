package com.fos.anomalydetectionapp;

import java.time.LocalDateTime;

// 앱 네트워크 사용 정보 구조체
public class AppDetail {
    private int index;
    private String appLabel;  // 앱 레이블(기본 이름)
    private String appProcessName;  // 앱 이름(상세 이름)
    private boolean isInWhitelist;

    public AppDetail(int index, String appLabel, String appProcessName, boolean isInWhitelist) {
        this.index = index;
        this.appLabel = appLabel;
        this.appProcessName = appProcessName;
        this.isInWhitelist = isInWhitelist;
    }

    public int getIndex() {
        return index;
    }

    public String getAppLabel() {
        return appLabel;
    }

    public String getAppProcessName() {
        return appProcessName;
    }

    public boolean isInWhitelist() {
        return isInWhitelist;
    }

    public void setInWhitelist(boolean inWhitelist) {
        isInWhitelist = inWhitelist;
    }
}
