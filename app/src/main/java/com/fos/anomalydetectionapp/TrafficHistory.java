package com.fos.anomalydetectionapp;

import java.util.List;
import java.util.Vector;

// 앱 트래픽 히스토리 관리 클래스
public class TrafficHistory {

    private static final List<TrafficDetail> history = new Vector<>();  // 히스토리 저장 리스트

    // 트래픽 히스토리 추가
    public void addTraffic(TrafficDetail trafficDetail){
        TrafficHistory.history.add(0, trafficDetail);
    }

    // 저장된 히스토리 개수 리턴
    public int getLength(){
        return history.size();
    }

    // 특정 인덱스의 트래픽 내역 리턴
    public TrafficDetail getTraffic(int position){
        return history.get(position);
    }

}
