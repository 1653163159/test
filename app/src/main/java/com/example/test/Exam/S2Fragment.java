package com.example.test.Exam;

import android.app.Activity;
import android.graphics.*;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.util.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.GeneralBasicParams;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.WordSimple;
import com.example.test.R;
import com.example.test.pojo.PictureBank;
import com.example.test.tools.LinePathView;
import com.google.gson.Gson;

import java.io.*;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link S2Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
/*
 * 书法题
 * */
public class S2Fragment extends Fragment {

    View curLayout;
    Activity curActivity;

    /*
     * 服务器请求设置
     * */
    private final OkHttpClient client = new OkHttpClient();
    String requestType;//向服务器请求数据的类型

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * 题目相关组件
     */
    private TextView title;
    private ImageView picResource;
    private LinePathView paintView;
    private Button reset, submit;
    private int questionId = 1;
    private String resourceGetPath, answerSavePath;//文件保存目录
    public String answerPictureToSting = "";

    //id绑定
    private void initBind() {
        title = curLayout.findViewById(R.id.writing_title);
        picResource = curLayout.findViewById(R.id.writing_resource);
        paintView = curLayout.findViewById(R.id.paint);
        reset = curLayout.findViewById(R.id.write_reset);
        submit = curLayout.findViewById(R.id.write_submit);
        reset.setOnClickListener(resetView);
        submit.setOnClickListener(submitView);
    }

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String prefix;
    private String level;

    public S2Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment S2Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static S2Fragment newInstance(String param1, String param2) {
        S2Fragment fragment = new S2Fragment();
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
        curLayout = inflater.inflate(R.layout.fragment_s2, container, false);
        curActivity = getActivity();
        initBind();

        getQuestionSource(questionId);

        return curLayout;
    }

    private void getQuestionSource(int questionId) {
        Request.Builder builder = null;
        builder = new Request.Builder().url(prefix + "HandwritingResourceSingle/" + questionId);
        execute(builder);
    }

    private void execute(Request.Builder builder) {
        Call call = client.newCall(builder.build());
        call.enqueue(resourceCallback);
    }

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
    private Handler updateSubResource = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.e("TAG", "handleMessage: " + msg);
            String res = (String) msg.obj;
            if (res == null || res.equals("")) return;
            Log.e("TAG2", "handleMessage: " + res);
            Gson gson = new Gson();
            try {
                getHandwritingResource(gson, res);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    /*
     * 向服务器请求图片，保存到本地，处理图片为bitmap并加载
     * */
    private void getHandwritingResource(Gson gson, String res) throws IOException {
        PictureBank p1 = gson.fromJson(res, PictureBank.class);
        byte[] bytes = Base64.decode(p1.getPicture_content(), Base64.DEFAULT);
        resourceGetPath = curActivity.getExternalCacheDir().getAbsolutePath() + File.separator + p1.getPicture_name() + ".jpg";
        FileOutputStream fileOutputStream = new FileOutputStream(resourceGetPath);
        fileOutputStream.write(bytes);
        fileOutputStream.close();
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length - 1);
        picResource.setImageBitmap(bitmap);

    }

    /**
     * 清空手写板
     */
    private View.OnClickListener resetView = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            paintView.clear();
            title.setText("请写出以下汉字");
        }
    };

    /**
     * 提交答案
     */
    private View.OnClickListener submitView = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            answerSavePath = curActivity.getExternalCacheDir().getAbsolutePath() + File.separator + "result.png";
            try {
                paintView.save(answerSavePath);
                paintView.clear();
                /*Bitmap bitmap = BitmapFactory.decodeFile(answerSavePath);//读取本地文件转为bitmap并更新UI
                picResource.setImageBitmap(bitmap);*/
            } catch (IOException e) {
                e.printStackTrace();
            }
            String ak = "xdMF0dc8gUpM4Epenwvn0rZS";
            String sk = "SoVzlkXOIGaLWIPBvMvQiHUF370sQBqG";
            String t = "24.0829db384e2a8427ac31ee52174cd4cd.2592000.1680763335.282335-29786756";
            //初始化ORC访问许可
            OCR.getInstance(curActivity.getApplicationContext()).initAccessToken(new OnResultListener<AccessToken>() {
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
            }, "aip-ocr.license", curActivity.getApplicationContext());
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
                    ocrError.printStackTrace();
                }
            });
        }
    };

    /**
     * 通过Handler来实时获取数据,对比答案，返回结果
     */
    Handler updateUI = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            answerPictureToSting = msg.obj.toString();
            if (!answerPictureToSting.equals("")) {
                title.setText(answerPictureToSting);
            }
            Toast.makeText(curActivity.getApplicationContext(), answerPictureToSting, Toast.LENGTH_LONG).show();
        }
    };

    public void onDestroy() {
        super.onDestroy();
        paintView.clear();
        title.setText("请写出以下汉字");
    }
}