package com.example.audiokeyboard;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
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
    final int SETTINGS_CODE = 11;

    final long MIN_TIME_GAP = 1000;
    final char KEY_NOT_FOUND = '*';

    final double minVelocityToStopSpeaking = 0.2;
    final double maxVelocityToDetermineHover = 0.1;
    final long minTimeGapThreshold = 150;               // 如果大于这个时间长度说明key是确定的；
    final long minMoveDistToCancelBestChar = 20;        // 如果大于这个距离就取消选中的最佳；（感觉这个机制不是很靠谱）

    int currMode;

    final float voiceSpeed = 10f;
    final long maxWaitingTimeToSpeakCandidate = 800;
    boolean speakCandidate = true;

    MediaPlayer mediaPlayer;

    Letter currentChar;
    KeyboardView keyboardView;
    TextSpeaker textSpeaker;
    DataRecorder recorder;
    KeyPos keyPos;

    // all the text input, send to TextView
    String inputText = "";
    TextView textView;
    TextView candidateView;
    TextView currCandidateView;
    TextView debugCandidateView;
    ArrayList<Word> candidates;

    // record the edge pointer moved along
    boolean isTowFingerMotion = false;
    MotionPoint secondStartPoint = new MotionPoint(0, 0);
    MotionPoint secondEndPoint = new MotionPoint(0, 0);
    MotionPoint startPoint = new MotionPoint(0, 0);
    MotionPoint endPoint =  new MotionPoint(0, 0);
    MotionPoint currPoint = new MotionPoint(0, 0);
    boolean skipUpDetect = false;
    char currMoveCharacter = KEY_NOT_FOUND;

    final int DICT_SIZE = 50000;
    int currCandidateIndex = 0;
    String currCandidate;

    Predictor predictor;

    //config variables
    boolean isDaFirst = true;

    class PinyinDecoderServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIPinyinDecoderService = IPinyinDecoderService.Stub.asInterface(service);
        }
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    public IPinyinDecoderService mIPinyinDecoderService = null;
    public PinyinDecoderServiceConnection mPinyinDecoderServiceConnection = null;

    void defaultParams() {
        currMode = Key.MODE_VIP;
    }

    void initTts() {
        textSpeaker = new TextSpeaker(MainActivity.this);
        textSpeaker.setVoiceSpeed(voiceSpeed);
    }

    void init() {
        updateSettings();
        int height = this.getWindowManager().getDefaultDisplay().getHeight();
        Log.e("+++++++++", ""+height);
        keyPos = new KeyPos(0, height, 0);
        keyboardView = (KeyboardView) (findViewById(R.id.keyboard));
        textView = (TextView) (findViewById(R.id.mytext));
        candidateView = (TextView) (findViewById(R.id.candidateView));
        currCandidateView = (TextView) (findViewById(R.id.currCandidate));
        debugCandidateView = (TextView)(findViewById(R.id.debugCandidate));
        recorder = new DataRecorder();
        currentChar = new Letter('*');
        initTts();
        defaultParams();
        keyboardView.setKeysAndRefresh(keyPos.keys);
        initPredictor();
        initDict();
        this.candidates = new ArrayList<>();
        mediaPlayer = MediaPlayer.create(this, R.raw.ios11_da);
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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main_relative);
        init();
        debug();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS_CODE);
                break;
            default:
                break;
        }
        return true;
    }

    private void updateSettings() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        isDaFirst = sharedPreferences.getBoolean("feedback", true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        updateSettings();
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    void appendText(String s) {
        inputText = inputText+s;
        this.textView.setText(inputText);
    }
    void clearText() {
        inputText = "";
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
    void refreshCandidate(int start, int length) {
        String s = "";
        // this.candidates = predictor.getCandidate(recorder);
        this.candidates = predictor.getVIPCandidate(recorder, currPoint.getX(), currPoint.getY());
        int end = Math.min(start+length, candidates.size());
        for(int i=start;i<end;i++) {
            s = s.concat(candidates.get(i).getText() + "\n");
        }
        candidateView.setText(s);
    }
    void refreshCandidate(int start) { refreshCandidate(start, 5); }
    void refreshCurrCandidate() {
        currCandidateView.setText(currCandidate);
    }

    public void processTouchUp(float x, float y) {
        isDaVoiceHappened = false;
        if(isTowFingerMotion) return;
        int moveType = MotionSeperator.getMotionType(startPoint, endPoint);
        long timeGap = startPoint.getTimeBetween(endPoint);
        switch (moveType) {
            case MotionSeperator.FLING_DOWN:                    // this means backspace
                recorder.removeLast();
                textSpeaker.speak(deleteLast() + "已删除");
                currentChar.setChar(KEY_NOT_FOUND);
                keyPos.reset();
                refresh();
                refreshCandidate(0);
                break;
            case MotionSeperator.FLING_UP:                   // this means word selected
                String s = recorder.getDataAsString();
                textSpeaker.stop();
                recorder.clear();
                currentChar.setChar(KEY_NOT_FOUND);
                if(currCandidateIndex != -1) {
                    for(int i=0;i<s.length();i++) deleteLast();
                    s = currCandidate;
                    appendText(s);
                }
                textSpeaker.speak(s);
                appendText(" ");
                keyPos.reset();
                refresh();
                refreshCandidate(0);
                break;
            case MotionSeperator.FLING_RIGHT:
                textSpeaker.stop();
                currCandidateIndex = Math.min(currCandidateIndex+1, this.candidates.size()-1);
                if(this.candidates.isEmpty()) {
                    currCandidate = "no candidate";
                }
                else {
                    currCandidate = this.candidates.get(currCandidateIndex).getText();
                }
                textSpeaker.speak(currCandidate);
                refreshCurrCandidate();
                break;
            case MotionSeperator.FLING_LEFT:
                textSpeaker.stop();
                currCandidateIndex = currCandidateIndex == 0 ? -1 : Math.max(currCandidateIndex-1, 0);
                if(candidates.isEmpty()) {
                    currCandidate = "no candidate";
                }
                else {
                    currCandidate = currCandidateIndex == -1 ? "" : this.candidates.get(currCandidateIndex).getText();
                }
                textSpeaker.speak(currCandidate);
                refreshCurrCandidate();
                break;
            case MotionSeperator.NORMAL_MOVE:
            default:
                currCandidateIndex = -1;
                currCandidate = "";
                if(!skipUpDetect || startPoint.getDistance(endPoint) > minMoveDistToCancelBestChar)                         // 如果这里面不要跳过或者移动距离超了才会进行更新currentchar，否则会直接利用touchdown时候的字符；
                    currentChar.setChar(keyPos.getKeyByPosition(x, y, currMode, getkey_mode));
                if(currentChar.getChar() == KEY_NOT_FOUND) break;
                if(currentChar.getChar() > 'z' || currentChar.getChar() < 'a') break;
                if(timeGap > minTimeGapThreshold) {               // 说明这个时候是确定的字符
                    recorder.add(currentChar.getChar(), true);
                    //mediaPlayer.start();
                }
                else {
                    recorder.add(currentChar.getChar(), false);
                }
                appendText(currentChar.getChar()+"");
                refreshCandidate(0);
                refreshCurrCandidate();
                debugCandidateView.setText(recorder.getDebugString());
                if(!candidates.isEmpty() && speakCandidate && timeGap > maxWaitingTimeToSpeakCandidate) textSpeaker.speak(candidates.get(0).getText());
                break;
        }
    }

    public void processTouchDown(float x ,float y){
        if (isDaFirst()) {
            textSpeaker.stop();
            char mostPossible = predictor.getVIPMostPossibleKey(recorder, x, y);
            if (y < keyPos.topThreshold) {
                currentChar.setChar(KEY_NOT_FOUND);                         // 需要清空
                textSpeaker.speak("出界");
                return;
            }
            char ch = KEY_NOT_FOUND;
            if (keyPos.shift(mostPossible, x, y)) {
                ch = mostPossible;
                skipUpDetect = true;
                refresh();
            }    // 如果设置的话
            else {
                ch = keyPos.getKeyByPosition(x, y, currMode, getkey_mode);
            }

            if (ch == KEY_NOT_FOUND) return;
            currentChar.setChar(ch);
            currMoveCharacter = ch;//?
        } else {
            textSpeaker.stop();
            char mostPossible = predictor.getVIPMostPossibleKey(recorder, x, y);
            if (y < keyPos.topThreshold) {
                currentChar.setChar(KEY_NOT_FOUND);                         // 需要清空
                textSpeaker.speak("出界");
                return;
            }
            char ch = KEY_NOT_FOUND;
            if (keyPos.shift(mostPossible, x, y)) {
                ch = mostPossible;
                skipUpDetect = true;
                refresh();
            }    // 如果设置的话
            else {
                ch = keyPos.getKeyByPosition(x, y, currMode, getkey_mode);
            }

            if (ch == KEY_NOT_FOUND) return;
            currentChar.setChar(ch);
            textSpeaker.speak(currentChar.getChar() + "");
            currMoveCharacter = ch;//?
        }
    }

    boolean isDaFirst() {
        return isDaFirst;
    }

    boolean isDaVoiceHappened = false;
    char lastReadKey = ' ';
    public void processTouchMove(float x, float y) {
        if (isDaFirst()) {//先嗒再读
            char curr = keyPos.getKeyByPosition(x, y, currMode);
            if (!isDaVoiceHappened) {
                mediaPlayer.start();
                isDaVoiceHappened = true;
            }
            if (curr != currMoveCharacter) {
                currMoveCharacter = curr;
            }
            ////
            if (curr != lastReadKey && System.currentTimeMillis() - startPoint.getTime() > minTimeGapThreshold) {
                textSpeaker.speak(curr + "");
                lastReadKey = curr;
            }

        } else { //先读再嗒
            char curr = keyPos.getKeyByPosition(x, y, currMode);
            if (curr != currMoveCharacter) {
                textSpeaker.speak(curr + "");
                currMoveCharacter = curr;
                mediaPlayer.start();
            }
            ////
            if (!isDaVoiceHappened && System.currentTimeMillis() - startPoint.getTime() > minTimeGapThreshold) {
                isDaVoiceHappened = true;
                mediaPlayer.start();
            }
        }
    }

    public void processDoubleTouchUp(float x, float y) {
        int motionType = MotionSeperator.getMotionType(startPoint, secondStartPoint, endPoint, secondEndPoint);
        switch(motionType) {
            case MotionSeperator.DOUBLE_FLING_DOWN:
                Log.e("+++++++++++", "this is a double fling down");
                recorder.clear();
                clearText();
                currentChar.setChar(KEY_NOT_FOUND);
                textSpeaker.speak("已清空");
                keyPos.reset();
                refresh();
                refreshCurrCandidate();
                break;
            default:
                Log.e("++++++++++", "NULL MOVE");
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY()-keyPos.wholewindowSize+keyPos.partialwindowSize;
        // float y = event.getY();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            // case MotionEvent.ACTION_POINTER_DOWN:
                isTowFingerMotion = false;
                startPoint.set(x, y);
                processTouchDown(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                processTouchMove(x, y);
                break;
            case MotionEvent.ACTION_UP:
            // case MotionEvent.ACTION_POINTER_UP:
                endPoint.set(x, y);
                processTouchUp(x, y);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                secondStartPoint.set(x, y);
                isTowFingerMotion = true;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                secondEndPoint.set(x, y);
                processDoubleTouchUp(x, y);
        }
        return super.onTouchEvent(event);
    }

    public void debug() {
        keyboardView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int height = getWindowManager().getDefaultDisplay().getHeight();

                int viewTop = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
                int viewBottom = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getBottom();
                int viewHeight = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getHeight();
                int viewWidth = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getWidth();
                Log.e("this is view top", viewTop+" "+viewBottom+" "+viewHeight);

                keyPos = new KeyPos(viewHeight, height, viewWidth);

                for(char c='a';c<='z';c++) {
                    assert KeyPos.getInitxByChar(c) == keyPos.getInitx(c);
                    assert KeyPos.getInityByChar(c) == keyPos.getInity(c);
                }
                refresh();
            }
        });
    }

    public void debug(String text) {
        for(int i=0;i<predictor.dictEng.size();i++) {
            if(predictor.dictEng.get(i).getText().equals(text)) {
                Log.e("+++++++test word", predictor.dictEng.get(i).getText()+" "+predictor.dictEng.get(i).getFreq());
                return;
            }
        }
        Log.e("+++++++test word", "word not found");
    }

}
