package com.example.audiokeyboard.Utils;

public class MotionPoint {
    float x;
    float y;
    long time;
    public MotionPoint(float x, float y) {
        this.x = x;
        this.y = y;
        time = System.currentTimeMillis();
    }

    public long getTimeBetween(MotionPoint mp) {
        return Math.abs(this.time - mp.time);
    }

    public float getDx(MotionPoint mp) { return this.x - mp.x; }
    public float getDy(MotionPoint mp) { return this.y - mp.y; }

    public double getDistance(MotionPoint mp) {
        return Math.sqrt(getDx(mp)*getDx(mp)+getDy(mp)*getDy(mp));
    }

    public long getTime() { return this.time; }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
        this.time = System.currentTimeMillis();
    }
}
