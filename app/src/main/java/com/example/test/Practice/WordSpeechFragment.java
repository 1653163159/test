package com.example.test.Practice;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baidu.aip.asrwakeup3.core.mini.AutoCheck;
import com.baidu.aip.asrwakeup3.core.util.AuthUtil;
import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.example.test.Adapter.QuizViewPageAdapter;
import com.example.test.R;
import com.example.test.pojo.ASRresponse;
import com.example.test.pojo.Composition;
import com.example.test.pojo.Word;
import com.example.test.tools.TextToSpeechUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WordSpeechFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WordSpeechFragment extends Fragment {
    OkHttpClient client = new OkHttpClient();
    EventManager asr;
    protected boolean enableOffline = false; // 测试离线命令词，需要改成true
    int position = 0;
    View wordSpeechFragment;
    Activity curActivity;

    SwipeRefreshLayout refreshLayout;
    ViewPager viewPage;
    PagerAdapter pagerAdapter;
    List<View> wordSpeechViewList;
    List<Word> wordSpeechList;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String prefix;
    private String level;

    public WordSpeechFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WordSpeechFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WordSpeechFragment newInstance(String param1, String param2) {
        WordSpeechFragment fragment = new WordSpeechFragment();
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
            level = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (wordSpeechFragment == null) {
            wordSpeechFragment = inflater.inflate(R.layout.fragment_word_speech, container, false);

        }
        curActivity = getActivity();
        initPermission();
        initView();
        asr = EventManagerFactory.create(curActivity.getApplicationContext(), "asr");
        asr.registerListener(yourListener);
        if (enableOffline) {
            loadOfflineEngine(); // 测试离线命令词请开启, 测试 ASR_OFFLINE_ENGINE_GRAMMER_FILE_PATH 参数时开启
        }
        getWordList(level, position);
        refreshLayout.setRefreshing(true);
        return wordSpeechFragment;
    }

    private void initView() {
        refreshLayout = wordSpeechFragment.findViewById(R.id.word_speech_refresh);
        refreshLayout.setOnRefreshListener(refreshListener);
        refreshLayout.setColorSchemeResources(R.color.colorYang, R.color.aliceBlue);
        refreshLayout.setProgressViewOffset(true, 0, 50);
        viewPage = wordSpeechFragment.findViewById(R.id.word_speech_list);
        wordSpeechViewList = new ArrayList<>();
        wordSpeechList = new ArrayList<>();
        viewPage.setAdapter(pagerAdapter = new QuizViewPageAdapter(wordSpeechViewList));
        viewPage.setOnPageChangeListener(changeListener);
        viewPage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    System.out.println(event.getRawX());
                    WindowManager wm = (WindowManager) curActivity.getSystemService(getContext().WINDOW_SERVICE);
                    int width = wm.getDefaultDisplay().getWidth();
                    int temp = width / 3;
                    if (event.getRawX() > width / 2 + temp) {
                        viewPage.setCurrentItem(viewPage.getCurrentItem() + 1);
                    }
                    if (event.getRawX() < width / 2 - temp) {
                        viewPage.setCurrentItem(viewPage.getCurrentItem() - 1);
                    }
                }
                return false;
            }
        });
    }

    private void getWordList(String level, int position) {
        Request.Builder builder = null;
        String url = prefix + Flags.WORD_URL + level + File.separator + position;
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
            Message message = updateWordList.obtainMessage();
            message.obj = str;
            message.sendToTarget();
        }
    };

    Handler updateWordList = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            clear();
            String res = (String) msg.obj;
            JsonArray jsonArray = new JsonParser().parse(res).getAsJsonArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                Word word = new Gson().fromJson(jsonArray.get(i), Word.class);
                wordSpeechList.add(word);
                addView(word, i);
            }
            pagerAdapter.notifyDataSetChanged();
            setSub_position((position % 10 + 1) + "/" + wordSpeechList.size());
            refreshLayout.setRefreshing(false);
        }
    };

    private void addView(Word word, int i) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_s3, null);
        Typeface typeface = Typeface.createFromAsset(curActivity.getAssets(), "fonts/标准楷体简.ttf");
        TextToSpeechUtil speechUtil = new TextToSpeechUtil(getContext());
        TextView content = view.findViewById(R.id.speech_title);
        content.setText(word.getContent());
        content.setTypeface(typeface);
        content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speechUtil.speak(content.getText().toString());
            }
        });
        TextView answer = view.findViewById(R.id.speech_text);
        answer.setTypeface(typeface);
        Button record = view.findViewById(R.id.audio_record);
        record.setTypeface(typeface);
        ProgressBar bar = view.findViewById(R.id.pg_word_record_bar);
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bar.setVisibility(View.VISIBLE);
                bar.bringToFront();
                String json = "{\"accept-audio-data\":false,\"disable-punctuation\":false,\"accept-audio-volume\":true,\"pid\":1736}";
                start();
            }
        });
        wordSpeechViewList.add(view);
    }


    /**
     * 更新题目总数和位置
     *
     * @param str
     */
    void setSub_position(String str) {
        PracticeActivity activity = (PracticeActivity) getActivity();
        activity.setSub_position(str);
    }

    private SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            refresh();
        }
    };
    ViewPager.OnPageChangeListener changeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            setSub_position((position % 10 + 1) + "/" + wordSpeechList.size());
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == 1) {
                if (viewPage.getCurrentItem() == wordSpeechList.size() - 1) {
                    refreshLayout.setRefreshing(true);
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                sleep(1000);
                                refreshHandler.sendEmptyMessage(1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }
            }
        }
    };

    Handler refreshHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            refresh();
        }
    };

    private void refresh() {
        int count = getMaxCount(level);
        position += 10;
        if (position > count) {
            Random random = new Random();
            position = random.nextInt(count);
        }
        getWordList(level, position);
        viewPage.setCurrentItem(0);
    }

    /**
     * 根据难度等级获取对应级别的数据总量
     *
     * @param level
     * @return
     */
    public int getMaxCount(String level) {
        int count = 0;
        switch (level) {
            case Flags.BEGINNER:
                count = Flags.WORD_BEGINNER_COUNT;
                break;
            case Flags.INTERMEDIATE:
                count = Flags.WORD_INTERMEDIATE_COUNT;
                break;
            case Flags.ADVANCED:
                count = Flags.WORD_ADVANCED_COUNT;
                break;
        }
        return count;
    }

    private void clear() {
        wordSpeechViewList.clear();
        wordSpeechList.clear();
        pagerAdapter.notifyDataSetChanged();
    }

    //---------------------------------------------------------------------------------------------------//
    /*
     * 以下是语音识别部分
     * */

    /**
     * 基于SDK集成2.2 发送开始事件
     * 点击开始按钮
     * 测试参数填在这里
     */
    private void start() {
        Map<String, Object> params = AuthUtil.getParam();
        String event = null;
        event = SpeechConstant.ASR_START; // 替换成测试的event

        if (enableOffline) {
            params.put(SpeechConstant.DECODER, 2);
        }
        // 基于SDK集成2.1 设置识别参数
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        (new AutoCheck(curActivity.getApplicationContext(), new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainErrorMessage(); // autoCheck.obtainAllMessage();
                        ; // 可以用下面一行替代，在logcat中查看代码
                        // Log.w("AutoCheckMessage", message);
                    }
                }
            }
        }, enableOffline)).checkAsr(params);
        String json = null; // 可以替换成自己的json
        json = new JSONObject(params).toString(); // 这里可以替换成你需要测试的json
        asr.send(event, json, null, 0, 0);
    }

    /**
     * 点击停止按钮
     * 基于SDK集成4.1 发送停止事件
     */
    private void stop() {
        asr.send(SpeechConstant.ASR_STOP, null, null, 0, 0); //
    }


    /**
     * enableOffline设为true时，在onCreate中调用
     * 基于SDK离线命令词1.4 加载离线资源(离线时使用)
     */
    private void loadOfflineEngine() {
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put(SpeechConstant.DECODER, 2);
        params.put(SpeechConstant.ASR_OFFLINE_ENGINE_GRAMMER_FILE_PATH, "assets://baidu_speech_grammar.bsg");
        asr.send(SpeechConstant.ASR_KWS_LOAD_ENGINE, new JSONObject(params).toString(), null, 0, 0);
    }

    /**
     * enableOffline为true时，在onDestory中调用，与loadOfflineEngine对应
     * 基于SDK集成5.1 卸载离线资源步骤(离线时使用)
     */
    private void unloadOfflineEngine() {
        asr.send(SpeechConstant.ASR_KWS_UNLOAD_ENGINE, null, null, 0, 0); //
    }

    @Override
    public void onPause() {
        super.onPause();
        asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
        Log.i("ActivityMiniRecog", "On pause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 基于SDK集成4.2 发送取消事件
        asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
        if (enableOffline) {
            unloadOfflineEngine(); // 测试离线命令词请开启, 测试 ASR_OFFLINE_ENGINE_GRAMMER_FILE_PATH 参数时开启
        }

        // 基于SDK集成5.2 退出事件管理器
        // 必须与registerListener成对出现，否则可能造成内存泄露
        asr.unregisterListener(yourListener);
    }

    EventListener yourListener = new EventListener() {
        @Override
        public void onEvent(String name, String params, byte[] data, int offset, int length) {
            if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_READY)) {
                // 引擎就绪，可以说话，一般在收到此事件后通过UI通知用户可以说话了
            }
            if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {
                // 识别相关的结果都在这里
                if (params == null || params.isEmpty()) {
                    return;
                }
                if (params.contains("\"final_result\"")) {
                    // 一句话的最终识别结果
                    Gson gson = new Gson();
                    ASRresponse asRresponse = gson.fromJson(params, ASRresponse.class);
                    TextView view = wordSpeechViewList.get(viewPage.getCurrentItem()).findViewById(R.id.speech_text);
                    ProgressBar bar = wordSpeechViewList.get(viewPage.getCurrentItem()).findViewById(R.id.pg_word_record_bar);
                    String answer = "";
                    if (asRresponse.getBest_result().contains("，")) {//包含逗号  则将逗号替换为空格，这个地方还会问题，还可以进一步做出来，你知道吗？
                        answer = asRresponse.getBest_result().replace('，', ' ').trim();
                    } else {//不包含
                        answer = asRresponse.getBest_result().trim();
                    }
                    view.setText(answer);
                    Log.e("语音识别结果为", answer);
                    bar.setVisibility(View.INVISIBLE);
                }
            }
        }
    };

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String permissions[] = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(curActivity.getApplicationContext(), perm)) {
                toApplyList.add(perm);
                //进入到这里代表没有权限.

            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(curActivity, toApplyList.toArray(tmpList), 123);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
    }
}