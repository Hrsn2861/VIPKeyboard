package com.example.audiokeyboard.Utils;

import java.util.ArrayList;

public class DataRecorder {

    public ArrayList<Letter> dataSeq;

    void init() {
        this.dataSeq = new ArrayList<>();
    }

    public DataRecorder() {
        init();
    }

    final char KEY_NOT_FOUNT = '*';

    public void add(Letter l) { if(l.getChar()!=KEY_NOT_FOUNT) this.dataSeq.add(l); }
    public void add(char ch) { this.add(new Letter(ch)); }
    public void add(char ch, long time, long timeGap) { this.add(new Letter(ch, time, timeGap)); }
    public void add(char ch, long timeGap) { this.add(new Letter(ch, timeGap)); }

    public void clear() { this.dataSeq.clear(); }
    public Letter removeLast() {
        if(this.dataSeq.size() > 0)
            return this.dataSeq.remove(dataSeq.size()-1);
        return new Letter(KEY_NOT_FOUNT);
    }

    public String getDataAsString() {
        /*
        * get the plaing string;
        */
        String ret = "";
        for(Letter l : dataSeq)
            ret += l.getChar();
        return ret;
    }

    public String getDebugString() {
        /*
        * gett the string whether, display the char whether it is certain or not;
        */
        String ret = "";
        for (Letter l: dataSeq) {
                ret += ("<" + l.getChar() + ">");
        }
        return ret;
    }

    public Letter letterAt(int index) {
        return dataSeq.get(index);
    }

    public int getDataLength() { return dataSeq.size(); }

    public Letter getLetterByIndex(int index) { return this.dataSeq.get(index); }

}
