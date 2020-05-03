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
import com.example.audiokeyboard.Utils.Word;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    final String TAG = "MainActivity";

    int getkey_mode;
    final int GETKEY_STRICT = 1;
    final int GETKEY_LOOSE = 0;

    final long MIN_TIME_GAP = 1000;
    final char KEY_NOT_FOUND = '*';

    final double minVelocityToStopSpeaking = 0.2;
    final double maxVelocityToDetermineHover = 0.1;
    final long minTimeGapThreshold = 500;               // 如果大于这个时间长度说明key是确定的；

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
    MotionPoint currPoint = new MotionPoint(0, 0);
    char currMoveCharacter = KEY_NOT_FOUND;

    final int DICT_SIZE = 50000;

    Predictor predictor;

    void defaultParams() {
        currMode = Key.MODE_VIP;
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
        initPredictor();
        initDict();
    }

    void initPredictor() {
        predictor = new Predictor(this.keyPos);
    }

    void initDict() {
        Log.i("init", "start loading dict_eng");
        BufferedReader reader = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.dict_eng)));
        String line;
        try{
            int lineNo = 0;
            while ((line = reader.readLine()) != null){
                lineNo++;
                String[] ss = line.split(" ");
                predictor.dictEng.add(new Word(ss[0], Double.valueOf(ss[1])));
                if (lineNo == DICT_SIZE)
                    break;
            }
            reader.close();
            Log.e("init", "read dict_eng finished " + predictor.dictEng.size());
        } catch (Exception e){
            Log.e("init", "read dict_eng failed");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_relative);
        init();
        debug();
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
        long timeGap = startPoint.getTimeBetween(endPoint);
        switch (moveType) {
            case MotionSeperator.FLING_LEFT:                    // this means backspace
                recorder.removeLast();
                textSpeaker.speak(deleteLast()+" removed");
                currentChar.setChar(KEY_NOT_FOUND);
                keyPos.reset();
                refresh();
                break;
            case MotionSeperator.FLING_RIGHT:                   // this means word selected
                String s = recorder.getDataAsString();
                recorder.clear();
                currentChar.setChar(KEY_NOT_FOUND);
                textSpeaker.speak(s);
                appendText(" ");
                keyPos.reset();
                refresh();
                break;
            case MotionSeperator.FLING_DOWN:
            case MotionSeperator.FLING_UP:
            case MotionSeperator.NORMAL_MOVE:
            default:
                currentChar.setChar(keyPos.getKeyByPosition(x, y, currMode, getkey_mode));
                if(currentChar.getChar() == KEY_NOT_FOUND) break;
                if(timeGap > minTimeGapThreshold)               // 说明这个时候是确定的字符
                    recorder.add(currentChar.getChar(), true);
                else
                    recorder.add(currentChar.getChar(), false);
                appendText(currentChar.getChar()+"");
        }
    }

    public void processTouchDown(float x ,float y){
        char mostPossible = predictor.getMostPossibleKey(recorder, x, y);
        Log.e("----------", mostPossible+" is the most possible character");
        if(y < keyPos.topThreshold) {
            textSpeaker.stop();
            textSpeaker.speak("out of range");
            return;
        }
        if(keyPos.shift(mostPossible, x, y)) { refresh(); }

        this.textSpeaker.stop();
        char ch = keyPos.getKeyByPosition(x, y, currMode, getkey_mode);
        if(ch == KEY_NOT_FOUND) return;
        currentChar.setChar(ch);
        textSpeaker.speak(currentChar.getChar()+"");
    }

    public void processTouchMove(float x, float y) {
        char curr = keyPos.getKeyByPosition(x, y, currMode);
        if(curr != currMoveCharacter) {
            textSpeaker.speak(curr+"");
            currMoveCharacter = curr;
        }
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

    public void debug() {

    }

}
