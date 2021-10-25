package com.fos.anomalydetectionapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Optional;

// 네트워크 히스토리 리스트뷰 어댑터
public class WhitelistAdapter extends BaseAdapter {

    AppsManager appsManager;  // 트래픽 히스토리 목록 인스턴스
    LayoutInflater layoutInflater;
    Activity activity;  // 메인 액티비티 컨텍스트
    boolean isInWhitelist;

    // Constructor
    public WhitelistAdapter(Activity activity, AppsManager appsManager) {
        this.activity = activity;
        this.appsManager = appsManager;
        layoutInflater = LayoutInflater.from(activity);
    }

    @Override
    public int getCount() {
        return appsManager.getLength();
    }

    // 수정 필요
    @Override
    public AppDetail getItem(int position) {
        return appsManager.getAppDetail(position);
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
        @SuppressLint("ViewHolder") View view = layoutInflater.inflate(R.layout.listview_whitelist, null);
//        TrafficDetail trafficDetail = trafficHistory.getTraffic(position);
        AppDetail appDetail = appsManager.getAppDetail(position);

        String appName = Optional.ofNullable(appDetail.getAppLabel()).orElse("untitled");
        String appProcessName = Optional.ofNullable(appDetail.getAppProcessName()).orElse("untitled");
        isInWhitelist = appDetail.getIsInWhitelist();

        TextView viewName = view.findViewById(R.id.appName);
        TextView viewProcessName = view.findViewById(R.id.appProcessName);
        ImageButton button = view.findViewById(R.id.buttonAddWhitelist);

        viewName.setText(appName);
        viewProcessName.setText(appProcessName);

        if(isInWhitelist)
            button.setImageResource(R.drawable.check);

//        button.setText(isInWhitelist + "");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                isInWhitelist = !appDetail.getIsInWhitelist();

                appsManager.setAppDetail(position, isInWhitelist);

                if(isInWhitelist)
                    button.setImageResource(R.drawable.check);
                else
                    button.setImageResource(R.drawable.plus);
            }
        });

        return view;
    }


}
