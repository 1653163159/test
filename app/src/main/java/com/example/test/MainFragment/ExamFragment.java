package com.example.test.MainFragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.example.test.Adapter.ExamListViewAdapter;
import com.example.test.R;
import com.example.test.Exam.SubjectActivity;
import com.example.test.pojo.Hsk;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
 * Use the {@link ExamFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExamFragment extends Fragment {
    private View examFragment;
    private Spinner level;

    private final OkHttpClient client = new OkHttpClient();
    private String prefix = "";
    //试题列表
    public ListView examList;
    ExamListViewAdapter examListViewAdapter;
    public List<Hsk> examItems = new ArrayList<>();
    public int index = 0;
    ProgressBar pg;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ExamFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ExamFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ExamFragment newInstance(String param1, String param2) {
        ExamFragment fragment = new ExamFragment();
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
        if (examFragment == null) {
            examFragment = inflater.inflate(R.layout.fragment_exam, container, false);
        }
        initView();
        return examFragment;
    }

    void initView() {
        pg = examFragment.findViewById(R.id.pg_bar);
        examList = examFragment.findViewById(R.id.exam_list);
        examItems = new ArrayList<>();
        examList.setAdapter(examListViewAdapter = new ExamListViewAdapter(getContext(), examItems));
        examList.setOnItemClickListener(examListListener);

        level = examFragment.findViewById(R.id.exam_level);
        level.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                examItems.clear();
                examListViewAdapter.notifyDataSetChanged();
                getExamList(String.valueOf(position + 1));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        level.setSelection(5);
    }

    /**
     * 获取对应难度试题列表
     */
    void getExamList(String level) {
        pg.setVisibility(View.VISIBLE);
        pg.bringToFront();
        Request.Builder builder = null;
        builder = new Request.Builder().url(prefix + "exam/level/" + level);
        Call call = client.newCall(builder.build());
        call.enqueue(examListCallback);
    }

    private Callback examListCallback = new Callback() {
        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {

        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            String str = new String(response.body().bytes());
            Message message = updateExamList.obtainMessage();
            message.obj = str;
            message.sendToTarget();
        }
    };
    private Handler updateExamList = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            Gson gson = new Gson();
            JsonArray jsonArray = new JsonParser().parse(msg.obj.toString()).getAsJsonArray();
            for (JsonElement jsonElement : jsonArray) {
                Hsk hsk = gson.fromJson(jsonElement, Hsk.class);
                examItems.add(hsk);
            }
            if (examItems.size() == 0) {
                Hsk hsk = new Hsk();
                hsk.setIdhsk("当前资源没有搜集到哦");
                examItems.add(hsk);
            }
            examListViewAdapter.notifyDataSetChanged();
            pg.setVisibility(View.INVISIBLE);
        }
    };

    /**
     * 点击对应的item应当跳转到相应的页面并传递对应试题的path，加载试题
     */
    AdapterView.OnItemClickListener examListListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (examItems.get(position).getIdhsk().equals("当前资源没有搜集到哦")) return;
            pg.setVisibility(View.VISIBLE);
            pg.bringToFront();
            String subjectPath = getActivity().getExternalCacheDir().getAbsolutePath() + File.separator + examItems.get(position).getIdhsk() + "-question.json";
            String answerPath = getActivity().getExternalCacheDir().getAbsolutePath() + File.separator + examItems.get(position).getIdhsk() + "-answer.json";
            String audioPath = getActivity().getExternalCacheDir().getAbsolutePath() + File.separator + examItems.get(position).getIdhsk() + "-audio.mp3";
            //判断是否缓存过试题，如果之前加载过则读取本地文件，否则请求网络资源
            try {
                File file = new File(subjectPath);
                if (file.exists()) {
                    Navigation(subjectPath, answerPath, audioPath, position);
                    pg.setVisibility(View.INVISIBLE);
                } else {
                    Request.Builder builder = null;
                    builder = new Request.Builder().url(prefix + "exam/HSK/" + examItems.get(position).getIdhsk());
                    Call call = client.newCall(builder.build());
                    call.enqueue(examCallback);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            index = position;
        }
    };
    private Callback examCallback = new Callback() {
        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {

        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            Gson gson = new Gson();
            JsonElement jsonElement = new JsonParser().parse(new String(response.body().bytes())).getAsJsonObject();
            Hsk hsk = gson.fromJson(jsonElement, Hsk.class);
            String subjectPath = getActivity().getExternalCacheDir().getAbsolutePath() + File.separator + hsk.getIdhsk() + "-question.json";
            String answerPath = getActivity().getExternalCacheDir().getAbsolutePath() + File.separator + hsk.getIdhsk() + "-answer.json";
            String audioPath = getActivity().getExternalCacheDir().getAbsolutePath() + File.separator + hsk.getIdhsk() + "-audio.mp3";
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(subjectPath);
                fileOutputStream.write(Base64.decode(hsk.getContent(), Base64.DEFAULT));
                fileOutputStream.close();
                fileOutputStream = new FileOutputStream(answerPath);
                fileOutputStream.write(Base64.decode(hsk.getAnswer(), Base64.DEFAULT));
                fileOutputStream.close();
                fileOutputStream = new FileOutputStream(audioPath);
                fileOutputStream.write(Base64.decode(hsk.getAudio(), Base64.DEFAULT));
                fileOutputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            pg.setVisibility(View.INVISIBLE);
            Navigation(subjectPath, answerPath, audioPath, index);
        }
    };

    void Navigation(String s, String an, String au, int position) {
        Intent intent = new Intent(getActivity(), SubjectActivity.class);
        intent.putExtra("subject", s);
        intent.putExtra("answer", an);
        intent.putExtra("audio", au);
        intent.putExtra("name", examItems.get(position).getIdhsk());
        startActivity(intent);
    }

}