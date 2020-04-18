package com.example.audiokeyboard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.example.audiokeyboard.Utils.Key;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    final int KEYNUM = 33;
    final String alphabet = "qwertyuiopasdfghjkl zxcvbnm       ";
    public Key keys[];
    final float keyboardWidth = 1080f;
    final float keyboardHeight = 680f;
    float bottomThreshold;
    float topThreshold;
    float screen_height_ratio = 1f;
    float screen_width_ratio = 1f;
    float baseImageHeight;
    float baseImageWidth;

    final int Q=0;
    final int W=1;
    final int O=8;
    final int P=9;
    final int A=10;
    final int S=11;
    final int K=17;
    final int L=18;
    final int SHIFT=19;
    final int Z=20;
    final int X=21;
    final int N=25;
    final int M=26;
    final int BACKSPACE=27;
    final int SYMBOL=28;
    final int LANGUAGE=29;
    final int SPACE=30;
    final int COMMA=31;
    final int PERIOD=32;
    int[] allChar={0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,20,21,22,23,24,25,26};

    final char KEY_NOT_FOUND = '*';
    final char shiftCh=KEY_NOT_FOUND;
    final char symbolCh=KEY_NOT_FOUND;
    final char languageCh=KEY_NOT_FOUND;
    final char spaceCh=KEY_NOT_FOUND;
    final char commaCh=KEY_NOT_FOUND;
    final char periodCh=KEY_NOT_FOUND;
    final char backspaceCh=KEY_NOT_FOUND;

    final int INIT_LAYOUT = Key.MODE_INIT;
    final int CURR_LAYOUT = Key.MODE_RELATIVE;

    KeyboardView keyboardView;
    TextSpeaker textSpeaker;

    void defaultParams() {
        screen_height_ratio = keyboardHeight / 907f;
        screen_width_ratio = keyboardWidth / 1440f;
        baseImageHeight = keyboardHeight;
        baseImageWidth = keyboardHeight;
        topThreshold = 0 * screen_height_ratio;
        bottomThreshold = 907 * screen_height_ratio;
    }

    void initKeys() {
        keys = new Key[KEYNUM];
        for(int i=0;i<KEYNUM;i++) {
            keys[i] = new Key();
        }
        for (int i=Q;i<=P;i++){
            this.keys[i].init_x=this.keyboardWidth*(2*i+1)/20F;
            this.keys[i].init_y=this.keyboardHeight/8F+(this.bottomThreshold-this.keyboardHeight);
        }
        for (int i=A;i<=L;i++){
            this.keys[i].init_x=(this.keys[i-(A-Q)].init_x+this.keys[i-(A-W)].init_x)/2F;
            this.keys[i].init_y=this.keyboardHeight*3F/8F+(this.bottomThreshold-this.keyboardHeight);
        }
        for (int i=Z;i<=M;i++){
            this.keys[i].init_x=this.keys[i-(Z-S)].init_x;
            this.keys[i].init_y=this.keyboardHeight*5F/8F+(this.bottomThreshold-this.keyboardHeight);
        }
        for (int i:allChar) {
            this.keys[i].init_height=this.keyboardHeight/4F;
            this.keys[i].init_width=this.keyboardWidth/10F;
        }

        // SHIFT
        this.keys[SHIFT].init_height=this.keyboardHeight/4F;
        this.keys[SHIFT].init_width=this.keys[Z].getLeft(INIT_LAYOUT);
        this.keys[SHIFT].init_x=this.keys[SHIFT].init_width/2F;
        this.keys[SHIFT].init_y=this.keys[Z].init_y;

        // BACKSPACE
        this.keys[BACKSPACE].init_height=this.keyboardHeight/4F;
        this.keys[BACKSPACE].init_width=this.keyboardWidth-this.keys[M].getRight(INIT_LAYOUT);
        this.keys[BACKSPACE].init_x=this.keys[M].getRight(INIT_LAYOUT)+this.keys[BACKSPACE].init_width/2F;
        this.keys[BACKSPACE].init_y=this.keys[M].init_y;

        // SYMBOL
        this.keys[SYMBOL].init_height=this.keyboardHeight/4F;
        this.keys[SYMBOL].init_width=this.keys[SHIFT].init_width;
        this.keys[SYMBOL].init_x=this.keys[SHIFT].init_x;
        this.keys[SYMBOL].init_y=this.keys[SHIFT].getBottom(INIT_LAYOUT)+this.keys[SYMBOL].init_height/2F;

        // LANGUAGE
        this.keys[LANGUAGE].init_height=this.keyboardHeight/4F;
        this.keys[LANGUAGE].init_width=this.keys[SYMBOL].init_width;
        this.keys[LANGUAGE].init_x=this.keys[SYMBOL].getRight(INIT_LAYOUT)+this.keys[LANGUAGE].init_width/2F;
        this.keys[LANGUAGE].init_y=this.keys[SYMBOL].init_y;

        // PERIOD
        this.keys[PERIOD].init_height=this.keyboardHeight/4F;
        this.keys[PERIOD].init_width=this.keys[BACKSPACE].init_width;
        this.keys[PERIOD].init_x=this.keys[BACKSPACE].init_x;
        this.keys[PERIOD].init_y=this.keys[SYMBOL].init_y;

        // COMMA
        this.keys[COMMA].init_height=this.keyboardHeight/4F;
        this.keys[COMMA].init_width=this.keys[BACKSPACE].init_width;
        this.keys[COMMA].init_x=this.keys[PERIOD].getLeft(INIT_LAYOUT)-this.keys[COMMA].init_width/2F;
        this.keys[COMMA].init_y=this.keys[SYMBOL].init_y;

        // SPACE
        this.keys[SPACE].init_height=this.keyboardHeight/4F;
        this.keys[SPACE].init_width=this.keys[COMMA].getLeft(INIT_LAYOUT)-this.keys[LANGUAGE].getRight(INIT_LAYOUT);
        this.keys[SPACE].init_x=this.keys[LANGUAGE].getRight(INIT_LAYOUT)+this.keys[SPACE].init_width/2F;
        this.keys[SPACE].init_y=this.keys[SYMBOL].init_y;

        for(int i=0;i<KEYNUM;i++) {
            keys[i].reset();
            keys[i].ch = alphabet.charAt(i);
        }

    }

    void initTts() {
        textSpeaker = new TextSpeaker(MainActivity.this);
    }

    void init() {
        keyboardView = (KeyboardView) (findViewById(R.id.keyboard));
        textSpeaker = new TextSpeaker(MainActivity.this);
        defaultParams();
        initKeys();
        keyboardView.setKeys(keys);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_relative);
        init();

        Log.e("peech is voer", "Speech is over");
    }

    public boolean onTouchEvent(MotionEvent event) {
        if(event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            Log.e("-------------", "touch down");
            textSpeaker.speak("touch");
        }
        return true;
    }

}
