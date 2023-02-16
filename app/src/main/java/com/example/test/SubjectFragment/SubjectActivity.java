package com.example.test.SubjectFragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test.Adapter.SubjectListViewAdapter;
import com.example.test.MainActivity;
import com.example.test.R;
import com.example.test.pojo.Subject;
import com.example.test.pojo.SubjectAnswer;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.jivesoftware.smack.SmackException;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SubjectActivity extends AppCompatActivity {

    TextView title, submit;
    ImageView back;


    ListView subjectList;
    SubjectListViewAdapter subjectListViewAdapter;
    List<Subject> subjectItems = new ArrayList<>();
    List<SubjectAnswer> answerItems = new ArrayList<SubjectAnswer>();
    String subjectPath, answerPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);
        try {
            initView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void initView() throws Exception {
        subjectPath = getIntent().getStringExtra("subject");
        answerPath = getIntent().getStringExtra("answer");
        title = findViewById(R.id.sub_title);
        submit = findViewById(R.id.sub_submit);
        back = findViewById(R.id.sub_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(SubjectActivity.this);
                dialog.setTitle("返回").setMessage("是否退出答题").setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onBackPressed();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(SubjectActivity.this);
                dialog.setTitle("返回").setMessage("是否提交").setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CalculateGrade();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
            }
        });
        title.setText(getIntent().getStringExtra("name"));
        subjectList = findViewById(R.id.subject_list);
        subjectList.setAdapter(subjectListViewAdapter = new SubjectListViewAdapter(getApplicationContext(), subjectItems));
        LoadSubject();
    }

    /**
     * 加载题目
     *
     * @throws Exception
     */
    void LoadSubject() throws Exception {
        BufferedReader fileInputStream = new BufferedReader(new InputStreamReader(new FileInputStream(subjectPath), "GBK"));
        StringBuilder builder = new StringBuilder();
        int ch;
        while ((ch = fileInputStream.read()) != -1) {
            builder.append((char) ch);
        }
        String sub = builder.toString();
        fileInputStream.close();
        JsonArray array = new JsonParser().parse(sub).getAsJsonArray();
        for (JsonElement element : array) {
            Subject subject = new Gson().fromJson(element, Subject.class);
            subjectItems.add(subject);
        }
        subjectListViewAdapter.notifyDataSetChanged();
        fileInputStream = new BufferedReader(new InputStreamReader(new FileInputStream(answerPath), "GBK"));
        builder = new StringBuilder();
        while ((ch = fileInputStream.read()) != -1) {
            builder.append((char) ch);
        }
        String ans = builder.toString();
        fileInputStream.close();
        array = new JsonParser().parse(ans).getAsJsonArray();
        for (JsonElement element : array) {
            SubjectAnswer answer = new Gson().fromJson(element, SubjectAnswer.class);
            answerItems.add(answer);
        }
    }

    /**
     * 计算成绩
     */
    private void CalculateGrade() {
        SubjectAnswer[] answers = subjectListViewAdapter.getAnswersAlphabet();
        for (int i = 1; i < 5; i++) {
            if (answers[i] != null)
                System.out.println(answers[i].getAnswer());
        }
        Toast.makeText(this, "666", Toast.LENGTH_SHORT).show();
        showGrade();
    }

    private void showGrade() {
    }
}