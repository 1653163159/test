package com.example.test.Practice;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.GeneralBasicParams;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.WordSimple;
import com.example.test.Adapter.QuizViewPageAdapter;
import com.example.test.R;
import com.example.test.pojo.Composition;
import com.example.test.pojo.Word;
import com.example.test.tools.LinePathView;
import com.example.test.tools.MyViewPage;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WordsWriteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WordsWriteFragment extends Fragment {
    OkHttpClient client = new OkHttpClient();
    int position = 0;
    View wordsWriteFragment;
    Activity curActivity;

    ProgressBar refreshLayout;
    MyViewPage viewPage;
    PagerAdapter pagerAdapter;
    List<View> wordsWriteViewList;
    List<Word> wordsWriteList;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String prefix;
    private String level;

    public WordsWriteFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WordsWriteFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WordsWriteFragment newInstance(String param1, String param2) {
        WordsWriteFragment fragment = new WordsWriteFragment();
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
        if (wordsWriteFragment == null)
            wordsWriteFragment = inflater.inflate(R.layout.fragment_words_write, container, false);
        curActivity = getActivity();
        initView();
        getWordList(level, position);
        refreshLayout.setVisibility(View.VISIBLE);
        return wordsWriteFragment;
    }

    private void initView() {
        refreshLayout = wordsWriteFragment.findViewById(R.id.pg_word_bar);
        viewPage = wordsWriteFragment.findViewById(R.id.words_write_list);
        wordsWriteViewList = new ArrayList<>();
        wordsWriteList = new ArrayList<>();
        viewPage.setAdapter(pagerAdapter = new QuizViewPageAdapter(wordsWriteViewList));
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
                wordsWriteList.add(word);
                addView(word, i);
            }
            pagerAdapter.notifyDataSetChanged();
            setSub_position((position % 10 + 1) + "/" + wordsWriteList.size());
            refreshLayout.setVisibility(View.INVISIBLE);
        }
    };

    private void addView(Word word, int i) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_s2, null);
        Typeface typeface = Typeface.createFromAsset(curActivity.getAssets(), "fonts/标准楷体简.ttf");
        TextView content = view.findViewById(R.id.word_content);
        content.setTypeface(typeface);
        TextView answer = view.findViewById(R.id.word_answer);
        content.setText(word.getContent());
        LinePathView paintView = view.findViewById(R.id.paint);
        Button reset = view.findViewById(R.id.write_reset), submit = view.findViewById(R.id.write_submit);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintView.clear();
            }
        });
        Handler updateUI = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                answer.setText(msg.obj.toString());
            }
        };
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String answerSavePath = curActivity.getExternalCacheDir().getAbsolutePath() + File.separator + "result.png";
                try {
                    paintView.save(answerSavePath);
                    paintView.clear();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String ak = "xdMF0dc8gUpM4Epenwvn0rZS";
                String sk = "SoVzlkXOIGaLWIPBvMvQiHUF370sQBqG";
                //初始化ORC访问许可
                OCR.getInstance(curActivity.getApplicationContext()).initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
                    @Override
                    public void onResult(AccessToken result) {
                        // 调用成功，返回AccessToken对象
                        String token = result.getAccessToken();
                        System.out.println("token:" + token);
                    }

                    @Override
                    public void onError(OCRError error) {
                        // 调用失败，返回OCRError子类SDKError对象
                        error.printStackTrace();
                    }
                }, curActivity.getApplicationContext(), ak, sk);
                System.out.println(ak + "\n" + sk);
                GeneralBasicParams param = new GeneralBasicParams();
                param.setDetectDirection(true);
                param.setImageFile(new File(answerSavePath));
                StringBuilder sb = new StringBuilder();
                // 调用通用文字识别服务
                OCR.getInstance(curActivity.getApplicationContext()).recognizeAccurateBasic(param, new OnResultListener<GeneralResult>() {
                    @Override
                    public void onResult(GeneralResult result) {
                        // 调用成功，返回GeneralResult对象
                        for (WordSimple wordSimple : result.getWordList()) {
                            // wordSimple不包含位置信息
                            WordSimple word = wordSimple;
                            sb.append(word.getWords());
                        }
                        Message message = updateUI.obtainMessage();
                        message.obj = sb.toString();
                        updateUI.sendMessage(message);
                        Log.e("Json", "onResult: " + result.getJsonRes());
                    }

                    @Override
                    public void onError(OCRError ocrError) {
                        Toast.makeText(getContext(), "抱歉，api暂不可用", Toast.LENGTH_SHORT).show();
                        ocrError.printStackTrace();
                    }
                });
            }
        });
        wordsWriteViewList.add(view);
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


    ViewPager.OnPageChangeListener changeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            setSub_position((position % 10 + 1) + "/" + wordsWriteList.size());
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == 1) {
                if (viewPage.getCurrentItem() == wordsWriteList.size() - 1) {
                    refreshLayout.setVisibility(View.VISIBLE);
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
        wordsWriteViewList.clear();
        wordsWriteList.clear();
        pagerAdapter.notifyDataSetChanged();
    }
}