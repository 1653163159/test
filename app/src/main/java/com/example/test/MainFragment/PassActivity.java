package com.example.test.MainFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.test.Adapter.QuizViewPageAdapter;
import com.example.test.Exam.SubjectActivity;
import com.example.test.MainActivity;
import com.example.test.Practice.Flags;
import com.example.test.R;
import com.example.test.pojo.Pass;
import com.example.test.pojo.Quiz;
import com.example.test.tools.JsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PassActivity extends AppCompatActivity {
    OkHttpClient client = new OkHttpClient();

    int right = 0, wrong = 0;//统计正确题数，错误题数

    TextView title, practice_position;
    ImageView back;
    Button submit;
    ProgressBar bar;
    String titleName, prefix, level, path;
    int index, position;
    List<Pass> passStateList;

    ViewPager viewPager;
    QuizViewPageAdapter pageAdapter;
    List<View> quizViewList;
    List<Quiz> quizList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.cornFlowerBlue));
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass);
        initView();
        LoadPassState();
        int maxCount = 0;
        switch (level) {
            case Flags.BEGINNER:
                maxCount = Flags.QUIZ_BEGINNER_COUNT;
                break;
            case Flags.INTERMEDIATE:
                maxCount = Flags.QUIZ_INTERMEDIATE_COUNT;
                break;
            case Flags.ADVANCED:
                maxCount = Flags.QUIZ_ADVANCED_COUNT;
                break;
        }
        position = new Random().nextInt(maxCount);
        getQuizList(position);
        if (position > maxCount - 30) {
            position -= new Random().nextInt(100) + 100;
        }
        for (int i = 1; i < 3; i++) {
            position += i * 10;
            getQuizList(position);
        }
    }

    void initView() {
        passStateList = new ArrayList<>();
        titleName = getIntent().getStringExtra("title");
        prefix = getIntent().getStringExtra("prefix");
        level = getIntent().getStringExtra("level");
        index = getIntent().getIntExtra("index", 0);

        title = findViewById(R.id.title_practice);
        title.setText(titleName);
        practice_position = findViewById(R.id.practice_position);
        submit = findViewById(R.id.submit);
        back = findViewById(R.id.practice_back);
        back.setOnClickListener(v -> onBackPressed());
        bar = findViewById(R.id.pg_bar);
        viewPager = findViewById(R.id.quiz_list);
        quizViewList = new ArrayList<>();
        quizList = new ArrayList<>();
        viewPager.setAdapter(pageAdapter = new QuizViewPageAdapter(quizViewList));
        viewPager.setOnPageChangeListener(changeListener);
        submit.setOnClickListener(v -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(PassActivity.this);
            dialog.setTitle("提示").setMessage("是否提交").setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String temp = "不合格，请多努力";
                    if (right > 24) {
                        //修改通关次数
                        temp = "恭喜通关";
                        bar.setVisibility(View.VISIBLE);
                        int count = passStateList.get(index).getTimes();
                        if (count > 0) {
                            count -= 1;
                            passStateList.get(index).setTimes(count);
                        }
                        if (count == 0) {
                            if (index < passStateList.size() - 1) {
                                passStateList.get(index + 1).setState(PassFragment.STATE_UNLOCK);
                            }
                        }
                        UpdateJson();
                    }
                    //统计结果
                    new AlertDialog.Builder(PassActivity.this).setTitle("成绩" + temp).setMessage("正确数：" + right + "\n错误数：" + wrong).setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            onBackPressed();
                        }
                    }).create().show();
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();
        });
    }

    private void getQuizList(int position) {
        Request.Builder builder = null;
        String url = prefix + Flags.QUIZ_URL + level + File.separator + position;
        System.out.println(url);
        builder = new Request.Builder().url(url);
        execute(builder);
    }

    private void execute(Request.Builder builder) {
        Call call = client.newCall(builder.build());
        call.enqueue(resourceCallback);
    }

    private Callback resourceCallback = new Callback() {
        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {

        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            String str = new String(response.body().bytes(), "utf-8");
            Message message = updateQuizList.obtainMessage();
            message.obj = str;
            message.sendToTarget();
        }
    };

    Handler updateQuizList = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            String res = (String) msg.obj;
            JsonArray jsonArray = new JsonParser().parse(res).getAsJsonArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                Quiz quiz = new Gson().fromJson(jsonArray.get(i), Quiz.class);
                quizList.add(quiz);
                addView(quiz, i);
            }

            pageAdapter.notifyDataSetChanged();
            setSub_position((viewPager.getCurrentItem() + 1), quizViewList.size());
        }
    };

    private void addView(Quiz quiz, int i) {
        View view = LayoutInflater.from(this).inflate(R.layout.quiz_list, null);
        TextView textView = view.findViewById(R.id.quiz_title);
        textView.setText((position / 10 * 10 + i + 1) + "." + quiz.getContent());
        textView.setTextSize(20);
        RadioGroup group = view.findViewById(R.id.quiz_group);
        if (!quiz.getOptiona().equals("")) {
            RadioButton button = new RadioButton(this);
            RadioGroup.LayoutParams lp = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 50, 0, 50);
            button.setLayoutParams(lp);
            button.setText(quiz.getOptiona());
            button.setTextSize(18);
            button.setPadding(5, 5, 5, 5);
            button.setButtonDrawable(R.drawable.check_circle_default);
            group.addView(button);
        }
        if (!quiz.getOptionb().equals("")) {
            RadioButton button = new RadioButton(this);
            RadioGroup.LayoutParams lp = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 50, 0, 50);
            button.setLayoutParams(lp);
            button.setText(quiz.getOptionb());
            button.setTextSize(18);
            button.setPadding(5, 5, 5, 5);
            button.setButtonDrawable(R.drawable.check_circle_default);
            group.addView(button);
        }
        if (!quiz.getOptionc().equals("")) {
            RadioButton button = new RadioButton(this);
            RadioGroup.LayoutParams lp = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 50, 0, 50);
            button.setLayoutParams(lp);
            button.setText(quiz.getOptionc());
            button.setTextSize(18);
            button.setPadding(5, 5, 5, 5);
            button.setButtonDrawable(R.drawable.check_circle_default);
            group.addView(button);
        }
        if (!quiz.getOptiond().equals("")) {
            RadioButton button = new RadioButton(this);
            button.setText(quiz.getOptiond());
            RadioGroup.LayoutParams lp = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 50, 0, 50);
            button.setLayoutParams(lp);
            button.setTextSize(18);
            button.setPadding(5, 5, 5, 5);
            button.setButtonDrawable(R.drawable.check_circle_default);
            group.addView(button);
        }
        TextView answer = view.findViewById(R.id.quiz_answer);
        answer.setText("正确答案是：" + quiz.getAnswer());
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton button = view.findViewById(checkedId);
                if (button.getText().equals(quiz.getAnswer())) {
                    button.setButtonDrawable(R.drawable.check_circle_right);
                    right++;
                } else {
                    button.setButtonDrawable(R.drawable.check_circle__wrong);
                    wrong++;
                }
                for (int i = 0; i < group.getChildCount(); i++) {
                    group.getChildAt(i).setEnabled(false);
                }

                answer.setVisibility(View.VISIBLE);
            }
        });
        quizViewList.add(view);
    }

    /**
     * 翻页后更新一些显示文本
     */
    ViewPager.OnPageChangeListener changeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            setSub_position((position + 1), quizViewList.size());
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    /**
     * 加载关卡状态，目前采用本地保存，日后用户系统完善后可以将本地文件传给服务器，
     * 一个用户对应一个文件，并且通过对比服务器文件保证本地文件未被修改
     * 在此方法中可以更换为向服务器请求加载数据
     */
    void LoadPassState() {
        passStateList.clear();
        path = getExternalCacheDir().getAbsolutePath() + File.separator + "PassState.json";
        JsonArray jsonArray = null;
        try {
            String content = new JsonUtil().read(path);
            jsonArray = new JsonParser().parse(content).getAsJsonArray();
            for (JsonElement jsonElement : jsonArray) {
                Pass pass = new Gson().fromJson(jsonElement, Pass.class);
                passStateList.add(pass);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新本地文件
     */
    void UpdateJson() {
        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < passStateList.size(); i++) {
            jsonArray.add(new Gson().toJsonTree(passStateList.get(i)));
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(path);
            FileChannel fileChannel = fileOutputStream.getChannel();
            FileLock fileLock = fileChannel.tryLock();
            fileOutputStream.write(jsonArray.toString().getBytes(StandardCharsets.UTF_8));
            fileLock.release();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        bar.setVisibility(View.INVISIBLE);
    }

    public void setSub_position(int position, int max) {
        practice_position.setText(position + "/" + max);
    }
}