package com.example.test.Practice;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.os.Message;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.test.Adapter.QuizViewPageAdapter;
import com.example.test.R;
import com.example.test.pojo.Quiz;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link QuizFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class QuizFragment extends Fragment {

    OkHttpClient client = new OkHttpClient();

    int position = 0;

    View quizFragment;
    Activity curActivity;

    SwipeRefreshLayout refreshLayout;
    ViewPager quizViewPage;
    QuizViewPageAdapter pageAdapter;
    List<View> quizViewList;
    List<Quiz> quizList;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String prefix;
    private String level;

    public QuizFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment QuizFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static QuizFragment newInstance(String param1, String param2) {
        QuizFragment fragment = new QuizFragment();
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
        if (quizFragment == null) {
            quizFragment = inflater.inflate(R.layout.fragment_quiz, container, false);
        }
        curActivity = getActivity();

        initView();
        getQuizList(level, position);
        refreshLayout.setRefreshing(true);
        return quizFragment;
    }

    void initView() {
        refreshLayout = quizFragment.findViewById(R.id.quiz_refresh);
        refreshLayout.setOnRefreshListener(refreshListener);
        refreshLayout.setColorSchemeResources(R.color.colorYang, R.color.aliceBlue);
        quizViewPage = quizFragment.findViewById(R.id.quiz_list);
        quizViewList = new ArrayList<>();
        quizList = new ArrayList<>();
        quizViewPage.setAdapter(pageAdapter = new QuizViewPageAdapter(quizViewList));
        quizViewPage.setOnPageChangeListener(changeListener);
        quizViewPage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    System.out.println(event.getRawX());
                    WindowManager wm = (WindowManager) curActivity.getSystemService(getContext().WINDOW_SERVICE);
                    int width = wm.getDefaultDisplay().getWidth();
                    int temp = width / 3;
                    if (event.getRawX() > width / 2 + temp) {
                        quizViewPage.setCurrentItem(quizViewPage.getCurrentItem() + 1);
                    }
                    if (event.getRawX() < width / 2 - temp) {
                        quizViewPage.setCurrentItem(quizViewPage.getCurrentItem() - 1);
                    }
                }
                return false;
            }
        });
    }

    private void getQuizList(String level, int position) {
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
            setSub_position((position + 1) + "/" + quizViewList.size());
            refreshLayout.setRefreshing(false);
            quizViewPage.setCurrentItem(position);
        }
    };

    /**
     * 动态设置每个页面
     *
     * @param quiz
     * @param i
     */
    private void addView(Quiz quiz, int i) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.quiz_list, null);
        TextView textView = view.findViewById(R.id.quiz_title);
        textView.setText((position / 10 * 10 + i + 1) + "." + quiz.getContent());
        RadioGroup group = view.findViewById(R.id.quiz_group);
        if (!quiz.getOptiona().equals("")) {
            RadioButton button = new RadioButton(getContext());
            button.setLayoutParams(new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT));
            button.setText(quiz.getOptiona());
            button.setTextSize(12);
            button.setPadding(5, 5, 5, 5);
            button.setButtonDrawable(R.drawable.check_circle_default);
            group.addView(button);
        }
        if (!quiz.getOptionb().equals("")) {
            RadioButton button = new RadioButton(getContext());
            button.setLayoutParams(new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT));
            button.setText(quiz.getOptionb());
            button.setTextSize(12);
            button.setPadding(5, 5, 5, 5);
            button.setButtonDrawable(R.drawable.check_circle_default);
            group.addView(button);
        }
        if (!quiz.getOptionc().equals("")) {
            RadioButton button = new RadioButton(getContext());
            button.setLayoutParams(new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT));
            button.setText(quiz.getOptionc());
            button.setTextSize(12);
            button.setPadding(5, 5, 5, 5);
            button.setButtonDrawable(R.drawable.check_circle_default);
            group.addView(button);
        }
        if (!quiz.getOptiond().equals("")) {
            RadioButton button = new RadioButton(getContext());
            button.setText(quiz.getOptiond());
            button.setLayoutParams(new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT));
            button.setTextSize(12);
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
                } else {
                    button.setButtonDrawable(R.drawable.check_circle__wrong);
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
     * 更新题目总数和位置
     *
     * @param str
     */
    void setSub_position(String str) {
        PracticeActivity activity = (PracticeActivity) getActivity();
        activity.setSub_position(str);
    }

    /**
     * 刷新页面，获取新的数据
     */
    SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            refresh();
        }
    };

    /**
     * 翻页后更新一些显示文本
     */
    ViewPager.OnPageChangeListener changeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            setSub_position((position + 1) + "/" + quizViewList.size());
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == 1) {
                if (quizViewPage.getCurrentItem() == quizList.size() - 1) {
                    refresh();
                    refreshLayout.setRefreshing(true);
                }
            }
        }
    };

    private void refresh() {
        int count = getMaxCount(level);
        if (position < count - 10) position += 10;
        getQuizList(level, position);
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
                count = Flags.QUIZ_BEGINNER_COUNT;
                break;
            case Flags.INTERMEDIATE:
                count = Flags.QUIZ_INTERMEDIATE_COUNT;
                break;
            case Flags.ADVANCED:
                count = Flags.QUIZ_ADVANCED_COUNT;
                break;
        }
        return count;
    }

    /**
     * 重置Fragment参数
     */
    public void reset() {
        position = 0;
    }
}