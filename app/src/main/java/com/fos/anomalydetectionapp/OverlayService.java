package com.fos.anomalydetectionapp;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

public class OverlayService extends Service {

    WindowManager wm;
    View mView;
    EventManagement eventManagement = new EventManagement();

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @SuppressLint({"RtlHardcoded", "InflateParams"})
    @Override
    public void onCreate() {
        super.onCreate();

        LayoutInflater inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);



        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                /*ViewGroup.LayoutParams.MATCH_PARENT*/300,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                30, 30, // X, Y 좌표
                TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.LEFT | Gravity.BOTTOM;

        mView = inflate.inflate(R.layout.overlay_view, null);

        mView.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int posX = (int)event.getX();
                int posY = (int)event.getY();

//                Log.v("OverlayService", "TOUCH EVENT OCCURRED! (" + posX + ", " + posY + ")");

                eventManagement.addTouchEvent();

                return true;
            }
        });

        wm.addView(mView, params);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(wm != null) {
            if(mView != null) {
                wm.removeView(mView);
                mView = null;
            }
            wm = null;
        }
    }
}
