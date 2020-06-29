package com.example.audiokeyboard.Utils;

import java.util.ArrayList;
import java.util.HashMap;

public class PinyinCandidateList {
    ArrayList<PinyinCandidate> pinyinCandidates;

    HashMap<Integer, Integer> candidateIndex2Length;
    HashMap<Integer, String> candidateIndex2Pinyin;

    public PinyinCandidateList() {
        pinyinCandidates = new ArrayList<>();
        candidateIndex2Length = new HashMap<>();
        candidateIndex2Pinyin = new HashMap<>();
    }

    public void clear() {
        pinyinCandidates.clear();
        candidateIndex2Length.clear();
        candidateIndex2Pinyin.clear();
    }

    public void add(int ChoiceIndex, int PinyinCandidateIndex, int TotalIndex, String pinyin, String hanzi) {
        this.pinyinCandidates.add(new PinyinCandidate(ChoiceIndex, PinyinCandidateIndex, TotalIndex, pinyin, hanzi));
        addLength(PinyinCandidateIndex);
        addPinyin(PinyinCandidateIndex, pinyin);
    }

    void addPinyin(int pinyinCandidateIndex, String pinyin) {
        candidateIndex2Pinyin.put(pinyinCandidateIndex, pinyin);
    }

    void addLength(int pinyinCandidateIndex) {
        candidateIndex2Length.put(pinyinCandidateIndex, candidateIndex2Length.getOrDefault(pinyinCandidateIndex, 0)+1);
    }

    public int getLength(int index) {
        return candidateIndex2Length.getOrDefault(index, 0);
    }

    public String getPinyin(int index) {
        return candidateIndex2Pinyin.getOrDefault(index, "");
    }

    public String getHanzi(int index) {
        try {
            return pinyinCandidates.get(index).getHanzi();
        } catch(Exception e) {
            e.printStackTrace();
            return "out of bounds";
        }
    }

    public PinyinCandidate get(int index) {
        try {
            return pinyinCandidates.get(index);
        } catch(Exception e) {
            e.printStackTrace();
            return new PinyinCandidate(0, 0, 0, "", "");
        }
    }

    public int size() {
        return pinyinCandidates.size();
    }

}
