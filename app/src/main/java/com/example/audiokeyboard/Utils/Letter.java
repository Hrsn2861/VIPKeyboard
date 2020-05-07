package com.example.audiokeyboard.Utils;

public class Letter {
    char ch;
    long timeStamp;

    MotionPoint mPoint;

    public Letter(char ch) {
        this.mPoint = new MotionPoint(0, 0);
        this.ch = ch;
        timeStamp = System.currentTimeMillis();
        this.isCorrect = true;
    }
    public Letter(char ch, long t) {
        this.mPoint = new MotionPoint(0, 0);
        this.ch = ch;
        this.timeStamp = t;
        this.isCorrect = true;
    }

    public Letter(char ch, boolean isCorrect) {
        this.mPoint = new MotionPoint(0, 0);
        this.ch = ch;
        this.timeStamp = System.currentTimeMillis();
        this.isCorrect = isCorrect;
    }

    public void setChar(char ch) {
        this.ch = ch;
        this.timeStamp = System.currentTimeMillis();
        this.isCorrect = true;
    }

    public void setTouchPoint(float x, float y) {
        this.mPoint.set(x, y);
    }

    public Letter(char ch, float x, float y) {
        mPoint = new MotionPoint(x, y);
        this.ch = ch;
        this.timeStamp = System.currentTimeMillis();
        this.isCorrect = true;
    }

    public Letter(char ch, boolean isCorrect, float x, float y) {
        mPoint = new MotionPoint(x, y);
        this.ch = ch;
        this.isCorrect = isCorrect;
        this.timeStamp = System.currentTimeMillis();
    }

    public MotionPoint getTouchPoint() {
        return mPoint;
    }

    public char getChar() { return ch; }

    /**
     * @time 输入时间，default=System.currentTimeMillis()
     * @return 返回的是现在的时间到这个字符创建的时间
     */
    public long getTimeGap() { return getTimeGap(System.currentTimeMillis()); }
    public long getTimeGap(long time) { return Math.abs(time - this.timeStamp); }

    public void setCorrect(boolean c) { this.isCorrect = c; }

    public boolean isCorrect() { return this.isCorrect; }

    boolean isCorrect;

}
