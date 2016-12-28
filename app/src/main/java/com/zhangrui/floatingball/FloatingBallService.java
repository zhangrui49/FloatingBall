package com.zhangrui.floatingball;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

/**
 * DESC:
 * Created by zhangrui on 2016/12/27.
 */

public class FloatingBallService extends AccessibilityService {
    public static final int ACTION_SHOW = 0;
    public static final int ACTION_HIDE = 1;

    private FloatingBall mFloatingBall;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle data = intent.getExtras();
        if (data != null) {
            int action = data.getInt("action");
            if (action == ACTION_SHOW) {
                show();
            } else {
                hide();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
//
//    @Override
//    public void onStart(Intent intent, int startId) {
//        super.onStart(intent, startId);
//    }

    public void show() {
        if (mFloatingBall == null) {
            WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            int screenWidth = windowManager.getDefaultDisplay().getWidth();
            int screenHeight = windowManager.getDefaultDisplay().getHeight();
            mFloatingBall = new FloatingBall(this);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.x = screenWidth;
            params.y = screenHeight / 2;
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.LEFT | Gravity.TOP;
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
            params.format = PixelFormat.RGBA_8888;
            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            mFloatingBall.setLayoutParams(params);
            windowManager.addView(mFloatingBall, params);
        }
    }

    public void hide() {
        if (mFloatingBall != null) {
            WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            windowManager.removeView(mFloatingBall);
            mFloatingBall = null;
        }
    }
}
