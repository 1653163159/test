package com.example.test.Practice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.test.Exam.S1Fragment;
import com.example.test.Exam.S2Fragment;
import com.example.test.Exam.S3Fragment;
import com.example.test.R;
import com.example.test.tools.HanZiToPinYin;

import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.Locale;

public class PracticeActivity extends AppCompatActivity {
    TextToSpeech textToSpeech;
    S1Fragment lisFragment;
    S2Fragment writeFragment;
    S3Fragment speakFragment;
    Fragment showFragment;
    TextView title;
    Button speak;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice);

        initView();
    }

    void initView() {
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.setLanguage(Locale.CHINESE);//中文
                    textToSpeech.setSpeechRate(0.80f);
                }
            }
        });

        title = findViewById(R.id.title_practice);
        title.setText(getIntent().getStringExtra("title"));
        EditText editText = findViewById(R.id.copy_content);
        TextView pinyin = findViewById(R.id.pinyin);
        speak = findViewById(R.id.speak_copy_content);
        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().equals("")) return;
                if (textToSpeech != null && !textToSpeech.isSpeaking()) {
                    textToSpeech.setLanguage(Locale.CHINESE);
                    textToSpeech.setPitch(1.0f);// 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
                    textToSpeech.speak(editText.getText().toString(),
                            TextToSpeech.QUEUE_FLUSH, null);
                    try {
                        pinyin.setText(HanZiToPinYin.getPinYinAllChar(editText.getText().toString(), 1));
                    } catch (BadHanyuPinyinOutputFormatCombination e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        int type = getIntent().getIntExtra("type", 1);
        switch (type) {
            case 3:
                showFragment = S3Fragment.newInstance(getIntent().getStringExtra("prefix"), null);
                break;
            case 2:
                break;
            case 1:
                showFragment = S1Fragment.newInstance(getIntent().getStringExtra("prefix"), null);
        }
        getSupportFragmentManager().beginTransaction().add(R.id.practice_container, showFragment).commit();

    }

    public void back(View view) {
        onBackPressed();
    }
}