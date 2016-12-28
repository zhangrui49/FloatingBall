package com.zhangrui.floatingball;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;

/**
 * DESC:
 * Created by zhangrui on 2016/12/27.
 */

public class FloatingBall extends View {

    private boolean isTouching = false;
    private boolean mIsLongTouch = false;
    private MODE mode;
    private final int TIME_LONG_CLICK_LIMIT = 3000;
    private final int TIME_CLICK_LIMIT = 500;
    private long lastTouchTime;
    private Paint mPaint;
    private int circleWidth = 20;
    private int mRadius;
    private float mLastDownX;
    private float mLastDownY;
    private OnFloatEvent OnFloatEvent;

    public FloatingBall.OnFloatEvent getOnFloatEvent() {
        return OnFloatEvent;
    }

    private AccessibilityService mAccessibilityService;
    private Vibrator mVibrator;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;

    public void setOnFloatEvent(FloatingBall.OnFloatEvent onFloatEvent) {
        OnFloatEvent = onFloatEvent;
    }

    enum MODE {

        MOVE, CLICK, UP, DOWN, RIGHT, LEFT, COMMON
    }

    public FloatingBall(Context context) {
        this(context, null);

    }

    public FloatingBall(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingBall(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mode = MODE.COMMON;
        mPaint = new Paint();
        mAccessibilityService = (AccessibilityService) context;
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams();
        setAlpha();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (!isLongTouch() && isTouchSlop(event)) {
                    return true;
                }
                if (mIsLongTouch && (mode == MODE.COMMON || mode == MODE.MOVE)) {
                    mLayoutParams=(WindowManager.LayoutParams)getLayoutParams();
                    mLayoutParams.x = (int) (event.getRawX() - dip2px(25));
                    mLayoutParams.y = (int) (event.getRawY() - dip2px(25)-46);
                    mWindowManager.updateViewLayout(FloatingBall.this, mLayoutParams);
                    mode = MODE.MOVE;
                    Log.e("xy","x:"+mLayoutParams.x+" "+"Y:"+mLastDownY);
                } else {
                    checkEvent(event);
                }


                break;

            case MotionEvent.ACTION_DOWN:
                lastTouchTime = System.currentTimeMillis();
                mLastDownX = event.getX();
                mLastDownY = event.getY();
                isTouching = true;
                showTouchAnimation();
                break;

            case MotionEvent.ACTION_UP:

                isTouching = false;
                mIsLongTouch=false;
                init();
                if (isLongTouch()) {
                    mIsLongTouch = false;
                } else if (isClick(event)) {
                    if (OnFloatEvent == null) {
                        back();
                    } else {
                        OnFloatEvent.onClick();
                    }

                } else {
                    checkAction();
                }
                mode = MODE.COMMON;
                break;
            case MotionEvent.ACTION_CANCEL:
                mIsLongTouch=false;
                isTouching = false;
                init();

                break;
        }

        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        setMeasuredDimension(measureWidth(widthMeasureSpec), measureWidth(heightMeasureSpec));
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int center = getWidth() / 2;
        mPaint.setColor(Color.GRAY);
        mPaint.setStyle(Paint.Style.STROKE);
        mRadius = center - circleWidth / 2;
        mPaint.setStrokeWidth(circleWidth);
        canvas.drawCircle(center, center, mRadius, mPaint);
        mPaint.setColor(Color.LTGRAY);
        mPaint.setStyle(Paint.Style.FILL);
        // canvas.drawCircle(center,center,mRadius-circleWidth/2,mPaint);
        canvas.drawCircle(center, center, mRadius - circleWidth, mPaint);
        super.onDraw(canvas);
    }

    public boolean isLongTouch() {
        long time = System.currentTimeMillis();
        return isTouching && mode == MODE.MOVE && (time - lastTouchTime >= TIME_LONG_CLICK_LIMIT);
    }

    private boolean isClick(MotionEvent event) {
        float offsetX = Math.abs(event.getX() - mLastDownX);
        float offsetY = Math.abs(event.getY() - mLastDownY);
        long time = System.currentTimeMillis() - lastTouchTime;

        if (offsetX < ViewConfiguration.getTouchSlop() * 2 && offsetY < ViewConfiguration.getTouchSlop() * 2 && time < TIME_CLICK_LIMIT) {
            return true;
        } else {
            return false;
        }
    }

    private void checkAction() {
        switch (mode) {
            case LEFT:
                if (OnFloatEvent == null) {

                } else {
                    OnFloatEvent.onLeft();
                }

                break;
            case RIGHT:
                if (OnFloatEvent == null) {
                    right();
                } else {
                    OnFloatEvent.onRignt();
                }

                break;
            case DOWN:
                if (OnFloatEvent == null) {
                    down();
                } else {
                    OnFloatEvent.onDown();
                }

                break;
            case UP:
                if (OnFloatEvent == null) {
                    up();
                } else {
                    OnFloatEvent.onUp();
                }
                break;

        }
    }

    private int measureWidth(int widthMeasureSpec) {
        int result;
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec);
        if (mode == MeasureSpec.EXACTLY) {//设置固定值
            result = size;
        } else {
            result = 160;
            if (mode == MeasureSpec.AT_MOST) {//设为wrap_content
                result = Math.min(result, size);
            }
        }
        return result;
    }

    private void showTouchAnimation() {
        new Thread() {
            @Override
            public void run() {
                while (isTouching && circleWidth != 0) {
                    circleWidth--;
                    postInvalidate();
                    try {
                        Thread.sleep(25);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (circleWidth == 1) {
                        mVibrator.vibrate(new long[]{0, 100}, -1);
                        mIsLongTouch = true;
                        mode=MODE.MOVE;
                    }
                }
            }
        }.start();
    }

    private void init() {

        circleWidth = 20;
        postInvalidate();
    }

    private void checkEvent(MotionEvent event) {
        float offsetX = event.getX() - mLastDownX;
        float offsetY = event.getY() - mLastDownY;
        if (isTouchSlop(event)) {
            return;
        }
        if (Math.abs(offsetX) > Math.abs(offsetY)) {
            if (offsetX > 0) {
                if (mode == MODE.RIGHT) {
                    return;
                }
                mode = MODE.RIGHT;
            } else {
                if (mode == MODE.LEFT) {
                    return;
                }
                mode = MODE.LEFT;
            }
        } else {
            if (offsetY > 0) {
                if (mode == MODE.DOWN) {
                    return;
                }
                mode = MODE.DOWN;

            } else {
                if (mode == MODE.UP) {
                    return;
                }
                mode = MODE.UP;
            }
        }
    }

    private boolean isTouchSlop(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (Math.abs(x - mLastDownX) < ViewConfiguration.getTouchSlop() && Math.abs(y - mLastDownY) < ViewConfiguration.getTouchSlop()) {
            return true;
        }
        return false;
    }

    public interface OnFloatEvent {
        void onLeft();

        void onRignt();

        void onUp();

        void onDown();

        void onClick();
    }

    private void up() {
        mAccessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
    }

    private void down() {
        mAccessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS);
    }

    private void left() {
        mAccessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
    }

    private void right() {
        mAccessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
    }

    private void back() {
        mAccessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    public int dip2px(float dip) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dip, getContext().getResources().getDisplayMetrics()
        );
    }

    private void setAlpha(){
        setAlpha(0.6f);
    }
}
