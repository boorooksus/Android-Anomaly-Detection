package com.fos.anomalydetectionapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// 네트워크 히스토리 리스트뷰 어댑터
public class AdapterHistory extends BaseAdapter {

    TrafficHistory trafficHistory;  // 트래픽 히스토리 목록 인스턴스
    LayoutInflater layoutInflater;
    Activity activity;  // 메인 액티비티 컨텍스트

    // Constructor
    public AdapterHistory(Activity activity, TrafficHistory trafficHistory) {
        this.activity = activity;
        this.trafficHistory = trafficHistory;
        layoutInflater = LayoutInflater.from(activity);
    }

    @Override
    public int getCount() {
        return trafficHistory.getLength();
    }

    @Override
    public TrafficDetail getItem(int position) {
        return trafficHistory.getTraffic(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // 리스트뷰의 히스토리 출력 방식 설정
    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // res/layout/istview_custom.xml 가져옴
        @SuppressLint("ViewHolder") View view = layoutInflater.inflate(R.layout.listview_traffic, null);
        TrafficDetail trafficDetail = trafficHistory.getTraffic(position);

        LocalDateTime time = trafficDetail.getTime();  // 업데이트 시각
        String name = trafficDetail.getAppLabel() + " (" + trafficDetail.getAppProcessName() + ")";  // 앱 이름
        int uid = trafficDetail.getUid();  // 앱 uid
        long usage = trafficDetail.getUsage();  // 앱 사용량
        long diff = trafficDetail.getDiff();  // 앱 트래픽 증가양

        TextView viewTime = view.findViewById(R.id.textViewTime);  // 시간
        TextView viewName = view.findViewById(R.id.textViewName);  // 앱 이름
        TextView viewUid = view.findViewById(R.id.textViewUid);  // 앱 uid
        TextView viewUsage = view.findViewById(R.id.textViewUsage);  // 트래픽 사용량

        // 리스트뷰에 히스토리 정보 세팅
        viewTime.setText(time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        viewName.setText(name);
        viewName.setTypeface(null, Typeface.BOLD);
        viewUid.setText("UID: " + uid);
        viewUsage.setText("Usage: " + usage + " (+" + diff + ") bytes");

        return view;
    }
}
