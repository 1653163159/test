package com.example.test.tools;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.Locale;

/**
 * @author : hqx
 * @date : 3/3/2023 上午 10:56
 * @descriptions: 文字转语音工具
 */
public class TextToSpeechUtil {
    TextToSpeech textToSpeech;

    public TextToSpeechUtil(Context context) {
        textToSpeech = new android.speech.tts.TextToSpeech(context, new android.speech.tts.TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                    textToSpeech.setLanguage(Locale.CHINESE);//中文
                    textToSpeech.setSpeechRate(0.85f);
                }
            }
        });
    }

    public void speak(String text) {
        if (textToSpeech != null && !textToSpeech.isSpeaking()) {
            textToSpeech.setLanguage(Locale.CHINESE);
            textToSpeech.setPitch(1.0f);// 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
            textToSpeech.speak(text,
                    TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public void releaseSpeech() {
        if (textToSpeech != null) {
            textToSpeech.stop(); // 不管是否正在朗读TTS都被打断
            textToSpeech.shutdown(); // 关闭，释放资源
        }
    }
}
