package com.fos.anomalydetectionapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.Optional;

// 네트워크 히스토리 리스트뷰 어댑터
public class WhitelistAdapter extends BaseAdapter {

    WhitelistManager whitelistManager;  // 트래픽 히스토리 목록 인스턴스
    LayoutInflater layoutInflater;
    Activity activity;  // 메인 액티비티 컨텍스트
    boolean isInWhitelist;

    // Constructor
    public WhitelistAdapter(Activity activity, WhitelistManager whitelistManager) {
        this.activity = activity;
        this.whitelistManager = whitelistManager;
        layoutInflater = LayoutInflater.from(activity);
    }

    @Override
    public int getCount() {
        return whitelistManager.getLength();
    }

    @Override
    public AppDetail getItem(int position) {
        return whitelistManager.getAppDetail(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // 리스트뷰의 출력 방식 설정
    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // res/layout/.listview_whitelist.xml 가져옴
        @SuppressLint("ViewHolder") View view = layoutInflater.inflate(R.layout.listview_whitelist, null);
        AppDetail appDetail = whitelistManager.getAppDetail(position);

        String appName = Optional.ofNullable(appDetail.getAppLabel()).orElse("untitled");
        String appProcessName = Optional.ofNullable(appDetail.getAppProcessName()).orElse("untitled");
        int uid = appDetail.getUid();
        isInWhitelist = appDetail.getIsInWhitelist();

        TextView viewName = view.findViewById(R.id.appName);
        TextView viewProcessName = view.findViewById(R.id.appProcessName);
        TextView viewUid = view.findViewById(R.id.appUid);
        ImageButton button = view.findViewById(R.id.buttonAddWhitelist);

        // 리스트뷰에 값 세팅
        viewName.setText(appName);
        viewName.setTypeface(null, Typeface.BOLD);
        viewProcessName.setText(appProcessName);
        viewUid.setText("uid: " + uid);

        // 화이트리스트에 등록된 앱은 버튼 표시 변경
        if(isInWhitelist)
            button.setImageResource(R.drawable.check);

        // 버튼 누르면 앱을 화이트리스트에 추가 또는 해제
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isInWhitelist = !appDetail.getIsInWhitelist();
                whitelistManager.setAppDetail(position, isInWhitelist);
                button.setImageResource(isInWhitelist?R.drawable.check:R.drawable.plus);
            }
        });
        return view;
    }
}
