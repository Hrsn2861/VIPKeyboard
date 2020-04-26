package com.example.audiokeyboard.Utils;

public class Letter {
    char ch;
    long timeStamp;
    public Letter(char ch) {
        this.ch = ch;
        timeStamp = System.currentTimeMillis();
    }
    public Letter(char ch, long t) {
        this.ch = ch;
        this.timeStamp = t;
    }

    public void setChar(char ch) {
        this.ch = ch;
        this.timeStamp = System.currentTimeMillis();
    }

    public char getChar() { return ch; }

    /**
     * @time 输入时间，default=System.currentTimeMillis()
     * @return 返回的是现在的时间到这个字符创建的时间
     */
    public long getTimeGap() { return getTimeGap(System.currentTimeMillis()); }
    public long getTimeGap(long time) { return Math.abs(time - this.timeStamp); }

}
