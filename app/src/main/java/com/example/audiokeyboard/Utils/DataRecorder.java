package com.example.audiokeyboard.Utils;

import java.util.ArrayList;

public class DataRecorder {

    ArrayList<Letter> dataSeq;

    void init() {
        this.dataSeq = new ArrayList<>();
    }

    public DataRecorder() {
        init();
    }

    final char KEY_NOT_FOUNT = '*';

    public void add(Letter l) { if(l.getChar()!=KEY_NOT_FOUNT) this.dataSeq.add(l); }
    public void add(char ch) { this.add(new Letter(ch)); }
    public void add(char ch, long time) { this.add(new Letter(ch, time)); }

    public void clear() { this.dataSeq.clear(); }
    public Letter removeLast() {
        if(this.dataSeq.size() > 0)
            return this.dataSeq.remove(dataSeq.size()-1);
        return new Letter(KEY_NOT_FOUNT);
    }

    public String getDataAsString() {
        String ret = "";
        for(Letter l : dataSeq)
            ret += l.getChar();
        return ret;
    }


}
