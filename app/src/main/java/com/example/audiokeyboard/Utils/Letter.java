package com.example.audiokeyboard.Utils;

public class Letter {
    char ch;
    long timeStamp;

    MotionPoint mPoint;

    public Letter(char ch) {
        this.mPoint = new MotionPoint(0, 0);
        this.ch = ch;
        timeStamp = System.currentTimeMillis();
        this.timeGap = 0;
    }
    public Letter(char ch, long t, long timeGap) {
        this.mPoint = new MotionPoint(0, 0);
        this.ch = ch;
        this.timeStamp = t;
        this.timeGap = timeGap;
    }

    public Letter(char ch, long timeGap) {
        this.mPoint = new MotionPoint(0, 0);
        this.ch = ch;
        this.timeStamp = System.currentTimeMillis();
        this.timeGap = timeGap;
    }

    public void setChar(char ch) {
        this.ch = ch;
        this.timeStamp = System.currentTimeMillis();
        this.timeGap = 0;
    }

    public void setTouchPoint(float x, float y) {
        this.mPoint.set(x, y);
    }

    public Letter(char ch, float x, float y) {
        mPoint = new MotionPoint(x, y);
        this.ch = ch;
        this.timeStamp = System.currentTimeMillis();
        this.timeGap = 0;
    }

    public Letter(char ch, long timeGap, float x, float y) {
        mPoint = new MotionPoint(x, y);
        this.ch = ch;
        this.timeGap = timeGap;
        this.timeStamp = System.currentTimeMillis();
    }

    public MotionPoint getTouchPoint() {
        return mPoint;
    }

    public char getChar() { return ch; }

    public void setCorrect(long c) { this.timeGap = c; }

    public long getTimeGap() { return this.timeGap; }

    long timeGap;

}
