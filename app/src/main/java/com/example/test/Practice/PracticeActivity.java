package com.example.test.Practice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.test.Exam.S1Fragment;
import com.example.test.Exam.S3Fragment;
import com.example.test.R;
import com.example.test.tools.HanZiToPinYin;
import com.example.test.tools.TextToSpeechUtil;

import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class PracticeActivity extends AppCompatActivity {
    TextToSpeechUtil speechUtil;
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
        speechUtil = new TextToSpeechUtil(getApplicationContext());
        title = findViewById(R.id.title_practice);
        title.setText(getIntent().getStringExtra("title"));
        EditText editText = findViewById(R.id.copy_content);
        TextView pinyin = findViewById(R.id.pinyin);
        speak = findViewById(R.id.speak_copy_content);
        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().equals("")) return;
                speechUtil.speak(editText.getText().toString());
                try {
                    pinyin.setText(HanZiToPinYin.getPinYinAllChar(editText.getText().toString(), 1));
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            }
        });

        String type = getIntent().getStringExtra("type");
        switch (type) {
            case Flags.SPEAK_TYPE1:
                showFragment = S3Fragment.newInstance(getIntent().getStringExtra("prefix"), null);
                break;
            case Flags.WRITE_TYPE1:
                break;
            case Flags.LISTEN_TYPE1:
                showFragment = S1Fragment.newInstance(getIntent().getStringExtra("prefix"), null);
        }
        getSupportFragmentManager().beginTransaction().add(R.id.practice_container, showFragment).commit();

    }

    public void back(View view) {
        onBackPressed();
    }
}