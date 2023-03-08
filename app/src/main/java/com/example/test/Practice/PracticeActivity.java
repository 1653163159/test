package com.example.test.Practice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.test.Exam.S1Fragment;
import com.example.test.Exam.S2Fragment;
import com.example.test.Exam.S3Fragment;
import com.example.test.R;
import com.example.test.tools.HanZiToPinYin;
import com.example.test.tools.TextToSpeechUtil;

import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class PracticeActivity extends FragmentActivity {
    TextToSpeechUtil speechUtil;
    Fragment showFragment;
    TextView title;
    Button speak;
    String titleName, type, prefix, level;
    TextView sub_position;
    ImageView subject_submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice);

        initView();
    }

    void initView() {
        titleName = getIntent().getStringExtra("title");
        type = getIntent().getStringExtra("type");
        prefix = getIntent().getStringExtra("prefix");
        level = getIntent().getStringExtra("level");

        speechUtil = new TextToSpeechUtil(getApplicationContext());
        title = findViewById(R.id.title_practice);
        title.setText(titleName);
        sub_position = findViewById(R.id.practice_position);
        subject_submit = findViewById(R.id.practice_submit);
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
        switch (type) {
            case Flags.SPEAK_TYPE1:
                showFragment = WordSpeechFragment.newInstance(prefix, level);
                break;
            case Flags.WRITE_TYPE1:
                showFragment = WordsWriteFragment.newInstance(prefix, level);
                break;
            case Flags.WRITE_TYPE3:
                showFragment = QuizFragment.newInstance(prefix, level);
                break;
            case Flags.WRITE_TYPE2:
                showFragment = CompositionFragment.newInstance(prefix, level);
                break;
            case Flags.LISTEN_TYPE1:
                showFragment = S1Fragment.newInstance(prefix, level);
                break;
        }
        getSupportFragmentManager().beginTransaction().add(R.id.practice_container, showFragment).commit();
    }

    public void setSub_position(String text) {
        sub_position.setText(text);
    }

    public void back(View view) {
        onBackPressed();
    }
}