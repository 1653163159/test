package com.example.test.SubjectFragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test.Adapter.SubjectListViewAdapter;
import com.example.test.MainActivity;
import com.example.test.R;
import com.example.test.pojo.Subject;
import com.example.test.pojo.SubjectAnswer;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.jivesoftware.smack.SmackException;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SubjectActivity extends AppCompatActivity {

    TextView title, submit;
    ImageView back;

    /*
     * 播放器相关组件
     * */
    private boolean isPlay = false, isPause = false;//播放暂停标志
    private MediaPlayer mediaPlayer;//媒体播放器
    ImageView audioPlay;

    ListView subjectList;
    SubjectListViewAdapter subjectListViewAdapter;
    List<Subject> subjectItems = new ArrayList<>();
    List<SubjectAnswer> answerItems = new ArrayList<SubjectAnswer>();
    String subjectPath, answerPath, audioPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);
        try {
            initView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void initView() throws Exception {
        subjectPath = getIntent().getStringExtra("subject");
        answerPath = getIntent().getStringExtra("answer");
        audioPath = getIntent().getStringExtra("audio");
        title = findViewById(R.id.sub_title);
        submit = findViewById(R.id.sub_submit);
        back = findViewById(R.id.sub_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(SubjectActivity.this);
                dialog.setTitle("返回").setMessage("是否退出答题").setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onBackPressed();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(SubjectActivity.this);
                dialog.setTitle("返回").setMessage("是否提交").setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CalculateGrade();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
            }
        });
        title.setText(getIntent().getStringExtra("name"));
        audioPlay = findViewById(R.id.sub_audio_play);
        audioPlay.setOnClickListener(audioListener);
        subjectList = findViewById(R.id.subject_list);
        subjectList.setAdapter(subjectListViewAdapter = new SubjectListViewAdapter(getApplicationContext(), subjectItems));
        LoadSubject();
    }

    /**
     * 加载题目
     *
     * @throws Exception
     */
    void LoadSubject() throws Exception {
        BufferedReader fileInputStream = new BufferedReader(new InputStreamReader(new FileInputStream(subjectPath), "GBK"));
        StringBuilder builder = new StringBuilder();
        int ch;
        while ((ch = fileInputStream.read()) != -1) {
            builder.append((char) ch);
        }
        String sub = builder.toString();
        fileInputStream.close();
        JsonArray array = new JsonParser().parse(sub).getAsJsonArray();
        for (JsonElement element : array) {
            Subject subject = new Gson().fromJson(element, Subject.class);
            subjectItems.add(subject);
        }
        subjectListViewAdapter.notifyDataSetChanged();
        fileInputStream = new BufferedReader(new InputStreamReader(new FileInputStream(answerPath), "GBK"));
        builder = new StringBuilder();
        while ((ch = fileInputStream.read()) != -1) {
            builder.append((char) ch);
        }
        String ans = builder.toString();
        fileInputStream.close();
        array = new JsonParser().parse(ans).getAsJsonArray();
        for (JsonElement element : array) {
            SubjectAnswer answer = new Gson().fromJson(element, SubjectAnswer.class);
            answerItems.add(answer);
        }
    }

    /**
     * 计算成绩
     */
    private void CalculateGrade() {
        SubjectAnswer[] answers = subjectListViewAdapter.getAnswersAlphabet();
        int rightCount = 0, errorCount = 0;
        for (int i = 1; i < 102; i++) {
            if (answers[i] != null)
                if (answers[i].getAnswer().equals(answerItems.get(i - 1).getAnswer())) {
                    rightCount++;
                    System.out.println("true");
                } else {
                    errorCount++;
                    System.out.println("false");

                }
        }
        showGrade(rightCount, errorCount);
    }

    /**
     * 显示答题结果
     *
     * @param rightCount
     * @param errorCount
     */
    private void showGrade(int rightCount, int errorCount) {
        int summary = rightCount + errorCount;
        float acc = (float) rightCount / summary;
        DecimalFormat decimalFormat = new DecimalFormat("#%");
        AlertDialog.Builder dialog = new AlertDialog.Builder(SubjectActivity.this);
        dialog.setTitle("您的答题结果是：").setMessage("答题总数:" + summary + "\n正确数:" + rightCount + "\n错误数:" + errorCount + "\n正确率:" + decimalFormat.format(acc)).setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onBackPressed();
            }
        }).create().show();
    }

    private View.OnClickListener audioListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.sub_audio_play:
                    if (isPlay) {
                        if (mediaPlayer != null) {
                            if (isPause) {
                                mediaPlayer.start();
                                isPause = false;
                                audioPlay.setImageDrawable(getDrawable(R.drawable.ic_baseline_stop));
                            } else {
                                mediaPlayer.pause();
                                isPause = true;
                                audioPlay.setImageDrawable(getDrawable(R.drawable.ic_baseline_play));
                            }
                        }
                    } else {
                        playMusic();
                        audioPlay.setImageDrawable(getDrawable(R.drawable.ic_baseline_stop));
                        isPlay = true;
                    }
                    break;
            }
        }
    };

    /**
     * 初始化音乐播放器并启动
     */
    private void playMusic() {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioPath);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    audioPlay.setImageDrawable(getDrawable(R.drawable.ic_baseline_play));
                    mediaPlayer.release();
                    isPlay = false;
                    isPause = true;
                }
            });
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mediaPlayer != null) {
            audioPlay.setImageDrawable(getDrawable(R.drawable.ic_baseline_play));
            mediaPlayer.release();
            isPlay = false;
            isPause = true;
        }
    }
}