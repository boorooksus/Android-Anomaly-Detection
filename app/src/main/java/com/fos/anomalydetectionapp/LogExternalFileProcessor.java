package com.fos.anomalydetectionapp;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// External Storage 파일 관리 class
public class LogExternalFileProcessor implements LogFileProcessor {

    // 외부 스토리지 권한 유무 체크 함수
    // 권한 있으면 true, 없으면 false 리턴
    @Override
    public boolean checkStoragePermission(Activity activity) {
        // 쓰기 권한 있는지 체크
        int permission = ActivityCompat.checkSelfPermission(
                activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        // 외부 스토리지 읽기, 쓰기 권한
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        int REQUEST_EXTERNAL_STORAGE = 1;

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 권한 없는 경우 권한 허용 설정 오픈
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );

            return false;
        }
        return true;
    }

    // 로그 파일 입력 함수
    // API 30 부터는 사용할 수 없음
    @Override
    public void writeLog(Activity activity, TrafficDetail trafficDetail){

        // 파일을 저장할 디렉토리
        File dir = new File(activity.getFilesDir(), "../../../../sdcard/TrafficMonitor");
        if(!dir.exists()){
            // 디렉토리가 없다면 생성
            if(dir.mkdirs()){
                Log.v("logFileProcessor", "Directory is created");
            }else{
                Log.v("logFileProcessor", "Directory is not created");
            }
        }
        // 로그 파일
        File file = new File(activity.getFilesDir(), "../../../../sdcard/TrafficMonitor/LogExternalFile.csv");

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