package com.example.audiokeyboard.Utils;

import android.util.Log;

public class MotionSeperator {

    public static double MIN_FLING_VELOCITY = 1.0;

    public static final int NULL_MOVE = 0;
    public static final int FLING_LEFT = 1;
    public static final int FLING_RIGHT = 2;
    public static final int FLING_UP = 3;
    public static final int FLING_DOWN = 4;

    public static final int NORMAL_MOVE = 5;

    public static final int DOUBLE_FLING_DOWN = 6;

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
            return dy > 0 ? FLING_DOWN : FLING_UP;
    }

    public static int getMotionType(MotionPoint s1, MotionPoint s2, MotionPoint e1, MotionPoint e2) {
        long t1 = s1.getTimeBetween(e1);
        long t2 = s2.getTimeBetween(e2);
        if(Math.max(t1, t2) > MIN_FLING_VELOCITY)
            return getDoubleFlingType(s1, s2, e1, e2);
        else
            return NORMAL_MOVE;
    }

    static int getDoubleFlingType(MotionPoint s1, MotionPoint s2, MotionPoint e1, MotionPoint e2) {
        double dx1 = e1.getDx(s1);
        double dx2 = e2.getDx(s2);
        double dy1 = e1.getDy(s1);
        double dy2 = e2.getDy(s2);
        if(getFlingType(s1, e1) == FLING_DOWN && getFlingType(s2, e2) == FLING_DOWN) {
            return DOUBLE_FLING_DOWN;
        }
        return NULL_MOVE;
    }

}
