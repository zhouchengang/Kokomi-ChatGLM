package com.zhouchengang.chatglmsample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Create by oy 2017/9/8 9:45.
 * <p>
 * 仿微信按钮
 */

public class WechatClickButton extends View {
    private int colorOn = Color.parseColor("#11AA11");
    private int colorOff = Color.GRAY;
    private int colorButton = Color.WHITE;
    private int center;
    private int rec_x;
    private Paint paint;
    private int measuredWidth;
    private int measuredHeight;
    private int smallCenter;
    private float smallCenter_x;
    private Paint smallPaint;
    private float startx;
    private float endx;
    private int mid_x;

    public WechatClickButton(Context context) {
        this(context, null);
    }

    public WechatClickButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WechatClickButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private OnMClickListener onClickListener;

    public void setOnMbClickListener(OnMClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public interface OnMClickListener {
        void onClick(boolean isRight);
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        smallPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(colorOn);
        smallPaint.setColor(colorButton);
    }


    public static final int TO_LEFT = 11;
    public static final int TO_RIGHT = 22;
    private boolean isRight = true;
    private boolean isAnimate = false;
    Handler handler = new Handler() {
        @Override

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TO_LEFT:
                    paint.setColor(colorOff);
                    if (smallCenter_x > center) {
                        smallCenter_x -= 5;
                        handler.sendEmptyMessageDelayed(TO_LEFT, 1);
                        isAnimate = true;
                    } else {
                        smallCenter_x = center;
                        setEnabled(true);
                        isAnimate = false;
                    }
                    break;

                case TO_RIGHT:
                    paint.setColor(colorOn);
                    if (smallCenter_x < rec_x) {
                        smallCenter_x += 5;
                        handler.sendEmptyMessageDelayed(TO_RIGHT, 1);
                        isAnimate = true;
                    } else {
                        smallCenter_x = rec_x;
                        setEnabled(true);
                        isAnimate = false;
                    }
                    break;
            }
            invalidate();
        }

    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measuredHeight = getMeasuredHeight();
        measuredWidth = getMeasuredWidth();
        center = measuredHeight / 2;
        rec_x = measuredWidth - center;
        smallCenter = center - 5;
        smallCenter_x = rec_x;
    }

    @Override

    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(center, center, center, paint);
        canvas.drawRect(center, 0, rec_x, measuredHeight, paint);
        canvas.drawCircle(rec_x, center, center, paint);
        canvas.drawCircle(smallCenter_x, center, smallCenter, smallPaint);
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startx = event.getX();
                endx = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                float distance = event.getX() - endx;
                smallCenter_x += distance;
                if (smallCenter_x > rec_x) {
                    isRight = true;
                    smallCenter_x = rec_x;
                    paint.setColor(colorOn);
                } else if (smallCenter_x < center) {
                    smallCenter_x = center;
                    isRight = false;
                    paint.setColor(colorOff);
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                float up_x = event.getX();
                if (Math.abs(up_x - Math.abs(startx)) < 2) {
                    if (!isAnimate) {
                        startGO();
                    }
                } else {
                    mid_x = (center + (rec_x - center) / 2);
                    if (smallCenter_x < mid_x) {
                        isRight = false;
                        smallCenter_x = center;
                        paint.setColor(colorOff);
                        setEnabled(true);
                        invalidate();
                    } else {
                        isRight = true;
                        smallCenter_x = rec_x;
                        paint.setColor(colorOn);
                        setEnabled(true);
                        invalidate();
                    }
                }
                if (smallCenter_x == rec_x || smallCenter_x == center) {
                    if (onClickListener != null) {
                        onClickListener.onClick(isRight);
                    }
                }
                break;
        }
        return true;
    }

    private void goLeft() {
        isRight = false;
        handler.sendEmptyMessageDelayed(TO_LEFT, 40);
    }

    private void goRight() {
        isRight = true;
        handler.sendEmptyMessageDelayed(TO_RIGHT, 40);
    }

    public void startGO() {
        if (isRight) {
            goLeft();
        } else {
            goRight();
        }
    }

    public void setDirection(boolean enable) {
        this.isRight = !enable;
        startGO();
    }

}