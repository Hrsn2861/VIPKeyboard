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

    public void add(Letter l) { this.dataSeq.add(l); }
    public void add(char ch) { this.add(new Letter(ch)); }
    public void add(char ch, long time) { this.add(new Letter(ch, time)); }

    public void clear() { this.dataSeq.clear(); }
    public Letter removeLast() { return this.dataSeq.remove(dataSeq.size()-1); }


}
