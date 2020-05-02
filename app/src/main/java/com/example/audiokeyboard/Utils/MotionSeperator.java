package com.example.audiokeyboard.Utils;

import android.util.Log;

public class MotionSeperator {

    public static int MIN_FLING_VELOCITY = 2;

    public static final int FLING_LEFT = 1;
    public static final int FLING_RIGHT = 2;
    public static final int FLING_UP = 3;
    public static final int FLING_DOWN = 4;

    public static final int NORMAL_MOVE = 5;

    public static int getMotionType(MotionPoint p1, MotionPoint p2) {
        long timeGap = p1.getTimeBetween(p2);
        double dist = p1.getDistance(p2);
        double velocity = dist / timeGap;
        if(velocity > MIN_FLING_VELOCITY)
            return getFlingType(p1, p2);
        else
            return NORMAL_MOVE;
    }

    static int getFlingType(MotionPoint p1, MotionPoint p2) {
        float dx = p2.x - p1.x;
        float dy = p2.y - p1.y;
        if(Math.abs(dx) > Math.abs(dy))
            return dx > 0 ? FLING_RIGHT : FLING_LEFT;
        else
            return dy > 0 ? FLING_UP : FLING_DOWN;
    }

}
