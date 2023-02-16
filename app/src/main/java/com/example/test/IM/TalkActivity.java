package com.example.test.IM;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;


import com.example.test.Adapter.MsgListViewAdapter;
import com.example.test.R;
import com.example.test.pojo.UserMsg;
import com.example.test.tools.JsonUtil;
import com.example.test.tools.SmarkUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.jivesoftware.smack.packet.Presence;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TalkActivity extends AppCompatActivity {

    //用户名；好友名；好友jid
    String USER_NAME = "hqx", FRIEND_NAME = "", FRIEND_JID = "";
    SmarkUtil smarkUtil;


    ListView msgList;
    MsgListViewAdapter msgListViewAdapter;
    List<String> items = new ArrayList<>();
    SwipeRefreshLayout refresh;
    JsonUtil jsonUtil = new JsonUtil();
    String path = "";


    //区分不同用户的消息
    int myFlag = 0, hisFlag = 1;

    Toolbar toolbar;
    EditText msgContent;
    Button msgSend;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk);

        try {
            initView();
            LoadMsg();
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 定义控件
     *
     * @throws Exception
     */
    private void initView() throws Exception {
        USER_NAME = getIntent().getStringExtra("myname");
        FRIEND_NAME = getIntent().getStringExtra("name");
        FRIEND_JID = getIntent().getStringExtra("jid");
        path = getExternalCacheDir().getAbsolutePath() + File.separator + FRIEND_JID + ".json";

        toolbar = findViewById(R.id.backToTalk);
        msgContent = findViewById(R.id.msg_edit);
        msgSend = findViewById(R.id.msg_send);
        msgSend.setOnClickListener(sendListener);
        toolbar.setTitle(FRIEND_NAME);
        msgList = findViewById(R.id.msg_list);
        refresh = findViewById(R.id.msg_list_refresh);
        refresh.setOnRefreshListener(refreshListener);


        smarkUtil = new SmarkUtil(getIntent().getStringExtra("myname"), getIntent().getStringExtra("mypwd"), getIntent().getStringExtra("domain"), getIntent().getStringExtra("host"));
        smarkUtil.setState(new Presence(Presence.Type.available));
    }


    /**
     * 加载历史消息
     *
     * @throws IOException
     */
    private void LoadMsg() throws IOException {
        items.clear();
        String content = jsonUtil.read(path);
        if (content == null) return;
        Gson gson = new Gson();
        JsonArray jsonArray = new JsonParser().parse(content).getAsJsonArray();
        //List<UserMsg> userMsgs = new ArrayList<>();
        for (JsonElement jsonElement : jsonArray) {
            UserMsg user = gson.fromJson(jsonElement, UserMsg.class);
            //userMsgs.add(user);
            items.add(user.getFlag() + user.getUser() + ":\n      " + user.getMsg());
        }
        msgListViewAdapter.notifyDataSetChanged();
    }

    /**
     * 发送消息，保存
     */
    View.OnClickListener sendListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String str = String.valueOf(msgContent.getText());
            msgContent.setText("");
            if (str == null || str.equals("")) return;
            try {
                smarkUtil.sendMessage(FRIEND_JID, str);
                jsonUtil.write(path, USER_NAME + "@192.168.0.107", USER_NAME, str, String.valueOf(myFlag));
            } catch (Exception e) {
                e.printStackTrace();
            }
            items.add(myFlag + USER_NAME + ":\n      " + str);
            refreshHandler.sendEmptyMessage(1);
        }
    };

    SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            refreshHandler.sendEmptyMessageDelayed(1, 5);
        }
    };

    Handler refreshHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 1:
                    try {
                        LoadMsg();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    refresh.setRefreshing(false);
                    break;
            }
        }
    };


}

