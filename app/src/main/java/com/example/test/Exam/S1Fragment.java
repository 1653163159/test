package com.example.test.Exam;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.example.test.R;
import com.example.test.pojo.AudioBank;
import com.example.test.pojo.ListeningQuestion;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link S1Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
/*
 * 听力题练习
 * */
public class S1Fragment extends Fragment {

    View lis;//获取对应Layout
    Activity curActivity;//获取当前Activity
    String requestType;//向服务器请求数据的类型

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    /*
     * 播放器相关组件
     * */
    private boolean isPlay = false, isPause = false;//播放暂停标志
    private MediaPlayer mediaPlayer;//媒体播放器
    private ImageView tv_play;//播放按钮
    private TextView audioName;

    /*
     * 题目相关组件
     * */
    private int questionId = 2;                   //题目编号
    private TextView questionContent;
    private RadioGroup radioGroup;
    private RadioButton btn1, btn2, btn3, btn4;
    private int audioId, pictureId;                        //听力音频编号
    private String listeningAnswer;             //听力题答案
    private String path = "/";                  //资源所在路径

    /*
     * 服务器请求设置
     * */
    private final OkHttpClient client = new OkHttpClient();
    String prefix = "http://192.168.2.211:8080/rest/";

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    //id绑定
    private void initBind() {
        audioName = lis.findViewById(R.id.audio_name);
        tv_play = lis.findViewById(R.id.tv_play);
        tv_play.setOnClickListener(audioListener);
        radioGroup = lis.findViewById(R.id.answer_group);
        questionContent = lis.findViewById(R.id.question);
    }

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public S1Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment S1Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static S1Fragment newInstance(String param1, String param2) {
        S1Fragment fragment = new S1Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            prefix = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        lis = inflater.inflate(R.layout.fragment_s1, container, false);
        curActivity = getActivity();
        initBind();

        getQuestion(questionId);
        Log.e("err", prefix);

        return lis;
    }

    private View.OnClickListener audioListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.tv_play:
                    if (isPlay) {
                        if (mediaPlayer != null) {
                            if (isPause) {
                                mediaPlayer.start();
                                isPause = false;
                                tv_play.setImageDrawable(curActivity.getDrawable(R.drawable.ic_baseline_stop));
                            } else {
                                mediaPlayer.pause();
                                isPause = true;
                                tv_play.setImageDrawable(curActivity.getDrawable(R.drawable.ic_baseline_play));
                            }
                        }
                    } else {
                        getQuestionResource(requestType);
                        tv_play.setImageDrawable(curActivity.getDrawable(R.drawable.ic_baseline_stop));
                        isPlay = true;
                    }
                    break;
            }
        }
    };

    /*
     * 初始化音乐播放器并启动
     * */
    private void playMusic() {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(path);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    tv_play.setImageDrawable(curActivity.getDrawable(R.drawable.ic_baseline_play));
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

    /*
     * 释放音乐资源
     * */
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            audioName.setText("点击播放音频");
            tv_play.setImageDrawable(curActivity.getDrawable(R.drawable.ic_baseline_play));
            mediaPlayer.release();
            isPlay = false;
            isPause = true;
        }
        resetRadioGroup();
    }

    /*
     * 更新题目组件文本
     * */
    private void setQuestion() {
        btn1 = (RadioButton) radioGroup.getChildAt(0);
        btn2 = (RadioButton) radioGroup.getChildAt(1);
        btn3 = (RadioButton) radioGroup.getChildAt(2);
        btn4 = (RadioButton) radioGroup.getChildAt(3);
        btn1.setChecked(false);
        btn2.setChecked(false);
        btn3.setChecked(false);
        btn4.setChecked(false);
        radioGroup.setEnabled(true);
        btn1.setEnabled(true);
        btn2.setEnabled(true);
        btn3.setEnabled(true);
        btn4.setEnabled(true);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                String chooseAnswer = null;
                int index = 0;
                switch (i) {
                    case R.id.ans_a:
                        chooseAnswer = (String) btn1.getText();
                        index = 0;
                        break;
                    case R.id.ans_b:
                        chooseAnswer = (String) btn2.getText();
                        index = 1;
                        break;
                    case R.id.ans_c:
                        chooseAnswer = (String) btn3.getText();
                        index = 2;
                        break;
                    case R.id.ans_d:
                        chooseAnswer = (String) btn4.getText();
                        index = 3;
                        break;
                }
                if (chooseAnswer.equals(listeningAnswer)) {
                    radioGroup.getChildAt(index).setBackgroundResource(R.color.limeGreen);
                } else {
                    radioGroup.getChildAt(index).setBackgroundResource(R.color.red);
                }
                radioGroup.setEnabled(false);
                btn1.setEnabled(false);
                btn2.setEnabled(false);
                btn3.setEnabled(false);
                btn4.setEnabled(false);
            }
        });
    }

    /*
     * 请求题目内容
     * */
    private void getQuestion(int questionId) {
        setQuestion();
        Request.Builder builder = null;
        builder = new Request.Builder().url(prefix + "ListeningQuestionSingle/" + questionId);
        if (builder != null) {
            execute(builder, 1);
        }
    }

    /*
     * 请求题目资源
     * */
    private void getQuestionResource(String type) {
        Request.Builder builder = null;
        builder = new Request.Builder().url(prefix + "AudioBankSingle/" + audioId);
        execute(builder, 2);
    }

    /*
     * 执行请求,type=1表示题目内容请求，type=2表示题目资源请求
     * */
    private void execute(Request.Builder builder, int type) {
        Call call = client.newCall(builder.build());
        switch (type) {
            case 1:
                call.enqueue(questionCallback);
                break;
            case 2:
                call.enqueue(resourceCallback);
                break;
        }
    }

    /**
     * 回调请求题目内容
     **/
    private Callback questionCallback = new Callback() {
        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            e.printStackTrace();
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            String str = new String(response.body().bytes(), "utf-8");
            Message message = updateSubContent.obtainMessage();
            message.obj = str;
            message.sendToTarget();
        }
    };

    /**
     * 回调请求题目资源
     **/
    private Callback resourceCallback = new Callback() {
        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            e.printStackTrace();
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            String str = new String(response.body().bytes(), "utf-8");
            Message message = updateSubResource.obtainMessage();
            message.obj = str;
            message.sendToTarget();
        }
    };
    /**
     * 更新题目内容,检测id，1表示请求听力题，2表示请求书法题，3表示请求口语题
     **/

    private Handler updateSubContent = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String res = (String) msg.obj;
            if (res == null || res.equals("")) return;
            Log.e("TAG", "handleMessage: " + res);
            Gson gson = new Gson();
            ListeningQuestion(gson, res);
        }
    };


    /**
     * 更新资源
     **/

    private Handler updateSubResource = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.e("TAG", "handleMessage: " + msg);
            String res = (String) msg.obj;
            if (res == null || res.equals("")) return;
            Log.e("TAG2", "handleMessage: " + res);
            Gson gson = new Gson();
            try {
                getAudioResource(gson, res);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 更新题目内容
     **/
    private void ListeningQuestion(Gson gson, String res) {
        ListeningQuestion l1 = gson.fromJson(res, ListeningQuestion.class);
        questionContent.setText(l1.getContent());
        btn1.setText(l1.getOption1());
        btn2.setText(l1.getOption2());
        btn3.setText(l1.getOption3());
        btn4.setText(l1.getOption4());
        listeningAnswer = l1.getAnswer();
        audioId = l1.getAudio_id();
    }

    /**
     * 更新题目资源
     **/
    private void getAudioResource(Gson gson, String res) throws IOException {
        AudioBank a1 = gson.fromJson(res, AudioBank.class);
        audioName.setText(a1.getAudio_name());
        //Json传递的byte数组自动经过Base64编码，所以服务器端不需处理，在客户端进行解码，可以正常播放
        byte[] bytes = Base64.decode(a1.getAudio_content(), Base64.DEFAULT);
        //向本机缓存目录写入音频文件
        path = curActivity.getExternalCacheDir().getAbsolutePath() + File.separator + a1.getAudio_name() + ".mp3";
        FileOutputStream fileOutputStream = new FileOutputStream(path);
        Log.e("TAG", "getAudioResource: " + bytes);
        Log.e("TAG", "getAudioResource: " + path);
        fileOutputStream.write(bytes);
        fileOutputStream.close();
        playMusic();
    }

    public void resetRadioGroup() {
        btn1.setChecked(false);
        btn2.setChecked(false);
        btn3.setChecked(false);
        btn4.setChecked(false);
        radioGroup.setEnabled(true);
        btn1.setEnabled(true);
        btn2.setEnabled(true);
        btn3.setEnabled(true);
        btn4.setEnabled(true);
        btn1.setBackgroundResource(R.color.whiteSmoke);
        btn2.setBackgroundResource(R.color.whiteSmoke);
        btn3.setBackgroundResource(R.color.whiteSmoke);
        btn4.setBackgroundResource(R.color.whiteSmoke);
    }

    public void next() {
        onDestroy();
        questionId += 1;
        getQuestion(questionId);
    }

    public void before() {
        onDestroy();
        questionId -= 1;
        getQuestion(questionId);
    }
}
