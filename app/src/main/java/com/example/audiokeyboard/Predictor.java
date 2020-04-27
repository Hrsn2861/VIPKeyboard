package com.example.audiokeyboard;

import com.example.audiokeyboard.Utils.Key;
import com.example.audiokeyboard.Utils.KeyPos;

import java.util.ArrayList;

public class Predictor {
    double predictRange = 0.5;

    public Predictor() {

    }

    public char getMostPossibleKey(String s) {
        char ch = 'a';
        return ch;
    }

    public ArrayList<String> getCandidate(String s) {
        ArrayList<String> ret = new ArrayList<>();
        return ret;
    }

}
