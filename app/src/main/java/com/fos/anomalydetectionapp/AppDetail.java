package com.fos.anomalydetectionapp;


// 앱 네트워크 사용 정보 구조체
public class AppDetail {
    private int index;  // 자료구조에 저장된 인덱스
    private String appLabel;  // 앱 이름
    private String appProcessName;  // 앱 프로세스 이름(패키지 네임)
    private int uid;  // 앱 uid
    private boolean isInWhitelist;  // 화이트리스트 등록 여부

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
