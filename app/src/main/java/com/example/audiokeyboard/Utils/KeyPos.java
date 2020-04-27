package com.example.audiokeyboard.Utils;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

public class KeyPos {
    final int KEYNUM = 33;
    public Key keys[];
    final float keyboardWidth = 1080f;
    final float keyboardHeight = 680f;
    float bottomThreshold;
    float topThreshold;
    float screen_height_ratio = 1f;
    float screen_width_ratio = 1f;
    float baseImageHeight;
    float baseImageWidth;

    float minHeightRatio = 0.5f;

    final float paddingTop = 1584 - keyboardHeight;     // This is the View's Height, I don't know how to get it before init;
    final float headerHeight = 210;                     // header Height
    public final float partialwindowSize = 1584;               // relative window size
    public final float wholewindowSize = 1794;                 // the whole size of the window

    final int Q=0;
    final int W=1;
    final int O=8;
    final int P=9;
    final int A=10;
    final int S=11;
    final int K=17;
    final int L=18;
    final int SHIFT=19;
    final int Z=20;
    final int X=21;
    final int N=25;
    final int M=26;
    final int BACKSPACE=27;
    final int SYMBOL=28;
    final int LANGUAGE=29;
    final int SPACE=30;
    final int COMMA=31;
    final int PERIOD=32;
    final int[] allChar={0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,20,21,22,23,24,25,26};
    final int[] keyPos={10,24,22,12,2,13,14,15,7,16,17,18,26,25,8,9,0,3,11,4,6,23,1,21,5,20};
    final String alphabet = "qwertyuiopasdfghjkl zxcvbnm       ";

    final int SCALING_NUM = 3;

    final char KEY_NOT_FOUND = '*';
    final char shiftCh=KEY_NOT_FOUND;
    final char symbolCh=KEY_NOT_FOUND;
    final char languageCh=KEY_NOT_FOUND;
    final char spaceCh=KEY_NOT_FOUND;
    final char commaCh=KEY_NOT_FOUND;
    final char periodCh=KEY_NOT_FOUND;
    final char backspaceCh=KEY_NOT_FOUND;

    final int INIT_LAYOUT = Key.MODE_INIT;
    final int VIP_LAYOUT = Key.MODE_VIP;

    final int GETKEY_STRICT = 1;
    final int GETKEY_LOOSE = 0;

    final int ADJUST_BODILY = 1;
    final int ADJUST_RESPECTIVELY = 0;
    int moveBodily = 0;

    final HashMap<Character,String> nearMapping = new HashMap<Character, String>() {
        {
            put('q', "wa"); put('w', "qase"); put('e', "wsdr"); put('r', "edft"); put('t', "rfgy");
            put('y', "tghu"); put('u', "yhji"); put('i', "uyko"); put('o', "iklp"); put('p', "ol");
            put('a', "qwsz"); put('s', "weadzx"); put('d', "ersfzxc"); put('f', "rtdgxcv"); put('g', "tyfhcvb");
            put('h', "yugjvbn"); put('j', "uihkbnm"); put('k', "iojlnm"); put('l', "opkm");
            put('z', "asdx"); put('x', "sdfzc"); put('c', "dfgxv"); put('v', "fghcb");
            put('b', "ghjvn"); put('n', "hjkbm"); put('m', "jkln");
        }
    };

    void initKeys() {
        keys = new Key[KEYNUM];
        for(int i=0;i<KEYNUM;i++) {
            keys[i] = new Key();
        }
        for (int i=Q;i<=P;i++){
            this.keys[i].init_x=this.keyboardWidth*(2*i+1)/20F;
            this.keys[i].init_y=this.keyboardHeight/8F;
        }
        for (int i=A;i<=L;i++){
            this.keys[i].init_x=(this.keys[i-(A-Q)].init_x+this.keys[i-(A-W)].init_x)/2F;
            this.keys[i].init_y=this.keyboardHeight*3F/8F;
        }
        for (int i=Z;i<=M;i++){
            this.keys[i].init_x=this.keys[i-(Z-S)].init_x;
            this.keys[i].init_y=this.keyboardHeight*5F/8F;
        }
        for (int i:allChar) {
            this.keys[i].init_height=this.keyboardHeight/4F;
            this.keys[i].init_width=this.keyboardWidth/10F;
        }

        // SHIFT
        this.keys[SHIFT].init_height=this.keyboardHeight/4F;
        this.keys[SHIFT].init_width=this.keys[Z].getLeft(INIT_LAYOUT);
        this.keys[SHIFT].init_x=this.keys[SHIFT].init_width/2F;
        this.keys[SHIFT].init_y=this.keys[Z].init_y;

        // BACKSPACE
        this.keys[BACKSPACE].init_height=this.keyboardHeight/4F;
        this.keys[BACKSPACE].init_width=this.keyboardWidth-this.keys[M].getRight(INIT_LAYOUT);
        this.keys[BACKSPACE].init_x=this.keys[M].getRight(INIT_LAYOUT)+this.keys[BACKSPACE].init_width/2F;
        this.keys[BACKSPACE].init_y=this.keys[M].init_y;

        // SYMBOL
        this.keys[SYMBOL].init_height=this.keyboardHeight/4F;
        this.keys[SYMBOL].init_width=this.keys[SHIFT].init_width;
        this.keys[SYMBOL].init_x=this.keys[SHIFT].init_x;
        this.keys[SYMBOL].init_y=this.keys[SHIFT].getBottom(INIT_LAYOUT)+this.keys[SYMBOL].init_height/2F;

        // LANGUAGE
        this.keys[LANGUAGE].init_height=this.keyboardHeight/4F;
        this.keys[LANGUAGE].init_width=this.keys[SYMBOL].init_width;
        this.keys[LANGUAGE].init_x=this.keys[SYMBOL].getRight(INIT_LAYOUT)+this.keys[LANGUAGE].init_width/2F;
        this.keys[LANGUAGE].init_y=this.keys[SYMBOL].init_y;

        // PERIOD
        this.keys[PERIOD].init_height=this.keyboardHeight/4F;
        this.keys[PERIOD].init_width=this.keys[BACKSPACE].init_width;
        this.keys[PERIOD].init_x=this.keys[BACKSPACE].init_x;
        this.keys[PERIOD].init_y=this.keys[SYMBOL].init_y;

        // COMMA
        this.keys[COMMA].init_height=this.keyboardHeight/4F;
        this.keys[COMMA].init_width=this.keys[BACKSPACE].init_width;
        this.keys[COMMA].init_x=this.keys[PERIOD].getLeft(INIT_LAYOUT)-this.keys[COMMA].init_width/2F;
        this.keys[COMMA].init_y=this.keys[SYMBOL].init_y;

        // SPACE
        this.keys[SPACE].init_height=this.keyboardHeight/4F;
        this.keys[SPACE].init_width=this.keys[COMMA].getLeft(INIT_LAYOUT)-this.keys[LANGUAGE].getRight(INIT_LAYOUT);
        this.keys[SPACE].init_x=this.keys[LANGUAGE].getRight(INIT_LAYOUT)+this.keys[SPACE].init_width/2F;
        this.keys[SPACE].init_y=this.keys[SYMBOL].init_y;

        for(int i=0;i<KEYNUM;i++) {
            keys[i].init_y += paddingTop;
            keys[i].ch = alphabet.charAt(i);
            keys[i].reset();
        }

    }

    void defaultParams() {
        screen_height_ratio = keyboardHeight / 907f;
        screen_width_ratio = keyboardWidth / 1440f;
        baseImageHeight = keyboardHeight;
        baseImageWidth = keyboardWidth;
        topThreshold = 0 * screen_height_ratio + paddingTop;
        bottomThreshold = 907 * screen_height_ratio + paddingTop;
    }

    public char getKeyByPosition(float x, float y, int mode, int getkey_mode) {
        char key = KEY_NOT_FOUND;
        if(y < topThreshold || y > bottomThreshold) return key;
        float min_dist = Float.MAX_VALUE;
        for(int i=0;i<KEYNUM;i++) {
            if(keys[i].getDist(x, y, mode) < min_dist) {
                key = keys[i].ch;
                min_dist = keys[i].getDist(x, y, mode);
            }
        }
        if(getkey_mode == GETKEY_LOOSE) {
            return key;
        }
        else if(key != KEY_NOT_FOUND && getkey_mode == GETKEY_STRICT){
            int index = this.keyPos[key-'a'];
            if(keys[index].contain(x, y, mode)) return key;
            else return KEY_NOT_FOUND;
        }
        return key;
    }

    public char getKeyByPosition(float x, float y, int mode) { return getKeyByPosition(x, y, mode, GETKEY_LOOSE); }

    public String getKeyAround(float x, float y, int mode, int getkey_mode) {
        char ch = getKeyByPosition(x, y, mode, getkey_mode);
        return getKeyAround(ch);
    }

    int getCharIndex(char ch) {
        if(ch >= 'a' && ch <= 'z')
            return allChar[ch-'a'];
        else
            return -1;
    }

    int getRowByIndex(int index) {
        if(index <= P) return 0;
        else if(index <=L) return 1;
        else return 2;
    }

    public String getKeyAround(char ch) {
        return nearMapping.get(ch);
    }

    // perform shift in one row
    boolean shiftx_linear(char ch, float dx) {                   // return whether the layout is changed
        int index = getCharIndex(ch);
        if(index == SHIFT || index == BACKSPACE || index == SYMBOL) return false;
        if(dx > 0)
            if(index == P || index == L) return false;
        else
            if(index == Q || index == A) return false;
        int row = getRowByIndex(index);
        if(row == -1) return false;
        int left = -1, right = -1;
        switch (row) {
            case 0:
                left = Q; right = P; break;
            case 1:
                left = A; right = L; break;
            case 2:
                left = Z; right = M; break;
        }

        shiftxByIndex_linear(index, left, right, dx);
        adjustxTo(row);

        return true;
    }

    void shiftxByIndex_linear(int index, int left, int right, float dx) {
        int begin = Math.min(index, Q+SCALING_NUM);             // only scale the first or the last letters
        int leftNum = begin - left + 1;
        int end = Math.max(index, P-SCALING_NUM);
        int rightNum = right - end + 1;
        float leftRatio = 2f / (leftNum+1) / leftNum;
        float rightRatio = 2f / (rightNum+1) / rightNum;
        for(int i=left;i<=begin;i++) {
            keys[i].curr_x = keys[i].init_x + (leftRatio*(i+1)*dx/2f);
            keys[i].curr_width = keys[i].curr_width + (leftRatio*(i+1)*dx);
        }
        for(int i=end;i<=right;i++) {
            keys[i].curr_x = keys[i].init_x + (rightRatio*(i+1)*dx/2f);
            keys[i].curr_width = keys[i].init_width + (rightRatio*(i+1)*dx);
        }
    }

    void adjustxTo(int row) {
        int left, right;
        if(moveBodily == ADJUST_RESPECTIVELY) {
            for(int i=0;i<3;i++) {
                if(i == row) continue;
                if(row == 0) { left = Q; right = P; }
                else if(row == 1) { left = A; right=L; }
                else { left = Z; right = M; }
                for(int j=left;j<=right;j++) {
                    keys[j].curr_x = keys[j].init_x;
                    keys[j].curr_width = keys[i].init_width;
                }
            }
        }
        assert moveBodily == ADJUST_BODILY;
        switch (row) {
            case 0:
                for(int i=A;i<=L;i++) {
                    keys[i].curr_x = (keys[i-(A-Q)].curr_x+keys[i-(A-W)].curr_x)/2f;
                    keys[i].curr_width = (keys[i-(A-Q)].curr_x-keys[i-(A-W)].curr_x);
                }
                for(int i=Z;i<=M;i++) {
                    keys[i].curr_x  = (keys[i-(Z-S)].curr_x);
                    keys[i].curr_width = (keys[i-(Z-S)].curr_width);
                }
                break;
            case 1:
                keys[Q].curr_x = keys[A].curr_x / 2f;
                keys[Q].curr_width = keys[A].curr_width;
                keys[P].curr_x = keyboardWidth - keys[L].curr_x;
                keys[P].curr_width = keyboardWidth - keys[L].curr_width / 2f;
                // match the first line
                for(int i=W;i<=O;i++) {
                    keys[i].curr_x = (keys[i+(A-W)].curr_x + keys[i+(S-W)].curr_x) / 2f;
                    keys[i].curr_width = keys[i+(S-W)].curr_x - keys[i+(A-W)].curr_x;
                }
                // match the third line
                for(int i=Z;i<=M;i++) {
                    keys[i].curr_x = keys[i-(Z-S)].curr_x;
                    keys[i].curr_width = keys[i-(Z-S)].curr_width;
                }
                break;
            case 2:
                // match the second line
                for(int i=S;i<K;i++) {
                    keys[i].curr_x = keys[i+(Z-S)].curr_x;
                    keys[i].curr_width = keys[i+(Z-S)].curr_width;
                }
                keys[A].curr_width = keys[S].getLeft(VIP_LAYOUT)*2f/3f;
                keys[A].curr_x = keys[S].curr_width;
                keys[L].curr_width = (keyboardWidth - keys[K].getRight(VIP_LAYOUT)*2f/3f);
                keys[L].curr_x = keys[K].getRight(VIP_LAYOUT)+this.keys[L].curr_width/2f;

                // match the first line
                keys[Q].curr_x = keys[A].curr_x/2f;
                keys[Q].curr_width = keys[A].curr_x;
                keys[P].curr_width = keyboardWidth - keys[L].curr_x;
                keys[P].curr_x = keyboardWidth - keys[L].curr_width / 2f;
                for(int i=W;i<=O;i++) {
                    keys[i].curr_x = (keys[i+(A-W)].curr_x+keys[i+(S-W)].curr_x) / 2f;
                    keys[i].curr_width = keys[i+(S-W)].curr_x - keys[i+(A-W)].curr_x;
                }
                break;
        }
    }

    // performm shift in the column
    boolean shifty_linear(char ch, float dy) {
        int index = getCharIndex(ch);
        if(index == SHIFT || index == BACKSPACE || index == SYMBOL)
            return false;
        if(dy>0){// downward movement, only compress
            if( keys[SYMBOL].init_y+dy>=bottomThreshold){                           // 超出了下界，这里面好像达不到这种情况？
                return false;
            }else{
                if(index>=SHIFT) //the third line
                {
                    for(int i=Q;i<=BACKSPACE;i++){
                        this.keys[i].curr_y=this.keys[i].init_y+dy;
                        this.keys[i].curr_height=this.keys[i].init_height;
                    }
                    for(int i=SYMBOL;i<=PERIOD;i++){
                        this.keys[i].curr_height=bottomThreshold-this.keys[SHIFT].getBottom(VIP_LAYOUT);
                        this.keys[i].curr_y=this.keys[SHIFT].getBottom(VIP_LAYOUT)+this.keys[i].curr_height/2F;

                    }
                }else if(index>=A)// the second line
                {
                    for(int i=Q;i<=L;i++) {                                             // 前两行的内容
                        this.keys[i].curr_y = this.keys[i].init_y + dy;
                        this.keys[i].curr_height = this.keys[i].init_height;
                    }
                    for(int i=SHIFT;i<=BACKSPACE;i++){
                        this.keys[i].curr_height=(bottomThreshold-this.keys[A].getBottom(VIP_LAYOUT))/2F;
                        this.keys[i].curr_y=this.keys[A].getBottom(VIP_LAYOUT)+this.keys[i].curr_height/2F;
                    }
                    for(int i=SYMBOL;i<=PERIOD;i++){
                        this.keys[i].curr_height=this.keys[SHIFT].curr_height;
                        this.keys[i].curr_y=this.keys[Z].getBottom(VIP_LAYOUT)+this.keys[i].curr_height/2F;
                    }
                }else // the first line
                {
                    for(int i=Q;i<=P;i++) {
                        this.keys[i].curr_y = this.keys[i].init_y + dy;
                        this.keys[i].curr_height = this.keys[i].init_height;
                    }
                    for(int i=A;i<=L;i++){
                        this.keys[i].curr_height=(bottomThreshold-this.keys[Q].getBottom(VIP_LAYOUT))/3F;
                        this.keys[i].curr_y=this.keys[Q].getBottom(VIP_LAYOUT)+this.keys[i].curr_height/2F;
                    }
                    for(int i=SHIFT;i<=BACKSPACE;i++){
                        this.keys[i].curr_height=this.keys[A].curr_height;
                        this.keys[i].curr_y=this.keys[A].getBottom(VIP_LAYOUT)+this.keys[i].curr_height/2F;
                    }
                    for(int i=SYMBOL;i<=PERIOD;i++){
                        this.keys[i].curr_height=this.keys[SHIFT].curr_height;
                        this.keys[i].curr_y=this.keys[SHIFT].getBottom(VIP_LAYOUT)+this.keys[i].curr_height/2F;
                    }
                }
            }
        }
        else {// upward movement, move or compress
            if (keys[Q].init_y + dy <= topThreshold) {
                return false;
            }
            if (keys[Q].getTop(INIT_LAYOUT) + dy > topThreshold) {// no compress
                for (int i = 0; i < KEYNUM; i++) {
                    keys[i].curr_y = keys[i].init_y + dy;
                    keys[i].curr_height = keys[i].init_height;
                }
            } else {// compress
                if (index <=L ){ // the first and the second line
                    for (int i = A; i <= PERIOD; i++) {
                        this.keys[i].curr_y = this.keys[i].init_y + dy;
                        this.keys[i].curr_height = this.keys[i].init_height;
                    }
                    for (int i = Q; i <= P; i++) {
                        this.keys[i].curr_height =this.keys[A].getTop(VIP_LAYOUT)-topThreshold;
                        this.keys[i].curr_y =topThreshold+this.keys[i].curr_height/2F;
                    }
                }else // the third line
                    for (int i = SHIFT; i <= PERIOD; i++) {
                        this.keys[i].curr_y = this.keys[i].init_y + dy;
                        this.keys[i].curr_height = this.keys[i].init_height;
                    }
                for (int i = Q; i <= P; i++) {
                    this.keys[i].curr_height = (this.keys[SHIFT].getTop(VIP_LAYOUT)-topThreshold)/2F;
                    this.keys[i].curr_y =topThreshold+keys[i].curr_height / 2F;
                }
                for (int i = A; i <= L; i++) {
                    this.keys[i].curr_height = this.keys[Q].curr_height;
                    this.keys[i].curr_y =this.keys[Q].getBottom(VIP_LAYOUT)+this.keys[i].curr_height / 2F;
                }
            }
        }

        float minHeight = minHeightRatio * (keyboardHeight / 4f);
        for (int i=0;i<KEYNUM;i++){
            if(this.keys[i].curr_height<minHeight){
                return false;
            }
        }
        return true;
    }

    // move Character ch to the location x and y;
    public boolean shift(char ch, float x, float y) {
        if(ch == KEY_NOT_FOUND) return false;
        int index = getCharIndex(ch);
        int qua = keys[index].containTap(x, y, INIT_LAYOUT);

        float dx = 0;
        float dy = 0;

        switch(qua){
            case 1:{
                dx=x-this.keys[index].getLeft_tap(INIT_LAYOUT);
                dy=y-this.keys[index].getTop_tap(INIT_LAYOUT);
                break;
            }
            case 2:{
                dx=0;
                dy=y-this.keys[index].getTop_tap(INIT_LAYOUT);
                break;
            }case 3:{
                dx=x-this.keys[index].getRight_tap(INIT_LAYOUT);
                dy=y-this.keys[index].getTop_tap(INIT_LAYOUT);
                break;
            }case 4:{
                dx=x-this.keys[index].getLeft_tap(INIT_LAYOUT);
                dy=0;
                break;
            }case 5:{
                dx=0;
                dy=0;
                break;
            }case 6:{
                dx=x-this.keys[index].getRight_tap(INIT_LAYOUT);
                dy=0;
                break;
            }
            case 7:{
                dx=x-this.keys[index].getLeft_tap(INIT_LAYOUT);
                dy=y-this.keys[index].getBottom_tap(INIT_LAYOUT);
                break;
            }case 8:{
                dx=0;
                dy=y-this.keys[index].getBottom_tap(INIT_LAYOUT);
                break;
            }case 9:{
                dx=x-this.keys[index].getRight_tap(INIT_LAYOUT);
                dy=y-this.keys[index].getBottom_tap(INIT_LAYOUT);
                break;
            }
        }
        return shiftx_linear(ch,dx)&&shifty_linear(ch,dy);
    }

    public KeyPos() {
        defaultParams();
        initKeys();
    }

}
