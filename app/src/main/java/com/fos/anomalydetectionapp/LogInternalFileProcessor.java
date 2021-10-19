package com.fos.anomalydetectionapp;

import android.app.Activity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


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
    public void writeLog(Activity activity, String log){
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

            bw.write(log);
            bw.newLine();
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
