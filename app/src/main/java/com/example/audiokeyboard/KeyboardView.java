package com.example.audiokeyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.example.audiokeyboard.Utils.Key;
import com.example.audiokeyboard.Utils.Painters;

public class KeyboardView extends AppCompatImageView {

    static final String TAG = "KeyboardView";
    public KeyboardView(Context context) {
        super(context);
        init();
    }

    final int CURR_LAYOUT = Key.MODE_VIP;
    final int INIT_LAYOUT = Key.MODE_INIT;

    int[] allChar={0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,20,21,22,23,24,25,26};

    public KeyboardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
        init();
    }

    public KeyboardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    float screen_height_ratio;
    float screen_width_ratio;
    float baseImageHeight;
    float baseImageWidth;

    void init() {
        this.screen_height_ratio = 680f / 907f;
        this.screen_width_ratio = 1080f / 1440f;
        this.baseImageHeight = 680f;
        this.baseImageWidth = 1080f;
    }

    Painters painters = new Painters();
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawKeys(canvas);
    }

    void drawKeys(Canvas canvas) {
        canvas.drawLine(0, 0, 1000, 0, Painters.textPaint);

        for(int i=0;i<this.keys.length;i++) {
            RectF rect = new RectF(
                    this.keys[i].getLeft(CURR_LAYOUT)+5*screen_width_ratio,
                    this.keys[i].getTop(CURR_LAYOUT)+10*screen_height_ratio,
                    this.keys[i].getRight(CURR_LAYOUT)-5*screen_width_ratio,
                    this.keys[i].getBottom(CURR_LAYOUT)-10*screen_height_ratio);
            canvas.drawRoundRect(rect,10,10,painters.backgroundPaint);
        }

        for (int i:allChar){
            canvas.drawText(String.valueOf(this.keys[i].ch).toUpperCase(),
                    this.keys[i].curr_x,this.keys[i].curr_y-Painters.fonttop/2F-Painters.fontbottom/2F,
                    painters.textPaint);
        }

    }

    Key keys[];

    /**
     *
     * @param keys 更新之后的键盘位置
     * 更新现在的键盘位置之后更新
     */
    public void setKeysAndRefresh(Key[] keys) {
        this.keys = keys;
        invalidate();
    }

}