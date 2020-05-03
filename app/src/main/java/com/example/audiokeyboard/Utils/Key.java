package com.example.audiokeyboard.Utils;

import android.util.Log;

public class Key {
    public char ch;
    public float init_x;               // 对应的都是每一个方块的中心
    public float init_y;
    public float curr_x;
    public float curr_y;
    public float init_width;
    public float init_height;
    public float curr_width;
    public float curr_height;

    float tapRange = 0.8f;

    public static final int MODE_INIT = 0;
    public static final int MODE_VIP = 1;
    final String TAG = "Key Module";

    public Key() {
        this.ch = 'a';
        init_x = init_y = curr_x = curr_y = 0;
        init_height = init_width = curr_height = curr_width = 0;
    }
    public Key(char ch, float x, float y, float width, float height) {
        this.ch = ch;
        init_x = curr_x = x;
        init_y = curr_y = y;
        init_width = curr_width = width;
        init_height = curr_height = height;
    }

    public float getDist(float x, float y, int mode) {
        if(mode == MODE_INIT) {
            return (init_x-x)*(init_x-x)+(init_y-y)*(init_y-y);
        }
        if(mode == MODE_VIP) {
            return (curr_x-x)*(curr_x-x)+(curr_y-y)*(curr_y-y);
        }
        else {
            Log.e(TAG, "getDist: no mode found");
            return -1;
        }
    }

    public float getBottom(int mode) {
        if(mode == MODE_INIT) {
            return init_y + init_height / 2;
        }
        else if(mode == MODE_VIP) {
            return curr_y + curr_height / 2;
        }
        else {
            Log.e(TAG, "getBottom: no mode found");
            return -1;
        }
    }

    public float getTop(int mode){
        // mode==0 init_layout
        // mode==1 current_layout
        if(mode==MODE_INIT){
            return init_y-init_height/2F;
        }else if(mode== MODE_VIP){
            return curr_y-curr_height/2F;
        }else{
            Log.e(TAG, "getTop: no mode found");
            return -1;
        }
    }

    public float getLeft(int mode){
        // mode==0 init_layout
        // mode==1 current_layout
        if(mode==MODE_INIT){
            return init_x-init_width/2F;
        }else if(mode== MODE_VIP){
            return curr_x-curr_width/2F;
        }else{
            Log.e(TAG, "getLeft: no mode found");
            return -1;
        }
    }

    public float getRight(int mode){
        // mode==0 init_layout
        // mode==1 current_layout
        if(mode==MODE_INIT){
            return init_x+init_width/2F;
        }else if(mode== MODE_VIP){
            return curr_x+curr_width/2F;
        }else{
            Log.e(TAG, "getRight: no mode found");
            return -1;
        }
    }

    public boolean contain(float x, float y, int mode) {
        return x>=getLeft(mode)&&x<=getRight(mode)&&y>=getTop(mode)&&y<=getBottom(mode);
    }

    float getBottom_tap(int mode) {
        if(mode == MODE_INIT) return init_y + tapRange * init_height / 2f;
        else if(mode == MODE_VIP) return curr_y + tapRange * curr_height / 2f;
        else Log.e(TAG, "getBottom_tap: no mode found");
        return -1;
    }

    float getTop_tap(int mode) {
        if(mode == MODE_INIT) return init_y - tapRange * init_height / 2f;
        else if(mode == MODE_VIP) return curr_y - tapRange * curr_height / 2f;
        else Log.e(TAG, "getTop_tap: no mode found");
        return -1;
    }

    float getLeft_tap(int mode) {
        if(mode == MODE_INIT) return init_x - tapRange * init_width / 2f;
        else if(mode == MODE_VIP) return curr_x - tapRange * curr_width / 2f;
        else Log.e(TAG, "getLeft_tap: no mode found");
        return -1;
    }

    float getRight_tap(int mode) {
        if(mode == MODE_INIT) return init_x + tapRange * init_width / 2f;
        else if(mode == MODE_VIP) return curr_x + tapRange * curr_x / 2f;
        else Log.e(TAG, "getRight_tap: no mode found");
        return -1;
    }

    public void reset() {
        curr_x = init_x;
        curr_y = init_y;
        curr_width = init_width;
        curr_height = init_height;
    }

    public void resetX() {
        curr_x = init_x;
        curr_width = init_width;
    }

    public void resetY() {
        curr_y = init_y;
        curr_height = init_height;
    }

    /*
    1 2 3
    4 5 6
    7 8 9
     */
    public int containTap(float x, float y, int mode) {
        int[][] quadrant={{1,2,3},{4,5,6},{7,8,9}};
        int row=0;
        int col=0;
        if(x<getLeft_tap(mode)) col=0;
        else if(x>getRight_tap(mode)) col=2;
        else col=1;
        if(y<getTop_tap(mode)) row=0;
        else if(y>getBottom_tap(mode)) row=2;
        else row=1;
        return quadrant[row][col];
    }

    @Override
    public String toString() {
        return this.ch + " " + this.init_width + " " + this.init_height + " " + this.init_x + " " + this.init_y;
    }

}
