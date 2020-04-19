package com.example.audiokeyboard;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MotionEvent;

import com.example.audiokeyboard.Utils.DataRecorder;
import com.example.audiokeyboard.Utils.Key;
import com.example.audiokeyboard.Utils.Letter;

public class MainActivity extends AppCompatActivity {

    int getkey_mode;
    final int GETKEY_STRICT = 1;
    final int GETKEY_LOOSE = 0;

    final long MIN_TIME_GAP = 1000;

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
    final int[] allChar={0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,20,21,22,23,24,25,26};
    final int[] keyPos={10,24,22,12,2,13,14,15,7,16,17,18,26,25,8,9,0,3,11,4,6,23,1,21,5,20};

    final char KEY_NOT_FOUND = '*';
    final char shiftCh=KEY_NOT_FOUND;
    final char symbolCh=KEY_NOT_FOUND;
    final char languageCh=KEY_NOT_FOUND;
    final char spaceCh=KEY_NOT_FOUND;
    final char commaCh=KEY_NOT_FOUND;
    final char periodCh=KEY_NOT_FOUND;
    final char backspaceCh=KEY_NOT_FOUND;

    final int INIT_LAYOUT = Key.MODE_INIT;
    final int VIP_LAYOUT = Key.MODE_VIP;
    int curr_layout;

    Letter currentChar;
    KeyboardView keyboardView;
    TextSpeaker textSpeaker;
    DataRecorder recorder;
    MyGestureDetector gestureDetector;

    void defaultParams() {
        screen_height_ratio = keyboardHeight / 907f;
        screen_width_ratio = keyboardWidth / 1440f;
        baseImageHeight = keyboardHeight;
        baseImageWidth = keyboardWidth;
        topThreshold = 0 * screen_height_ratio;
        bottomThreshold = 907 * screen_height_ratio;
        curr_layout = INIT_LAYOUT;
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
        recorder = new DataRecorder();
        currentChar = new Letter('*');
        initTts();
        defaultParams();
        initKeys();
        keyboardView.setKeysAndRefresh(keys);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_relative);
        init();
    }

    public char getKeyPosition(float x, float y, int mode) {
        char key = KEY_NOT_FOUND;
        if(y < topThreshold || y > bottomThreshold) return key;
        float min_dist = Float.MAX_VALUE;
        for(int i=0;i<KEYNUM;i++) {
            if(keys[i].getDist(x, y, mode) < min_dist) {
                key = keys[i].ch;
                min_dist = keys[i].getDist(x, y, mode);
            }
        }

        if(getkey_mode == GETKEY_LOOSE) {
            return key;
        }
        else if(key != KEY_NOT_FOUND && getkey_mode == GETKEY_STRICT){
            int pos = this.keyPos[key-'a'];
            if(keys[pos].contain(x, y, mode)) return key;
            else return KEY_NOT_FOUND;
        }
        return key;
    }

    void refresh() {
        this.keyboardView.setKeysAndRefresh(keys);
    }

    public void processTouchDown(float x ,float y){
        this.textSpeaker.stop();
        currentChar.setChar(getKeyPosition(x, y, curr_layout));
        textSpeaker.speak(currentChar.getChar()+"");
        if(currentChar.getChar() != '*') { refresh(); }
    }

    public void processTouchUp() {
        recorder.add(currentChar);
        if(currentChar.getTimeGap() > MIN_TIME_GAP) {
            textSpeaker.speak(currentChar.getChar()+"");
        }
    }

    public void processTouchMove(float x, float y) {
        this.textSpeaker.stop();
        currentChar.setChar(getKeyPosition(x, y, curr_layout));
    }

    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY() - this.getWindowManager().getDefaultDisplay().getHeight() + keyboardHeight;
        if(event.getActionMasked() == MotionEvent.ACTION_DOWN) { processTouchDown(x,y); }
        else if(event.getActionMasked() == MotionEvent.ACTION_UP) { processTouchUp(); }
        else if(event.getActionMasked() == MotionEvent.ACTION_MOVE) { processTouchMove(x, y); }
        return true;
    }

}
