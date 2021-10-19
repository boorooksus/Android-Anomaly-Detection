package com.fos.anomalydetectionapp;

import java.util.List;
import java.util.Vector;

// 앱 트래픽 히스토리 관리 클래스
public class Histories {

    private static List<History> histories = new Vector<>();  // 히스토리 저장 리스트

    // 히스토리 추가
    public void addHistory(History history){
        histories.add(0, history);
    }

    // 저장된 히스토리 개수 리턴
    public int getLength(){
        return histories.size();
    }

    // 특정 인덱스의 히스토리 리턴
    public History getHistory(int position){
        return histories.get(position);
    }

}
