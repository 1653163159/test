package com.example.test.Fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerTabStrip;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;

import com.example.test.Adapter.ExamListViewAdapter;
import com.example.test.Adapter.ExamViewpageAdapter;
import com.example.test.R;
import com.example.test.SubjectFragment.SubjectActivity;
import com.example.test.pojo.Hsk;
import com.example.test.pojo.Subject;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
    public List<String> subjectPathItems = new ArrayList<>();//存放对应试题的存储路径
    public List<String> answerPathItems = new ArrayList<>();//存放对应试题答案的存储路径
    public List<String> audioPathItems = new ArrayList<>();//存放对应试题答案的存储路径

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
        examList = examFragment.findViewById(R.id.exam_list);
        examItems = new ArrayList<>();
        examList.setAdapter(examListViewAdapter = new ExamListViewAdapter(getContext(), examItems));
        examList.setOnItemClickListener(examListListener);

        level = examFragment.findViewById(R.id.exam_level);
        level.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                examItems.clear();
                subjectPathItems.clear();
                answerPathItems.clear();
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
        Request.Builder builder = null;
        builder = new Request.Builder().url(prefix + "exam/" + level);
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
                subjectPathItems.add(subjectPath);
                answerPathItems.add(answerPath);
                audioPathItems.add(audioPath);
            }
            if (examItems.size() == 0) {
                Hsk hsk = new Hsk();
                hsk.setIdhsk("当前资源没有搜集到哦");
                examItems.add(hsk);
            }
            examListViewAdapter.notifyDataSetChanged();
        }
    };

    /**
     * 点击对应的item应当跳转到相应的页面并传递对应试题的path，加载试题
     */
    AdapterView.OnItemClickListener examListListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (examItems.get(position).getIdhsk().equals("当前资源没有搜集到哦")) return;
            Intent intent = new Intent(getActivity(), SubjectActivity.class);
            intent.putExtra("subject", subjectPathItems.get(position));
            intent.putExtra("answer", answerPathItems.get(position));
            intent.putExtra("audio", audioPathItems.get(position));
            intent.putExtra("name", examItems.get(position).getIdhsk());
            startActivity(intent);
        }
    };


}