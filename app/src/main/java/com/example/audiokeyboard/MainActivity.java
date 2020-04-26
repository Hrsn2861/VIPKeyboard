package com.example.audiokeyboard;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import com.example.audiokeyboard.Utils.DataRecorder;
import com.example.audiokeyboard.Utils.Key;
import com.example.audiokeyboard.Utils.KeyPos;
import com.example.audiokeyboard.Utils.Letter;
import com.example.audiokeyboard.Utils.MotionPoint;
import com.example.audiokeyboard.Utils.MotionSeperator;

public class MainActivity extends AppCompatActivity {

    final String TAG = "MainActivity";

    int getkey_mode;
    final int GETKEY_STRICT = 1;
    final int GETKEY_LOOSE = 0;

    final long MIN_TIME_GAP = 1000;
    final char KEY_NOT_FOUND = '*';

    int currMode;

    Letter currentChar;
    KeyboardView keyboardView;
    TextSpeaker textSpeaker;
    DataRecorder recorder;
    KeyPos keyPos;

    // all the text input, send to TextView
    String inputText = "";
    TextView textView;

    // record the edge pointer moved along
    MotionPoint startPoint = new MotionPoint(0, 0);
    MotionPoint endPoint =  new MotionPoint(0, 0);

    void defaultParams() {
        currMode = Key.MODE_INIT;
    }

    void initTts() {
        textSpeaker = new TextSpeaker(MainActivity.this);
    }

    void init() {
        keyPos = new KeyPos();
        keyboardView = (KeyboardView) (findViewById(R.id.keyboard));
        textView = (TextView) (findViewById(R.id.mytext));
        recorder = new DataRecorder();
        currentChar = new Letter('*');
        initTts();
        defaultParams();
        keyboardView.setKeysAndRefresh(keyPos.keys);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_relative);
        init();
    }

    void appendText(String s) {
        inputText = inputText+s;
        this.textView.setText(inputText);
    }
    String deleteLast() {
        char ch = ' ';
        if(!inputText.isEmpty()) {
            ch = inputText.charAt(inputText.length() - 1);
            inputText = inputText.substring(0, inputText.length() - 1);
        }
        else return "blank";
        this.textView.setText(inputText);
        return ch+"";
    }
    void refresh() {
        this.keyboardView.setKeysAndRefresh(keyPos.keys);
    }

    public void processTouchUp(float x, float y) {
        int moveType = MotionSeperator.getMotionType(startPoint, endPoint);
        switch (moveType) {
            case MotionSeperator.FLING_LEFT:                    // this means backspace
                recorder.removeLast();
                textSpeaker.speak(deleteLast()+" removed");
                currentChar.setChar(KEY_NOT_FOUND);
                Log.e(TAG, recorder.getDataAsString());
                break;
            case MotionSeperator.FLING_RIGHT:                   // this means word selected
                String s = recorder.getDataAsString();
                recorder.clear();
                currentChar.setChar(KEY_NOT_FOUND);
                textSpeaker.speak(s);
                appendText(" ");
                break;
            case MotionSeperator.FLING_DOWN:
            case MotionSeperator.FLING_UP:
            case MotionSeperator.NORMAL_MOVE:
            default:
                currentChar.setChar(keyPos.getKeyByPosition(x, y, currMode, getkey_mode));
                Log.e(TAG, currentChar.getChar()+" is up");
                recorder.add(currentChar.getChar());
                appendText(currentChar.getChar()+"");
                Log.e(TAG, recorder.getDataAsString());
        }
    }

    public void processTouchDown(float x ,float y){
        this.textSpeaker.stop();
        currentChar.setChar(keyPos.getKeyByPosition(x, y, currMode, getkey_mode));
        textSpeaker.speak(currentChar.getChar()+"");
        if(currentChar.getChar() != '*') { refresh(); }
    }

    public void processTouchMove(float x, float y) {
        this.textSpeaker.stop();
    }

    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY()-keyPos.wholewindowSize+keyPos.partialwindowSize;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                startPoint.set(x, y);
                processTouchDown(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                processTouchMove(x, y);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                endPoint.set(x, y);
                processTouchUp(x, y);
                break;
        }
        return super.onTouchEvent(event);
    }

}
