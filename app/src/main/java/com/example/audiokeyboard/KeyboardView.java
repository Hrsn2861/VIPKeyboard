package com.example.audiokeyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.example.audiokeyboard.Utils.Key;
import com.example.audiokeyboard.Utils.Painters;

public class KeyboardView extends AppCompatImageView {

    public KeyboardView(Context context) {
        super(context);
        init();
    }

    final int CURR_LAYOUT = Key.MODE_RELATIVE;
    final int INIT_LAYOUT = Key.MODE_INIT;

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
        for(int i=0;i<this.keys.length;i++) {
            RectF rect = new RectF(this.keys[i].getLeft(CURR_LAYOUT)+5*screen_width_ratio,
                    this.keys[i].getTop(CURR_LAYOUT)+10*screen_height_ratio,
                    this.keys[i].getRight(CURR_LAYOUT)-5*screen_width_ratio,
                    this.keys[i].getBottom(CURR_LAYOUT)-10*screen_height_ratio);
            canvas.drawRoundRect(rect,10,10,painters.backgroundPaint);
        }
    }

    Key keys[];

    public void setKeys(Key[] keys) {
        this.keys = keys;
        invalidate();
    }

}