package com.example.audiokeyboard;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.Locale;

public class TextSpeaker {
    TextToSpeech tts;
    public float voiceSpeed = 2.0f;

    public TextSpeaker(Context context) {
        init(context);
    }

    void init(final Context context) {
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status){
                if (status == tts.SUCCESS){
                    int result = tts.setLanguage(Locale.ENGLISH);
                    if (result != TextToSpeech.LANG_COUNTRY_AVAILABLE && result != TextToSpeech.LANG_AVAILABLE){
                        Toast.makeText(context, "TTS暂时不支持英文朗读！", Toast.LENGTH_SHORT);
                    }
                }
            }
        });
        tts.setSpeechRate(voiceSpeed);
        tts.setPitch(1.0f);
    }

    public void speak(String text2speak) {

    }

}
