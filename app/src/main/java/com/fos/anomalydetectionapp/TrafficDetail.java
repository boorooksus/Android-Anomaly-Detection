package com.fos.anomalydetectionapp;

import java.time.LocalDateTime;

// 앱 네트워크 사용 정보 구조체
public class TrafficDetail {
    private final LocalDateTime time;  // 업데이트 시각
    private final String appLabel;  // 앱 레이블(기본 이름)
    private final String appProcessName;  // 앱 이름(상세 이름)
    private final int uid;  // uid
    private final long usage;  // 송신 트래픽 발생량
    private final int risk;  // 위험도

    // Constructor
    public TrafficDetail(LocalDateTime time, String appLabel, String appProcessName, int uid, long usage, int risk) {
        this.time = time;
        this.appLabel = appLabel;
        this.appProcessName = appProcessName;
        this.uid = uid;
        this.usage = usage;
        this.risk = risk;
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

    public int getRisk() {
        return risk;
    }
}
