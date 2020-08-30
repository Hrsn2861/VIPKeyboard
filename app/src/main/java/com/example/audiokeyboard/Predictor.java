package com.example.audiokeyboard;

import android.util.Log;

import com.example.audiokeyboard.Utils.DataRecorder;
import com.example.audiokeyboard.Utils.Key;
import com.example.audiokeyboard.Utils.KeyPos;
import com.example.audiokeyboard.Utils.Letter;
import com.example.audiokeyboard.Utils.Word;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Predictor {
    double predictRange = 0.5;
    public ArrayList<Word> dictEng;
    public ArrayList<Word> dictChnQuan;
    public ArrayList<Word> dictChnJian;
    public ArrayList<Word> dictChnChar;

    public HashMap<String, Word> hanzi2word;
    public HashMap<String, String> hanzi2pinyin;

    KeyPos keyPos;
    final double minTimeGapThreshold = 300.0;

    final int LANG_ENG = 0;
    final int LANG_CHN_QUAN = 1;
    final int LANG_CHN_JIAN = 2;
    final int LANG_CHN = 3;

    public double[][] touchmodelBivariate = new double[][] {                                                        // miux, miuy, sigmax, sigmay, rho
            {106.5370705244123,310.4864376130199,30.84643738543627,108.36748192235623,-0.02330831833564358},
            {589.7261904761905,478.5,48.845798968580134,94.48548049354666,0.214730908005056},
            {400.4183673469388,474.8469387755101,50.113669446052356,97.35738732536404,-0.2366447308709764},
            {338.765306122449,330.204081632653,43.8208162699546,109.4846370792012,-0.014730635130942407},
            {286.26050420168065,137.04481792717093,48.88236203092681,126.57314893236622,0.11530058891156747},
            {420.64285714285717,315.44047619047615,60.80219615111465,103.05342528233166,-0.029111188633954798},
            {487.0922619047619,304.60416666666674,74.50397444834644,107.21701182358194,-0.1891351222320456},
            {634.6490683229814,310.47515527950304,42.9105185283909,112.78831247368309,0.18517499885998054},
            {790.6239892183288,136.08490566037744,56.09568763620872,114.92904371390392,-0.1430138856465102},
            {727.3877551020408,294.98979591836724,46.43621779111141,109.78061486418231,-0.005532231006964178},
            {819.9047619047619,289.22619047619037,52.52414683883449,111.03807565674778,0.1879121406310597},
            {934.1875,287.6517857142858,48.160866183360234,101.4181167799763,0.11981797653836067},
            {825.2755102040817,452.3367346938776,51.550351846618895,107.16765397885197,0.12248260287722319},
            {703.5204081632653,468.6020408163265,48.02057366699831,104.75335199865003,0.2749304318160406},
            {866.6933797909408,156.67247386759573,55.934246406423995,115.12637040547965,-0.16734430162925548},
            {964.2857142857143,143.3571428571429,54.78161934039498,106.24666037072011,-0.14146958166927429},
            {97.38095238095238,143.67857142857133,33.69276664780214,121.11024159674253,0.14333767990303925},
            {393.9761904761905,146.20238095238096,56.931998369324134,107.55875724143993,-0.060714979920368464},
            {226.05882352941177,331.47058823529414,39.78742116988672,115.94657036094083,0.22385525418373883},
            {455.7032967032967,133.7472527472528,65.98391967500609,117.74903570320824,-0.2213498212701198},
            {702.2722371967654,152.93800539083554,47.7343862697426,117.09182783730488,0.05875227778760281},
            {535.3571428571429,487.6428571428571,59.1595031743402,108.62998353060202,0.2562240917454625},
            {186.06593406593407,136.91208791208783,41.40365045575416,101.33921565729938,0.08239569633014367},
            {323.83035714285717,494.17857142857133,40.90306898172254,93.52392518347689,0.030522251525525085},
            {596.1339285714286,180.6428571428571,43.95769783373213,124.10376776611244,-0.05291746091888336},
            {230.16964285714286,509.75892857142867,36.81213283953571,105.07022495366927,0.1516670690845691},
    };

    public double BivariateNormal(double x, double y, double mux, double muy, double sigmax, double sigmay, double rho) {
        return 1.0 / (2.0 * Math.PI*sigmax*sigmay*Math.sqrt(1-rho*rho))*
                Math.pow(Math.E, -1.0/(2.0*(1-rho*rho))*((x-mux)*(x-mux)/sigmax/sigmax+
                        (y-muy)*(y-muy)/sigmay/sigmay-2*rho*(x-mux)*(y-muy)/sigmax/sigmay));
    }

    public double Normal(double x, double miu, double sigma) {
        return 1.0 / Math.sqrt(2.0 * Math.PI) / sigma  * Math.pow(Math.E, -(x - miu) * (x - miu) / 2.0 / sigma / sigma);
    }

    public Predictor(KeyPos keyPos) {
        dictEng = new ArrayList<>();
        dictChnQuan = new ArrayList<>();
        dictChnJian = new ArrayList<>();
        dictChnChar = new ArrayList<>();
        hanzi2word = new HashMap<>();
        hanzi2pinyin = new HashMap<>();
        this.keyPos = keyPos;
    }

    public char getVIPMostPossibleKey(DataRecorder recorder, float x, float y, int langMode) {
        double maxP = Double.NEGATIVE_INFINITY;
        char maxPChar = KeyPos.KEY_NOT_FOUND;
        for(char c='a';c<='z';c++) {
            double p = getVIPPossiblilityByChar(recorder, c, langMode) + getMultiByPointAndKey(x, y, c);
            if(p > maxP) {
                maxPChar = c;
                maxP = p;
            }
        }
        return maxPChar;
    }

    double time2possibility(double time) {
        // return the value from 0 to 1
        if(time > minTimeGapThreshold) return 1.0;
        return time / minTimeGapThreshold;
    }

    public double getVIPPossiblilityByChar(DataRecorder recorder, char c, int langMode) {
        double possibility = 0.0;

        ArrayList<Word> dict;
        switch (langMode) {
            case LANG_ENG:
                dict = dictEng;
                break;
            case LANG_CHN:
            case LANG_CHN_QUAN:
                dict = dictChnQuan;
                break;
            case LANG_CHN_JIAN:
                dict = dictChnJian;
                break;
            default:
                throw new RuntimeException();
        }

        String data = recorder.getDataAsString();
        for(int i=0;i<dict.size();i++) {
            String text = dict.get(i).getText();
            if(text.length() <= recorder.getDataLength()) continue;
            if(text.charAt(recorder.getDataLength()) != c) continue;
            boolean flag = true;
            double buf = 1.0;
            for(int j=0;j<recorder.getDataLength();j++) {
                buf *= calDiffChar(text.charAt(j), recorder.getLetterByIndex(j));
            }
            if(!flag) continue;
            possibility += (buf * dict.get(i).getFreq());
        }
        return Math.log(possibility);
    }

    public char getMostPossibleKey(DataRecorder recorder, float x, float y) {
        double maxP = 0;
        char maxPChar = KeyPos.KEY_NOT_FOUND;
        for(char c='a';c<='z';c++) {
            double p = getPossibilityByChar(recorder, c) * getMultiByPointAndKey(x, y, c);
            if(p > maxP) {
                maxPChar = c;
                maxP = p;
            }
        }
        return maxPChar;
    }

    public double getPossibilityByChar(DataRecorder recorder, char c) {         // 计算P(pre, c)
        double possibility = 0.0;
        for(int i=0;i<dictEng.size();i++) {
            String text = dictEng.get(i).getText();
            if(text.length() <= recorder.getDataLength()) continue;              // 如果长度小于输入串则跳过
            boolean flag = true;
            for(int j=0;j<recorder.getDataLength();j++)
                if(text.charAt(j) != recorder.dataSeq.get(j).getChar()) {
                    flag = false;
                    break;
                }
            if(!flag) continue;                                                 // 如果发现有不一样的则跳过
            if(text.charAt(recorder.getDataLength()) == c)
                possibility += dictEng.get(i).getFreq();
        }
        return possibility;
    }

    public double getMultiByPointAndKey(float x, float y, char c) {             // 找到位置乘子
        double ret = 0.0;
        ret += Math.log(Normal(x, KeyPos.getInitxByChar(c), 52.7));
        ret += Math.log(Normal(y, KeyPos.getInityByChar(c), 45.8));
        return ret;
    }

    public ArrayList<Word> getCandidate(DataRecorder recorder) {
        ArrayList<Word> ret = new ArrayList<>();

        for(int i=0;i<dictEng.size();i++) {
            Word bufWord = dictEng.get(i);
            String data = recorder.getDataAsString();
            if(bufWord.getText().length() <= data.length()) continue;
            boolean flag = true;
            for(int j=0;j<recorder.getDataLength();j++) {
                if(data.charAt(j) != bufWord.getText().charAt(j)) { flag = false; break; }
            }
            if(flag) ret.add(bufWord);
        }

        Collections.sort(ret);
        return ret;
    }

    double calDiffChar(char target, Letter curr) {
        double ret = 1.0;
        if(target == curr.getChar()) return 1.0;
        else if(KeyPos.getKeyAround(curr.getChar()).indexOf(target) == -1) return 0;
        else return 0.2 * (1 - time2possibility(curr.getTimeGap()));
        // ret *= Normal(KeyPos.getInitxByChar(target), KeyPos.getInitxByChar(curr), 52.7);
        // ret *= Normal(KeyPos.getInityByChar(target), KeyPos.getInityByChar(curr), 45.8);
        // return ret;
    }

    public ArrayList<Word> getVIPCandidate(DataRecorder recorder, float x, float y, int langMode) {

        ArrayList<Word> ret = new ArrayList<>();
        String data = recorder.getDataAsString();

        ArrayList<Word> dict;
        switch (langMode) {
            case LANG_ENG:
                dict = dictEng;
                break;
            case LANG_CHN_QUAN:
            case LANG_CHN:
                dict = dictChnQuan;
                break;
            case LANG_CHN_JIAN:
                dict = dictChnJian;
                break;
            default:
                throw new RuntimeException();
        }

        for(int i=0;i<dict.size();i++) {
            Word bufWord = new Word(dict.get(i));
            if(bufWord.getText().length() < data.length()) continue;
            boolean flag = true;
            for(int j=0;j<data.length();j++) {
                // 如果字符不确定，需要计算
                bufWord.freq *= calDiffChar(bufWord.getText().charAt(j), recorder.getLetterByIndex(j));
                bufWord.freq *= Math.exp(-Math.abs(bufWord.getText().length() - data.length()));
            }
            if(flag) ret.add(bufWord);
        }

        Collections.sort(ret);
        return ret;
    }

    public Word getWordFromPinyin(String str) {
        return hanzi2word.getOrDefault(str, new Word(str, 0));
    }

    public boolean checkHanzi2Pinyin(String hanzi, String pinyin) {
        return hanzi2pinyin.getOrDefault(hanzi, "-").equals(pinyin);
    }

}
