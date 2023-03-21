package com.example.test.Practice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test.Adapter.BookListViewAdapter;
import com.example.test.Adapter.QuizViewPageAdapter;
import com.example.test.R;
import com.example.test.pojo.Quiz;
import com.example.test.pojo.Stack;
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

public class BookListActivity extends AppCompatActivity {
    OkHttpClient client = new OkHttpClient();

    int position = 0;
    String title, type, prefix, level;
    TextView bookListPosition;

    ProgressBar refreshLayout;
    ViewPager bookListViewPage;
    QuizViewPageAdapter pageAdapter;
    List<View> bookViewList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.cornFlowerBlue));
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);
        initView();
        getStackList(level, position);
        refreshLayout.setVisibility(View.VISIBLE);
        refreshLayout.bringToFront();
    }

    private void initView() {
        title = getIntent().getStringExtra("title");
        type = getIntent().getStringExtra("type");
        prefix = getIntent().getStringExtra("prefix");
        level = getIntent().getStringExtra("level");
        bookListPosition = findViewById(R.id.bookList_position);
        refreshLayout = findViewById(R.id.bookList_refresh);
        bookListViewPage = findViewById(R.id.bookList_view);
        bookViewList = new ArrayList<>();
        bookListViewPage.setAdapter(pageAdapter = new QuizViewPageAdapter(bookViewList));
        bookListViewPage.setOnPageChangeListener(changeListener);
        bookListViewPage.setOnTouchListener(new View.OnTouchListener() {
            private float endX;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    endX = event.getX();
                    System.out.println("endX:" + endX);
                    WindowManager wm = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        wm = (WindowManager) getSystemService(getApplicationContext().WINDOW_SERVICE);
                    }
                    int width = wm.getDefaultDisplay().getWidth();
                    int temp = width / 3;
                    if (event.getRawX() > width / 2 + temp) {
                        bookListViewPage.setCurrentItem(bookListViewPage.getCurrentItem() + 1);
                    }
                    if (event.getRawX() < width / 2 - temp) {
                        bookListViewPage.setCurrentItem(bookListViewPage.getCurrentItem() - 1);
                    }
                    if (bookListViewPage.getCurrentItem() == bookViewList.size() - 1 && startX - endX >= (width / 4)) {
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

    private float startX;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            startX = ev.getX();
            System.out.println("startX:" + startX);
        }
        return super.dispatchTouchEvent(ev);
    }

    void getStackList(String level, int position) {
        Request.Builder builder = null;
        String url = prefix + Flags.STACK_URL + level + File.separator + position;
        System.out.println(level + ":" + url);
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
            Message message = updateBookList.obtainMessage();
            message.obj = str;
            message.sendToTarget();
        }
    };
    Handler updateBookList = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            String res = (String) msg.obj;
            JsonArray jsonArray = new JsonParser().parse(res).getAsJsonArray();
            List<Stack> bookList = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                Stack stack = new Gson().fromJson(jsonArray.get(i), Stack.class);
                bookList.add(stack);
                System.out.println("\\" + stack.getBookname());
            }
            addView(bookList);
            pageAdapter.notifyDataSetChanged();
            refreshLayout.setVisibility(View.INVISIBLE);
            bookListViewPage.setCurrentItem(bookListViewPage.getCurrentItem() + 1);
            setList_position(String.valueOf(bookListViewPage.getCurrentItem() + 1), String.valueOf(bookViewList.size()));
        }
    };

    private void addView(List<Stack> list) {
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.book_list, null);
        GridView gridView = view.findViewById(R.id.book_list);
        BookListViewAdapter adapter;
        gridView.setAdapter(adapter = new BookListViewAdapter(this, list));
        adapter.setParams(title, type, prefix, level, refreshLayout);
        bookViewList.add(view);
    }

    public void setList_position(String cur, String max) {
        bookListPosition.setText(cur + "/" + max);
    }


    /**
     * 翻页后更新一些显示文本
     */
    ViewPager.OnPageChangeListener changeListener = new ViewPager.OnPageChangeListener() {
        private int currentPosition = 0;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            setList_position(String.valueOf((position + 1)), String.valueOf(bookViewList.size()));
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
        int count = getMaxCount(level);
        position += 10;
        if (position > count) {
            Random random = new Random();
            position = random.nextInt(count);
        }
        getStackList(level, position);
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
                count = Flags.STACK_BEGINNER_COUNT;
                break;
            case Flags.INTERMEDIATE:
                count = Flags.STACK_INTERMEDIATE_COUNT;
                break;
            case Flags.ADVANCED:
                count = Flags.STACK_ADVANCED_COUNT;
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

    public void back1(View view) {
        onBackPressed();
    }
}