package com.example.audiokeyboard;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.elvishew.xlog.LogConfiguration;
import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.flattener.PatternFlattener;
import com.elvishew.xlog.printer.ConsolePrinter;
import com.elvishew.xlog.printer.Printer;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator;
import com.example.audiokeyboard.Utils.DataRecorder;
import com.example.audiokeyboard.Utils.GestureDetector;
import com.example.audiokeyboard.Utils.Key;
import com.example.audiokeyboard.Utils.KeyPos;
import com.example.audiokeyboard.Utils.Letter;
import com.example.audiokeyboard.Utils.MotionPoint;
import com.example.audiokeyboard.Utils.MotionSeperator;
import com.example.audiokeyboard.Utils.PinyinCandidate;
import com.example.audiokeyboard.Utils.PinyinCandidateList;
import com.example.audiokeyboard.Utils.Word;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GestureDetector.onGestureListener {

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

    boolean isFirstCharCertain = true;

    int langMode = 0;
    final int LANG_ENG = 0;
    final int LANG_CHN_QUANPIN = 1;
    final int LANG_CHN_JIANPIN = 2;
    final int LANG_CHN = 3;
    final int maxChnCandidateLength = 50;

    final float voiceSpeed = 10f;
    final long maxWaitingTimeToSpeakCandidate = 800;

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
    TextView studyPhraseView;
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
    int currCandidateIndex = 0;                                 // pinyin: total index;
    String currCandidate;

    PinyinCandidateList pinyinCandidateList;
    PinyinCandidate currCandidateChn;
    String currHanziAndPinyin = "";

    Predictor predictor;

    //settings variables
    boolean isDaFirst = true;
    boolean autoSpeakCandidate = true;
    //
    public IPinyinDecoderService mIPinyinDecoderService = null;
    public PinyinDecoderServiceConnection mPinyinDecoderServiceConnection = null;

    class PinyinDecoderServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIPinyinDecoderService = IPinyinDecoderService.Stub.asInterface(service);
        }
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    GestureDetector gestureDetector = new GestureDetector(this);
    void defaultParams() {
        currMode = Key.MODE_VIP;
    }

    void write(String s) {
        Log.e("-----------", s);
    }

    void initTts() {
        textSpeaker = new TextSpeaker(MainActivity.this);
        textSpeaker.setVoiceSpeed(voiceSpeed);
    }

    void init() {
        updateSettings();
        int height = this.getWindowManager().getDefaultDisplay().getHeight();

        keyPos = new KeyPos(0, height, 0);
        keyboardView = (KeyboardView) (findViewById(R.id.keyboard));
        textView = (TextView) (findViewById(R.id.mytext));
        candidateView = (TextView) (findViewById(R.id.candidateView));
        currCandidateView = (TextView) (findViewById(R.id.currCandidate));
        debugCandidateView = (TextView)(findViewById(R.id.debugCandidate));
        studyPhraseView = findViewById(R.id.studyText);
        recorder = new DataRecorder();
        currentChar = new Letter('*');
        initTts();
        defaultParams();
        keyboardView.setKeysAndRefresh(keyPos.keys);
        initPredictor();
        initDict();
        this.candidates = new ArrayList<>();
        mediaPlayer = MediaPlayer.create(this, R.raw.ios11_da);
        // PINYIN related
        // langMode = LANG_CHN_QUANPIN;
        pinyinCandidateList = new PinyinCandidateList();
        currCandidateChn = new PinyinCandidate();
    }

    void initPredictor() {
        predictor = new Predictor(this.keyPos);
    }

    ArrayList<Word> getInitDict(int id) {
        BufferedReader reader = new BufferedReader(new InputStreamReader((getResources().openRawResource(id))));
        ArrayList<Word> ret = new ArrayList<>();
        String line;
        try{
            int lineNo = 0;
            while ((line = reader.readLine()) != null){
                lineNo++;
                String[] ss = line.split(" ");
                ret.add(new Word(ss[0], Double.valueOf(ss[1])));
                if (lineNo == DICT_SIZE)
                    break;
            }
            reader.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        return ret;
    }

    void initDict() {
        predictor.dictEng = getInitDict(R.raw.dict_eng);
        predictor.dictChnQuan = getInitDict(R.raw.dict_chn_quanpin);
        predictor.dictChnJian = getInitDict(R.raw.dict_chn_jianpin);

        BufferedReader reader = new BufferedReader(new InputStreamReader((getResources().openRawResource(R.raw.dict_chn_pinyin))));
        String line;
        try{
            int lineNo = 0;
            while ((line = reader.readLine()) != null){
                lineNo++;
                String[] ss = line.split(" ");
                predictor.dictChnChar.add(new Word(ss[2], Double.valueOf(ss[1])));
                predictor.hanzi2word.put(ss[2], new Word(ss[2], Double.valueOf(ss[1])));
                predictor.hanzi2pinyin.put(ss[2], ss[0]);
                if (lineNo == DICT_SIZE)
                    break;
            }
            reader.close();
        } catch (Exception e){
            e.printStackTrace();
        }

        reader = new BufferedReader(new InputStreamReader((getResources().openRawResource(R.raw.dict_chn_hint))));
        try{
            int lineNo = 0;
            while ((line = reader.readLine()) != null){
                lineNo++;
                String[] ss = line.split(" ");
                if(ss.length == 1) continue;
                textSpeaker.hanzi2hint.put(ss[0], ss[1]);
                if (lineNo == DICT_SIZE)
                    break;
            }
            reader.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    void initPinyin() {
        if(mIPinyinDecoderService == null) {
            Intent serviceIntent = new Intent();
            serviceIntent.setClass(this, PinyinDecoderService.class);

            if(mPinyinDecoderServiceConnection == null) {
                mPinyinDecoderServiceConnection = new PinyinDecoderServiceConnection();
            }

            if(bindService(serviceIntent, mPinyinDecoderServiceConnection, Context.BIND_AUTO_CREATE)) {
                Log.i("pinyin initialize", "successful");
            }
        }
    }

    private void initLog() {
        // avaiable logtags: RAW_TOUCH_EVENT
        LogConfiguration config = new LogConfiguration.Builder()
                .logLevel(LogLevel.ALL)
                .tag("STUDY")
                .build();
        Printer consolePrinter = new ConsolePrinter();
        Printer filePrinter = new FilePrinter
                .Builder("/sdcard/VIPKeyboard/")
                .fileNameGenerator(new DateFileNameGenerator())
                .flattener(new PatternFlattener("{d yyyy-MM-dd HH:mm:ss.SSS}|{l}|{t}|{m}"))
                .build();
        XLog.init(config, consolePrinter, filePrinter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main_relative);

        requestPermission();

        initLog();
        init();
        initPinyin();
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
            case R.id.action_startStudy:
                startStudy();
                break;
            default:
                break;
        }
        return true;
    }

    private void updateSettings() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        isDaFirst = sharedPreferences.getBoolean("feedback", true);
        isFirstCharCertain = sharedPreferences.getBoolean("firstChar", false);
        autoSpeakCandidate = sharedPreferences.getBoolean("autoSpeakCandidate", true);
        switch (sharedPreferences.getString("langmode", "eng")) {
            case "quanpin":
                langMode = LANG_CHN_QUANPIN;
                break;
            case "jianpin":
                langMode = LANG_CHN_JIANPIN;
                break;
            case "eng":
                langMode = LANG_ENG;
                break;
            case "chn":
                langMode = LANG_CHN;
            default:
                langMode = LANG_ENG;
                break;
        }
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

    @Override
    protected void onDestroy() {
        unbindService(mPinyinDecoderServiceConnection);
        super.onDestroy();
    }

    void appendText(String s) {
        inputText = inputText+s;
//        if(langMode == LANG_ENG) {
//            inputText += " ";
//        }
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
        this.candidates = predictor.getVIPCandidate(recorder, currPoint.getX(), currPoint.getY(), langMode);
        int end = Math.min(start+length, candidates.size());

        if(langMode == LANG_CHN_JIANPIN || langMode == LANG_CHN_QUANPIN) {
            pinyinCandidateList.clear();
            try {
                if (mIPinyinDecoderService.imGetFixedLen() == 0) {                              // 如果要重新选择候选词;
                    int countCandidate = 0;
                    for (int i = 0; i < candidates.size(); i++) {
                        if (candidates.get(i).getText().length() > recorder.getDataLength())
                            continue;
                        countCandidate++;
                        byte[] bytes = candidates.get(i).getText().getBytes();
                        String str = candidates.get(i).getText();
                        int strlen = str.length();
                        int listlen = mIPinyinDecoderService.imSearch(bytes, strlen);
                        List<String> wordlist = mIPinyinDecoderService.imGetChoiceList(0, listlen, mIPinyinDecoderService.imGetFixedLen());
                        for (int j = 0; j < wordlist.size(); j++) {
                            pinyinCandidateList.add(j, i, countCandidate, str, wordlist.get(j));
                        }
                        if (countCandidate > maxChnCandidateLength)
                            break;
                    }
                    end = Math.min(pinyinCandidateList.size(), end);
                    for (int i = start; i < end; i++) {
                        s = s.concat(pinyinCandidateList.getHanzi(i) + "\n");
                    }
                }
                else {
                    List<String> wordlist = mIPinyinDecoderService.imGetChoiceList(0, 100, mIPinyinDecoderService.imGetFixedLen());
                    end = Math.min(wordlist.size(), end);
                    for(int i=0;i<wordlist.size();i++) {
                        pinyinCandidateList.add(currCandidateChn.getChoiceIndex(),
                                currCandidateChn.getPinyinCandidateIndex(),
                                currCandidateChn.getTotalIndex(),
                                currCandidateChn.getPinyin(),
                                wordlist.get(i));
                    }
                    for(int i=start;i<end;i++) {
                        s = s.concat(wordlist.get(i) + "\n");
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            candidateView.setText(s);
        }

        else if(langMode == LANG_ENG) {
            for(int i=start;i<end;i++) {
                s = s.concat(candidates.get(i).getText() + "\n");
            }
            candidateView.setText(s);
        }

    }
    void refreshCandidate(int start) { refreshCandidate(start, 5); }
    void refreshCurrCandidate() {
        currCandidateView.setText(currCandidate);
    }

    void speak(String text2speak) {
        if(langMode == LANG_CHN_JIANPIN || langMode == LANG_CHN_QUANPIN)
            textSpeaker.speakHint(text2speak);
        else if(langMode == LANG_ENG)
            textSpeaker.speak(text2speak);
    }

    public int getRemainPinyinLength(String s) {
        int ret = 0;
        for(int i=s.length()-1;i>=0;i--) {
            if(s.charAt(i) <'a' || s.charAt(i) > 'z')
                break;
            ret++;
        }
        return ret;
    }

    public void processTouchUp(float x, float y) {
 /*       isDaVoiceHappened = false;
        if(isTowFingerMotion) return;*/
        int moveType = MotionSeperator.getMotionType(startPoint, endPoint);
        //long timeGap = startPoint.getTimeBetween(endPoint);
        switch (moveType) {
            case MotionSeperator.FLING_DOWN:                    // this means backspace

//                if(langMode == LANG_CHN_JIANPIN || langMode == LANG_CHN_QUANPIN) {
//                    try {
//                        if (mIPinyinDecoderService.imGetFixedLen() == 0) {
//                            recorder.removeLast();
//                            textSpeaker.speak(deleteLast() + "已删除");
//                        }
//                        else {
//                            mIPinyinDecoderService.imCancelLastChoice();
//                            String buf = currHanziAndPinyin.substring(0, mIPinyinDecoderService.imGetFixedLen());
//                            for(int i=0;i<currHanziAndPinyin.length();i++) deleteLast();
//                            write(buf+" "+currHanziAndPinyin);
//                            buf += recorder.getDataAsString().substring(mIPinyinDecoderService.imGetSplStart()[mIPinyinDecoderService.imGetFixedLen()+1]);
//                            write(buf);
//                            currHanziAndPinyin = buf;
//                            appendText(buf);
//                            currCandidateChn.setPinyin(recorder.getDataAsString());
//                            currCandidateChn.setHanzi(mIPinyinDecoderService.imGetChoice(0));
//                            currCandidateIndex = -1;
//                        }
//                    } catch (RemoteException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                else if(langMode == LANG_ENG) {
//                    recorder.removeLast();
//                    textSpeaker.speak(deleteLast() + "已删除");
//                }
//
//                currentChar.setChar(KEY_NOT_FOUND);
//                keyPos.reset();
//                refresh();
//                refreshCandidate(0);
//                break;
            case MotionSeperator.FLING_UP:                   // this means word selected
/*                String s = "";
                textSpeaker.stop();
                currentChar.setChar(KEY_NOT_FOUND);

                if(langMode == LANG_CHN_QUANPIN || langMode == LANG_CHN_JIANPIN) {
                    s = currHanziAndPinyin;
                    // Log.e("current candidate", currCandidate);
                    // Log.e("current candidate chn", currCandidateChn.getHanzi());
                    try {
                        int temp;
                        String curr = recorder.getDataAsString();
                        int fixedLen = mIPinyinDecoderService.imGetFixedLen();
                        if(mIPinyinDecoderService.imGetFixedLen() == 0)                                 // 如果没有确定的那么刷新；
                            mIPinyinDecoderService.imSearch(curr.getBytes(), curr.length());
                        if(currCandidateIndex == -1)
                            temp = mIPinyinDecoderService.imChoose(0);
                        else
                            temp = mIPinyinDecoderService.imChoose(currCandidateIndex);

                        if(temp == 1) {                 // 最后一个拼音
                            // int removeLength = getRemainPinyinLength(currHanziAndPinyin);
                            int removeLength = currHanziAndPinyin.length() - fixedLen;
                            for(int i=0;i<removeLength;i++) deleteLast();

                            s = currCandidateChn.getHanzi();
                            Log.e("++++++++++", s);
                            mIPinyinDecoderService.imResetSearch();
                            recorder.clear();
                            currHanziAndPinyin = "";
                        }
                        else {
                            if (currHanziAndPinyin.length() == 0) currHanziAndPinyin = recorder.getDataAsString();
                            for(int i=0;i<currHanziAndPinyin.length();i++) deleteLast();

                            String buffer = mIPinyinDecoderService.imGetChoice(0);

                            s = buffer.substring(0, mIPinyinDecoderService.imGetFixedLen());
                            s += recorder.getDataAsString().substring(mIPinyinDecoderService.imGetSplStart()[mIPinyinDecoderService.imGetFixedLen()+1]);
                            currHanziAndPinyin = s;

                            currCandidateIndex = -1;
                            currCandidateChn.setHanzi(buffer.substring(mIPinyinDecoderService.imGetFixedLen()));
                            currCandidateChn.setPinyin("");
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

                else if(langMode == LANG_ENG) {
                    s = recorder.getDataAsString();
                    for(int i=0;i<s.length();i++) deleteLast();
                    if(currCandidateIndex != -1) {
                        s = currCandidate;
                    }
                    recorder.clear();
                }

                appendText(s);
                speak(s);
                keyPos.reset();
                refresh();
                refreshCandidate(0);
                break;*/
            case MotionSeperator.FLING_RIGHT:
/*                textSpeaker.stop();
                currCandidateIndex = Math.min(currCandidateIndex+1, this.candidates.size()-1);

                if(langMode == LANG_CHN_JIANPIN || langMode == LANG_CHN_QUANPIN) {
                    if(this.candidates.isEmpty() || this.pinyinCandidateList.size() == 0) {
                        currCandidate = "no candidate";
                        currCandidateChn = new PinyinCandidate(0, 0, 0, "", "");
                    }
                    else {
                        currCandidate = this.pinyinCandidateList.getHanzi(currCandidateIndex);
                        currCandidateChn = this.pinyinCandidateList.get(currCandidateIndex);
                    }
                    speak(currCandidateChn.getHanzi());
                }

                else if(langMode == LANG_ENG) {
                    if(this.candidates.isEmpty())
                        currCandidate = "no candidate";
                    else
                        currCandidate = this.candidates.get(currCandidateIndex).getText();
                    speak(currCandidate);
                }

                refreshCandidate(Math.max(0, currCandidateIndex));
                refreshCurrCandidate();
                break;*/
            case MotionSeperator.FLING_LEFT:
 /*               textSpeaker.stop();
                currCandidateIndex = currCandidateIndex == 0 ? -1 : Math.max(currCandidateIndex-1, 0);

                if(langMode == LANG_CHN_QUANPIN || langMode == LANG_CHN_JIANPIN) {
                    if(this.candidates.isEmpty() || this.pinyinCandidateList.size() == 0) {
                        currCandidate = "no candidate";
                        currCandidateChn = new PinyinCandidate(0, 0, 0, "", "");
                    }
                    else if(currCandidateIndex == -1) {
                        currCandidate = recorder.getDataAsString();
                        currCandidateChn = new PinyinCandidate(0, 0, 0, "", "");
                    }
                    else {
                        currCandidate = this.pinyinCandidateList.getHanzi(currCandidateIndex);
                        currCandidateChn = this.pinyinCandidateList.get(currCandidateIndex);
                    }
                    speak(currCandidateChn.getHanzi());
                }

                else if(langMode == LANG_ENG) {
                    if (candidates.isEmpty())
                        currCandidate = "no candidate";
                    else
                        currCandidate = currCandidateIndex == -1 ? recorder.getDataAsString() : this.candidates.get(currCandidateIndex).getText();
                    speak(currCandidate);
                }
                refreshCandidate(Math.max(0, currCandidateIndex));
                refreshCurrCandidate();
                break;*/
            case MotionSeperator.NORMAL_MOVE:
            default:
 /*               currCandidateIndex = -1;
                currCandidate = "";
                if(!skipUpDetect || startPoint.getDistance(endPoint) > minMoveDistToCancelBestChar)                         // 如果这里面不要跳过或者移动距离超了才会进行更新currentchar，否则会直接利用touchdown时候的字符；
                    currentChar.setChar(keyPos.getKeyByPosition(x, y, currMode, getkey_mode));
                if(currentChar.getChar() == KEY_NOT_FOUND) break;
                if(currentChar.getChar() > 'z' || currentChar.getChar() < 'a') break;
                if(timeGap > minTimeGapThreshold || ((recorder.getDataLength() == 0) && isFirstCharCertain)) {               // 说明这个时候是确定的字符
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
                if(!candidates.isEmpty() && speakCandidate && timeGap > maxWaitingTimeToSpeakCandidate) {
                    textSpeaker.speak(candidates.get(0).getText());
                }
                break;*/
        }
    }

    public void processTouchDown(float x ,float y){
        if (isDaFirst()) {
            textSpeaker.stop();
            char mostPossible = predictor.getVIPMostPossibleKey(recorder, x, y, langMode);
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
            char mostPossible = predictor.getVIPMostPossibleKey(recorder, x, y, langMode);
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
        //if (gestureDetector.isPotentialSwipe()) return;
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
            if (curr != lastReadKey && System.currentTimeMillis() - startPoint.getTime() > minTimeGapThreshold && curr != KEY_NOT_FOUND) {
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
//                recorder.clear();
//                clearText();
//                currentChar.setChar(KEY_NOT_FOUND);
//                textSpeaker.speak("已清空");
//                keyPos.reset();
//                refresh();
//                currCandidate = "";
//                refreshCurrCandidate();
//                break;
            default:
                // Log.e("++++++++++", "NULL MOVE");
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY()-keyPos.wholewindowSize+keyPos.partialwindowSize;
        XLog.tag("RAW_TOUCH_EVENT").i("%s,%f,%f", MotionEvent.actionToString(event.getActionMasked()), x, y);
        gestureDetector.onTouchEvent(event);
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
                lastReadKey = ' ';
                isDaVoiceHappened = false;
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

    private void confirm() {
        String s = "";
        textSpeaker.stop();
        currentChar.setChar(KEY_NOT_FOUND);
        XLog.tag("DEBUG").i("index" + currCandidateIndex);
        XLog.tag("DEBUG").i(candidates);


        if(langMode == LANG_CHN_QUANPIN || langMode == LANG_CHN_JIANPIN) {
            s = currHanziAndPinyin;
            // Log.e("current candidate", currCandidate);
            // Log.e("current candidate chn", currCandidateChn.getHanzi());
            try {
                int temp;
                String curr = recorder.getDataAsString();
                int fixedLen = mIPinyinDecoderService.imGetFixedLen();
                if(mIPinyinDecoderService.imGetFixedLen() == 0)                                 // 如果没有确定的那么刷新；
                    mIPinyinDecoderService.imSearch(curr.getBytes(), curr.length());
                if(currCandidateIndex == -1)
                    temp = mIPinyinDecoderService.imChoose(0);
                else
                    temp = mIPinyinDecoderService.imChoose(currCandidateIndex);

                if(temp == 1) {                 // 最后一个拼音
                    // int removeLength = getRemainPinyinLength(currHanziAndPinyin);
                    int removeLength = currHanziAndPinyin.length() - fixedLen;
                    for(int i=0;i<removeLength;i++) deleteLast();

                    s = currCandidateChn.getHanzi();
                    Log.e("++++++++++", s);
                    mIPinyinDecoderService.imResetSearch();
                    recorder.clear();
                    currHanziAndPinyin = "";
                }
                else {
                    if (currHanziAndPinyin.length() == 0) currHanziAndPinyin = recorder.getDataAsString();
                    for(int i=0;i<currHanziAndPinyin.length();i++) deleteLast();

                    String buffer = mIPinyinDecoderService.imGetChoice(0);

                    s = buffer.substring(0, mIPinyinDecoderService.imGetFixedLen());
                    s += recorder.getDataAsString().substring(mIPinyinDecoderService.imGetSplStart()[mIPinyinDecoderService.imGetFixedLen()+1]);
                    currHanziAndPinyin = s;

                    currCandidateIndex = -1;
                    currCandidateChn.setHanzi(buffer.substring(mIPinyinDecoderService.imGetFixedLen()));
                    currCandidateChn.setPinyin("");
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        else if(langMode == LANG_ENG) {
            s = recorder.getDataAsString();
            for(int i=0;i<s.length();i++) deleteLast();
            if(currCandidateIndex != -1) {
                if (currCandidateIndex >= 0 && currCandidateIndex < candidates.size()) {
                    currCandidate = candidates.get(currCandidateIndex).getText();
                    s = currCandidate;
                }
            }
            recorder.clear();
        }

        appendText(s + " ");
        speak(s);
        XLog.tag("STUDY").i("CONFIRM,%s", s);
        keyPos.reset();
        refresh();
        refreshCandidate(0);
        if (inStudy) {
            nextStudyTask();
        }

    }

    private void delete() {
        if(langMode == LANG_CHN_JIANPIN || langMode == LANG_CHN_QUANPIN) {
            try {
                if (mIPinyinDecoderService.imGetFixedLen() == 0) {
                    recorder.removeLast();
                    textSpeaker.speak(deleteLast() + "已删除");
                }
                else {
                    mIPinyinDecoderService.imCancelLastChoice();
                    String buf = currHanziAndPinyin.substring(0, mIPinyinDecoderService.imGetFixedLen());
                    for(int i=0;i<currHanziAndPinyin.length();i++) deleteLast();
                    write(buf+" "+currHanziAndPinyin);
                    buf += recorder.getDataAsString().substring(mIPinyinDecoderService.imGetSplStart()[mIPinyinDecoderService.imGetFixedLen()+1]);
                    write(buf);
                    currHanziAndPinyin = buf;
                    appendText(buf);
                    currCandidateChn.setPinyin(recorder.getDataAsString());
                    currCandidateChn.setHanzi(mIPinyinDecoderService.imGetChoice(0));
                    currCandidateIndex = -1;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        else if(langMode == LANG_ENG) {
            recorder.removeLast();
            textSpeaker.speak(deleteLast() + "已删除");
        }

        currentChar.setChar(KEY_NOT_FOUND);
        keyPos.reset();
        refresh();
        refreshCandidate(0);
        XLog.tag("STUDY").i("DELETE,%s", inputText);
    }

    private void previousCandidate() {
        textSpeaker.stop();
        currCandidateIndex = currCandidateIndex == 0 ? -1 : Math.max(currCandidateIndex-1, 0);

        if(langMode == LANG_CHN_QUANPIN || langMode == LANG_CHN_JIANPIN) {
            if(this.candidates.isEmpty() || this.pinyinCandidateList.size() == 0) {
                currCandidate = "no candidate";
                currCandidateChn = new PinyinCandidate(0, 0, 0, "", "");
            }
            else if(currCandidateIndex == -1) {
                currCandidate = recorder.getDataAsString();
                currCandidateChn = new PinyinCandidate(0, 0, 0, "", "");
            }
            else {
                currCandidate = this.pinyinCandidateList.getHanzi(currCandidateIndex);
                currCandidateChn = this.pinyinCandidateList.get(currCandidateIndex);
            }
            speak(currCandidateChn.getHanzi());
        }

        else if(langMode == LANG_ENG) {
            if (candidates.isEmpty())
                currCandidate = "no candidate";
            else
                currCandidate = currCandidateIndex == -1 ? recorder.getDataAsString() : this.candidates.get(currCandidateIndex).getText();
            speak(currCandidate);
        }
        refreshCandidate(Math.max(0, currCandidateIndex));
        refreshCurrCandidate();
        XLog.tag("STUDY").i("PREVIOUS,%s", currCandidate);
    }

    private void nextCandidate() {
        textSpeaker.stop();
        XLog.tag("DEBUG").d(currCandidateIndex);
        currCandidateIndex = Math.min(currCandidateIndex+1, this.candidates.size()-1);
        XLog.tag("DEBUG").d(currCandidateIndex);
        if(langMode == LANG_CHN_JIANPIN || langMode == LANG_CHN_QUANPIN) {
            if(this.candidates.isEmpty() || this.pinyinCandidateList.size() == 0) {
                currCandidate = "no candidate";
                currCandidateChn = new PinyinCandidate(0, 0, 0, "", "");
            }
            else {
                currCandidate = this.pinyinCandidateList.getHanzi(currCandidateIndex);
                currCandidateChn = this.pinyinCandidateList.get(currCandidateIndex);
            }
            speak(currCandidateChn.getHanzi());
        }

        else if(langMode == LANG_ENG) {
            if(this.candidates.isEmpty())
                currCandidate = "no candidate";
            else
                currCandidate = this.candidates.get(currCandidateIndex).getText();
            speak(currCandidate);
        }

        refreshCandidate(Math.max(0, currCandidateIndex));
        refreshCurrCandidate();
        XLog.tag("STUDY").i("NEXT,%s", currCandidate);
    }

    @Override
    public boolean onSwipe(GestureDetector.Direction direction) {
        XLog.tag("GESTURE").i("SWIPE" + direction.name());
        switch (direction) {
            case DOWN: //删除
                delete();
                break;
            case UP: //确认
                confirm();
                break;
            case LEFT: //上一个
                previousCandidate();
                break;
            case RIGHT: //下一个
                nextCandidate();
                break;
            case DOWN_THEN_UP:
                if (inStudy) {
                    readStudyTask();
                }
                break;
            case DOWN_THEN_LEFT:
                startStudy();
                break;
            case DOWN_THEN_RIGHT:
                readInput();
                break;
            case LEFT_THEN_RIGHT:
                if (inStudy) {
                    previousStudyTask();
                }
                break;
            case RIGHT_THEN_LEFT:
                help();
                break;

            default:
                break;
        }
        return false;
    }

    private void help() {
        speak("左右滑动 切换候选 上滑确认 下滑删除 下左开始实验 下上虫读实验任务 左右虫做前一个任务 下右读当前候选");
    }

    private void readInput() {
        String split = "";
        for (int i =0; i<inputText.length(); i++) {
            split += (inputText.charAt(i) + " ");
        }
        speak("当前输入内容为 " + inputText + " " + split);
    }

    @Override
    public boolean onTap(MotionEvent event) {
        float x = event.getX();
        float y = event.getY()-keyPos.wholewindowSize+keyPos.partialwindowSize;
        XLog.tag("GESTURE").i("TAP," + x + "," + y);

        isDaVoiceHappened = false;
        //long timeGap = startPoint.getTimeBetween(endPoint);
        long timeGap = System.currentTimeMillis() - startPoint.getTime();
        /////////

        currCandidateIndex = autoSpeakCandidate? 0 : -1;
        currCandidate = "";
        if(!skipUpDetect || startPoint.getDistance(endPoint) > minMoveDistToCancelBestChar)                         // 如果这里面不要跳过或者移动距离超了才会进行更新currentchar，否则会直接利用touchdown时候的字符；
            currentChar.setChar(keyPos.getKeyByPosition(x, y, currMode, getkey_mode));
        if(currentChar.getChar() == KEY_NOT_FOUND) return false;
        if(currentChar.getChar() > 'z' || currentChar.getChar() < 'a') return false;
        if(timeGap > minTimeGapThreshold || ((recorder.getDataLength() == 0) && isFirstCharCertain)) {               // 说明这个时候是确定的字符
            recorder.add(currentChar.getChar(), true);
            //mediaPlayer.start();
        }
        else {
            recorder.add(currentChar.getChar(), false);
        }
        appendText(currentChar.getChar()+"");
        refreshCandidate(0);
        refreshCurrCandidate();
        String can = "";
        for (int i=0; i<5; i++) {
            if (i < candidates.size()) {
                can += (((i == 0) ? "" : ",") + candidates.get(i).getText());
            } else {
                can += (((i == 0) ? "" : ",") + "");
            }
        }
        XLog.tag("STUDY").i("CANDIDATE,%s", can);
        String debugStr = recorder.getDebugString();
        XLog.tag("STUDY").i("CHAR,%s", debugStr);
        debugCandidateView.setText(debugStr);
        if(!candidates.isEmpty() && autoSpeakCandidate) {
            textSpeaker.speak(candidates.get(0).getText());
        }
        return false;
    }

    @Override
    public boolean on2FingerSwipe(GestureDetector.Direction direction) {
        XLog.tag("GESTURE").i("2SWIPE" + direction.name());
        switch (direction) {
            case LEFT: //清空
                clearAll();
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        XLog.tag("GESTURE").i("DOUBLETAP");
        //confirm();
        return false;
    }

    public void clearAll() {
        isDaVoiceHappened = false;
        recorder.clear();
        clearText();
        candidateView.setText("");
        debugCandidateView.setText("");
        currentChar.setChar(KEY_NOT_FOUND);
        textSpeaker.speak("已清空");
        keyPos.reset();
        refresh();
        currCandidate = "";
        refreshCurrCandidate();
        XLog.d("CLEARALL");
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



    List<String> testPhrases = new ArrayList<>();
    List<String> tasks = new ArrayList<>();
    private boolean inStudy = false;
    private final int TASK_NUM = 2;
    private int currentTaskIndex = -1;

    private void loadTestPhrase() {
        if (testPhrases.size() == 0) {
            try {
                InputStream is = getResources().openRawResource(R.raw.eng_test_phrase);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String str;
                while ((str = reader.readLine()) != null) {
                    testPhrases.add(str);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Collections.shuffle(testPhrases);
        tasks.clear();
        for (int i=0; i< TASK_NUM; i++) {
            String[] words = testPhrases.get(i).split(" ");
            for (String word : words) {
                tasks.add(word);
            }
        }
    }
    public void startStudy() {
        loadTestPhrase();
        XLog.i("STARTTASK");
        inStudy = true;
        setTitle("实验中");
        XLog.tag("CONFIG").i("Feedback,%s", isDaFirst()?"DaFirst":"ReadFirst");
        XLog.tag("CONFIG").i("AutoSpeakCandidate,%s", autoSpeakCandidate?"Yes":"No");
        currentTaskIndex = -1;
        clearAll();
        nextStudyTask();
    }

    public void endStudy() {
        XLog.i("ENDTASK");
        speak("实验结束，谢谢参与");
        inStudy = false;
        studyPhraseView.setText("");
        setTitle("VIPKeyboard");
    }

    private void nextStudyTask() {
        XLog.i("NEXTTASK");
        currentTaskIndex += 1;
        clearText();
        if (currentTaskIndex == tasks.size()) {
            endStudy();
            return;
        }
        String task = tasks.get(currentTaskIndex);
        studyPhraseView.setText(task);
        XLog.i("TASK,%s", task);
        readStudyTask();
    }

    private void previousStudyTask() {
        XLog.i("PREVTASK");
        clearAll();
        currentTaskIndex -= 1;
        if (currentTaskIndex < 0) currentTaskIndex = 0;
        String task = tasks.get(currentTaskIndex);
        studyPhraseView.setText(task);
        XLog.i("TASK,%s", task);
        readStudyTask();
    }

    private void readStudyTask() {
        if (currentTaskIndex < 0 || currentTaskIndex >= tasks.size()) return;
        textSpeaker.speak("当前任务为 " + tasks.get(currentTaskIndex));
    }

    private static String[] PERMISSION = {
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static int REQUEST_CODE = 1;

    private void requestPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSION, REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            Toast.makeText(this,"申请权限成功", Toast.LENGTH_SHORT).show();
        }
    }
}
