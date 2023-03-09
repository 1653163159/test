package com.example.test.Practice;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.test.Adapter.QuizViewPageAdapter;
import com.example.test.R;
import com.example.test.pojo.Composition;
import com.example.test.pojo.Quiz;
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
 * Use the {@link CompositionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CompositionFragment extends Fragment {
    OkHttpClient client = new OkHttpClient();
    int position = 0;
    View compositionFragment;
    Activity curActivity;

    ProgressBar refreshLayout;
    ViewPager viewPage;
    PagerAdapter pagerAdapter;
    List<View> compositionViewList;
    List<Composition> compositionList;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String prefix;
    private String level;

    public CompositionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CompositionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CompositionFragment newInstance(String param1, String param2) {
        CompositionFragment fragment = new CompositionFragment();
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
        if (compositionFragment == null) {
            compositionFragment = inflater.inflate(R.layout.fragment_composition, container, false);

        }
        curActivity = getActivity();

        initView();
        getCompositionList(level, position);
        refreshLayout.setVisibility(View.VISIBLE);
        refreshLayout.bringToFront();
        return compositionFragment;
    }

    void initView() {
        refreshLayout = compositionFragment.findViewById(R.id.composition_refresh);
        viewPage = compositionFragment.findViewById(R.id.composition_list);
        compositionViewList = new ArrayList<>();
        compositionList = new ArrayList<>();
        viewPage.setAdapter(pagerAdapter = new QuizViewPageAdapter(compositionViewList));
        viewPage.setOnPageChangeListener(changeListener);
        viewPage.setOnTouchListener(new View.OnTouchListener() {
            private float endX;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    System.out.println(event.getRawX());
                    endX = event.getX();
                    WindowManager wm = (WindowManager) curActivity.getSystemService(getContext().WINDOW_SERVICE);
                    int width = wm.getDefaultDisplay().getWidth();
                    int temp = width / 3;
                    if (event.getRawX() > width / 2 + temp) {
                        viewPage.setCurrentItem(viewPage.getCurrentItem() + 1);
                    }
                    if (event.getRawX() < width / 2 - temp) {
                        viewPage.setCurrentItem(viewPage.getCurrentItem() - 1);
                    }
                    if (viewPage.getCurrentItem() == compositionList.size() - 1 && startX - endX >= (width / 4)) {
                        refreshLayout.setVisibility(View.VISIBLE);
                        refreshLayout.bringToFront();
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
                return false;
            }
        });
    }

    public float startX;

    private void getCompositionList(String level, int position) {
        Request.Builder builder = null;
        String url = prefix + Flags.COMPOSITION_URL + level + File.separator + position;
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
            Message message = updateCompositionList.obtainMessage();
            message.obj = str;
            message.sendToTarget();
        }
    };
    Handler updateCompositionList = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            clear();
            String res = (String) msg.obj;
            JsonArray jsonArray = new JsonParser().parse(res).getAsJsonArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                Composition composition = new Gson().fromJson(jsonArray.get(i), Composition.class);
                compositionList.add(composition);
                addView(composition, i);
            }
            System.out.println(compositionViewList.size() + ";" + compositionList.size());
            pagerAdapter.notifyDataSetChanged();
            setSub_position((position % 5 + 1) + "/" + compositionList.size());
            refreshLayout.setVisibility(View.INVISIBLE);
        }
    };

    private void addView(Composition composition, int i) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.composition_list, null);
        TextView textView = view.findViewById(R.id.composition_title);
        textView.setText((i + 1) + "." + composition.getContent());
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
        textView.setScrollbarFadingEnabled(false);//设置scrollbar一直显示
        compositionViewList.add(view);
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
            setSub_position((position % 5 + 1) + "/" + compositionViewList.size());
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    Handler refreshHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            refresh();
        }
    };

    private void refresh() {
        int count = Flags.COMPOSITION_COUNT;
        position += 5;
        if (position > count) {
            Random random = new Random();
            position = random.nextInt(count);
        }
        getCompositionList(level, position);
        viewPage.setCurrentItem(0);
    }

    private void clear() {
        compositionViewList.clear();
        compositionList.clear();
        pagerAdapter.notifyDataSetChanged();
        System.out.println("clean:" + compositionViewList.size() + ";" + compositionList.size());
    }
}