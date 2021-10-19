package com.fos.anomalydetectionapp;

import java.time.LocalDateTime;

// 앱 네트워크 사용 정보 구조체
public class History {
    private LocalDateTime time;  // 업데이트 시각
    private String appLabel;  // 앱 레이블(기본 이름)
    private String appProcessName;  // 앱 이름(상세 이름)
    private int uid;  // uid
    private long usage;  // 네트워크 사용 총량
    private long diff;  // 증가된 네트워크 양

    // Constructor
    public History(LocalDateTime time, String appLabel, String appProcessName, int uid, long usage, long diff) {
        this.time = time;
        this.appLabel = appLabel;
        this.appProcessName = appProcessName;
        this.uid = uid;
        this.usage = usage;
        this.diff = diff;
    }

    // getters

    public LocalDateTime getTime() {
        return time;
    }

    public String getAppLabel() {
        return appLabel;
    }

    public String getAppProcessName() {
        return appProcessName;
    }

    public int getUid() {
        return uid;
    }

    public long getUsage() {
        return usage;
    }

    public long getDiff() { return diff; }
}
