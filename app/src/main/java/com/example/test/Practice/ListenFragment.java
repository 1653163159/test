package com.example.test.Practice;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test.Adapter.ChapterListAdapter;
import com.example.test.R;
import com.example.test.pojo.Stack;
import com.example.test.tools.TextToSpeechUtil;
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
 * Use the {@link ListenFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListenFragment extends Fragment {
    View curLayout;
    TextToSpeechUtil util;

    TextView textView, title;
    ProgressBar bar;
    ListView listView;
    ImageView imageView, showTxt, tv_play;
    ChapterListAdapter adapter;
    List<Stack> stackList;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private String prefix;
    private String bookName;
    private String path;
    private OkHttpClient client;
    private boolean isPlay = false, isPause = false;//播放暂停标志
    private MediaPlayer mediaPlayer;//媒体播放器

    public ListenFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ListenFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ListenFragment newInstance(String param1, String param2) {
        ListenFragment fragment = new ListenFragment();
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
            bookName = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        curLayout = inflater.inflate(R.layout.fragment_listen, container, false);
        client = new OkHttpClient();
        bar = curLayout.findViewById(R.id.pg_bar);
        showTxt = curLayout.findViewById(R.id.text_show);
        tv_play = curLayout.findViewById(R.id.tv_play);
        util = new TextToSpeechUtil(getContext());
        title = curLayout.findViewById(R.id.chapter_name);
        textView = curLayout.findViewById(R.id.speech_material);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
        textView.setScrollbarFadingEnabled(true);//设置scrollbar一直显示
        listView = curLayout.findViewById(R.id.speech_chapter_list);
        imageView = curLayout.findViewById(R.id.speech_chapter_switch);
        stackList = new ArrayList<>();
        listView.setAdapter(adapter = new ChapterListAdapter(getContext(), stackList));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                title.setText(stackList.get(position).getChaptername());
                textView.setText(stackList.get(position).getContent());
                textView.scrollTo(0, 0);
                hiddenAnimation(listView);
                curLayout.findViewById(R.id.tip).setVisibility(View.VISIBLE);
                hiddenUI.sendEmptyMessageDelayed(1, 5000);
                path = getActivity().getExternalCacheDir().getAbsolutePath() + File.separator + stackList.get(position).getChaptername() + ".mp3";
                util.saveToFile(path, stackList.get(position).getContent());
                System.out.println("path:" + path);
                releaseMusic();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playMusic();
                        bar.setVisibility(View.INVISIBLE);
                    }
                }, 2000);
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listView.bringToFront();
                imageView.bringToFront();
                if (listView.getVisibility() == View.VISIBLE) {
                    hiddenAnimation(listView);

                } else {
                    startAnimation(listView);
                }
            }
        });
        showTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textView.getVisibility() == View.INVISIBLE) {
                    textView.setVisibility(View.VISIBLE);
                    textView.bringToFront();
                } else {
                    textView.setVisibility(View.INVISIBLE);
                }
            }
        });
        tv_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer == null) {
                    bar.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "音频加载中", Toast.LENGTH_SHORT).show();
                    return;
                }
                switch (v.getId()) {
                    case R.id.tv_play:
                        if (isPlay) {
                            if (mediaPlayer != null) {
                                if (isPause) {
                                    mediaPlayer.start();
                                    isPause = false;
                                    tv_play.setImageDrawable(getActivity().getDrawable(R.drawable.ic_baseline_stop));
                                } else {
                                    mediaPlayer.pause();
                                    isPause = true;
                                    tv_play.setImageDrawable(getActivity().getDrawable(R.drawable.ic_baseline_play));
                                }
                            }
                        } else {
                            tv_play.setImageDrawable(getActivity().getDrawable(R.drawable.ic_baseline_stop));
                            isPlay = true;
                        }
                        break;
                }
            }
        });
        getChapters();
        bar.setVisibility(View.VISIBLE);
        hiddenUI.sendEmptyMessageDelayed(1, 5000);
        return curLayout;
    }

    private void getChapters() {
        Request.Builder builder = null;
        String url = prefix + Flags.STACK_BOOK_URL + bookName.replace("?", "%3F");
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
            Message message = updateUI.obtainMessage();
            message.obj = str;
            message.sendToTarget();
        }
    };
    Handler updateUI = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            String res = (String) msg.obj;
            JsonArray jsonArray = new JsonParser().parse(res).getAsJsonArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                Stack stack = new Gson().fromJson(jsonArray.get(i), Stack.class);
                stackList.add(stack);
            }
            if (stackList.size() == 0) {
                ((PracticeActivity) getActivity()).back();
                Toast.makeText(getContext(), "资源丢失", Toast.LENGTH_SHORT).show();
                return;
            }
            adapter.notifyDataSetChanged();
            textView.setText(stackList.get(0).getContent());
            title.setText(stackList.get(0).getChaptername());
            bar.setVisibility(View.INVISIBLE);
            path = getActivity().getExternalCacheDir().getAbsolutePath() + File.separator + stackList.get(0).getChaptername() + ".mp3";
            util.saveToFile(path, stackList.get(0).getContent());
            System.out.println("path:" + path);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    playMusic();
                    bar.setVisibility(View.INVISIBLE);
                }
            }, 2000);
        }
    };

    private void startAnimation(ListView view) {
        Handler handler = new Handler();
        view.setVisibility(View.GONE);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.VISIBLE);
                ValueAnimator anim = ObjectAnimator.ofFloat(view, "translationX", 500, 0);
                anim.setDuration(200);
                anim.start();
            }
        }, 0);
    }

    private void hiddenAnimation(ListView view) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.VISIBLE);
                ValueAnimator anim = ObjectAnimator.ofFloat(view, "translationX", 0, 500);
                anim.setDuration(200);
                anim.start();
            }
        }, 0);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.INVISIBLE);
            }
        }, 200);
    }

    Handler hiddenUI = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            TextView textView = curLayout.findViewById(R.id.tip);
            textView.setVisibility(View.INVISIBLE);
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
                    tv_play.setImageDrawable(getActivity().getDrawable(R.drawable.ic_baseline_play));
                    mediaPlayer.release();
                    isPlay = false;
                    isPause = true;
                }
            });
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.pause();
            isPlay = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void releaseMusic() {
        if (mediaPlayer != null) {
            tv_play.setImageDrawable(getActivity().getDrawable(R.drawable.ic_baseline_play));
            mediaPlayer.release();
            isPlay = false;
            isPause = true;
            mediaPlayer = null;
        }
    }
}