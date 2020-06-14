package com.example.audiokeyboard;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

public class TextSpeaker implements TextToSpeech.OnInitListener{

    final String TAG = "TextSpeaker";

    TextToSpeech tts;
    public float voiceSpeed = 100f;

    public TextSpeaker(Context context) {
        init(context);
    }

    void init(final Context context) {
        tts = new TextToSpeech(context, this);
        tts.setSpeechRate(voiceSpeed);
        tts.setPitch(1.0f);
    }

    public void setVoiceSpeed(float voiceSpeed) {
        this.voiceSpeed = voiceSpeed;
        this.tts.setSpeechRate(voiceSpeed);
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.ENGLISH);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "language not supported!");
            }
        }
        Log.e(TAG, "speaker init is over!");
    }

    public void speak(String text2speak) { speak(text2speak, true); }
    public void speak(String text2speak, boolean flush) {
        int mode = flush ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD;
        this.tts.speak(text2speak, mode, null, null);
    }

    public void stop() {
        this.tts.stop();
    }

}
