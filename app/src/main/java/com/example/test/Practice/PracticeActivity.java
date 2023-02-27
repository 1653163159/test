package com.example.test.Practice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.test.Exam.S1Fragment;
import com.example.test.Exam.S2Fragment;
import com.example.test.Exam.S3Fragment;
import com.example.test.R;

public class PracticeActivity extends AppCompatActivity {
    S1Fragment lisFragment;
    S2Fragment writeFragment;
    S3Fragment speakFragment;
    Fragment showFragment;
    TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice);

        initView();
    }

    void initView() {
        title = findViewById(R.id.title_practice);
        title.setText(getIntent().getStringExtra("title"));
        int type = getIntent().getIntExtra("type", 1);
        switch (type) {
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