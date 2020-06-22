package com.example.audiokeyboard.Utils;

public class Word implements Comparable<Word>{
    String text, alias;
    public double freq;
    public Word(String text, double freq){
        this.text = this.alias = text;
        this.freq = freq;
    }

    public Word(Word tmp){
        this.text = tmp.text;
        this.alias = tmp.alias;
        this.freq = tmp.freq;
    }

    @Override
    public int compareTo(Word o){
        if (this.freq > o.freq) return -1;
        if (this.freq < o.freq) return 1;
        return 0;
    }

    public String getText() {
        return text;
    }

    public double getFreq() {
        return freq;
    }

    @Override
    public String toString() {
        return "Word{" +
                "text='" + text + '\'' +
                ", alias='" + alias + '\'' +
                ", freq=" + freq +
                '}';
    }
}
