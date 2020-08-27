package com.example.audiokeyboard;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class OperationView extends View implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    GestureDetector detector;
    MainActivity mainActivity;


    public OperationView(Context context) {
        super(context);
        init();
    }

    public OperationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    public OperationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    private void init() {
        detector = new GestureDetector(getContext(), this);
        mainActivity = (MainActivity)getContext();
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        System.out.println("双击at" + e.getX());
        mainActivity.startStudy();
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        System.out.println("双击xx");
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        System.out.println("on long press!!!");
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }


}
