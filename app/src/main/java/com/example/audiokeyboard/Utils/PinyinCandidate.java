package com.example.audiokeyboard.Utils;

public class PinyinCandidate {
    int ChoiceIndex;                                // 在choicelist里面的位置
    int PinyinCandidateIndex;                       // 是第几个拼音拼出来的汉字;
    int TotalIndex;
    String pinyin;
    String hanzi;

    public PinyinCandidate(int choiceIndex, int pinyinCandidateIndex, int totalIndex, String pinyin, String hanzi) {
        this.ChoiceIndex = choiceIndex;
        this.PinyinCandidateIndex = pinyinCandidateIndex;
        this.TotalIndex = totalIndex;
        this.pinyin = pinyin;
        this.hanzi = hanzi;
    }

    public PinyinCandidate() {
        this.ChoiceIndex = 0;
        this.PinyinCandidateIndex = 0;
        this.TotalIndex = 0;
        this.pinyin = "";
        this.hanzi = "";
    }

    public int getChoiceIndex() {
        return ChoiceIndex;
    }

    public int getPinyinCandidateIndex() {
        return PinyinCandidateIndex;
    }

    public int getTotalIndex() {
        return TotalIndex;
    }

    public String getPinyin() {
        return pinyin;
    }

    public String getHanzi() {
        return hanzi;
    }

    public void setChoiceIndex(int choiceIndex) {
        ChoiceIndex = choiceIndex;
    }

    public void setPinyinCandidateIndex(int pinyinCandidateIndex) {
        PinyinCandidateIndex = pinyinCandidateIndex;
    }

    public void setTotalIndex(int totalIndex) {
        TotalIndex = totalIndex;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public void setHanzi(String hanzi) {
        this.hanzi = hanzi;
    }
}
