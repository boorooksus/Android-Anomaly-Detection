package com.fos.anomalydetectionapp;

import android.app.Activity;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


// 내부 스토리지 로그 파일 관리 클래스
public class LogInternalFileProcessor implements LogFileProcessor{

    // 스토리지 권한 유무 체크 함수
    // 항상 true 리턴. internal 스토리지는 권한이 따로 필요하지 않음
    @Override
    public boolean checkStoragePermission(Activity activity) {
        return true;
    }

    // 로그 파일 입력 함수
    @Override
    public void writeLog(Activity activity, TrafficDetail trafficDetail){
        // 로그 파일
        File file = new File(activity.getFilesDir(), "LogInternalFile.csv");

        if(!file.exists()){
            // 파일이 존재하지 않는 경우, 파일 생성 후 항목들 title 작성
            try {
                FileWriter fw = new FileWriter( file.getAbsoluteFile() ,true);
                BufferedWriter bw = new BufferedWriter( fw );

                // 각 항목들의 타이틀
                String[] titles = new String[]{"time", "uid", "usage", "increase", "app label", "app name"};

                for (String title: titles){
                    bw.write(title);
                    bw.write(",");
                }

                bw.newLine();
                bw.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 파일에 로그 쓰기
        try {
            FileWriter fw = new FileWriter( file.getAbsoluteFile() ,true);
            BufferedWriter bw = new BufferedWriter( fw );

            LocalDateTime time = trafficDetail.getTime();  // 업데이트 시각
            String name = trafficDetail.getAppLabel();
            String processName = trafficDetail.getAppProcessName();  // 앱 이름
            int uid = trafficDetail.getUid();  // 앱 uid
            long usage = trafficDetail.getUsage();  // 앱 사용량
            long diff = trafficDetail.getDiff();  // 앱 트래픽 증가양

            String data = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            data += "," + uid + "," + usage + "," + diff + "," + name + "," + processName;
            Log.v("", data);

            bw.write(data);
            bw.newLine();
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
