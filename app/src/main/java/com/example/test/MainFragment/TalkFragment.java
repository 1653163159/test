package com.example.test.MainFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.test.Adapter.MsgListViewAdapter;
import com.example.test.Adapter.UserListViewAdapter;
import com.example.test.MainActivity;
import com.example.test.Practice.Flags;
import com.example.test.R;
import com.example.test.pojo.UserMsg;
import com.example.test.tools.JsonUtil;
import com.example.test.tools.SmarkUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TalkFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TalkFragment extends Fragment {

    View curLayout;
    Activity curActivity;
    JsonUtil jsonUtil = new JsonUtil();
    String path = "";
    //openfireConfig
    public String C_HOST = "";//服务器在局域网中IP
    public String C_DOMAIN = "";//服务器主机名
    public String USER_NAME = "", USER_PWD = "";
    public String FriendJid = "";
    SmarkUtil connect;

    //用户列表
    public ListView userList;
    UserListViewAdapter userListViewAdapter;
    public List<RosterEntry> userItems = new ArrayList<>();
    SwipeRefreshLayout userRefresh;
    //消息列表
    public ListView msgList;
    public MsgListViewAdapter msgListViewAdapter;
    public List<String> msgItems = new ArrayList<>();
    SwipeRefreshLayout msgRefresh;
    public static boolean isTalking = false;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TalkFragment() {
        // Required empty public constructor
    }

    public void setOpenfireConfig(SmarkUtil smarkUtil, String domain, String host, String name, String pwd) {
        this.C_DOMAIN = domain;
        this.C_HOST = host;
        this.USER_NAME = name;
        this.USER_PWD = pwd;
        this.connect = smarkUtil;
        System.out.println("已获得登录数据");
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TalkFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TalkFragment newInstance(String param1, String param2) {
        TalkFragment fragment = new TalkFragment();
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
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        curLayout = inflater.inflate(R.layout.fragment_talk, container, false);
        //linkToOpenfire();
        System.out.println("显示talk");
        initView();
        return curLayout;
    }


    private void initView() {
        curActivity = getActivity();
        userList = curLayout.findViewById(R.id.user_list);
        userList.setAdapter(userListViewAdapter = new UserListViewAdapter(curActivity.getApplicationContext(), userItems));
        userList.setOnItemClickListener(listListener);
        userRefresh = curLayout.findViewById(R.id.list_refresh);
        userRefresh.setColorSchemeResources(R.color.colorYang, R.color.aliceBlue);
        userRefresh.setOnRefreshListener(refreshListener);
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (connect != null) {
                            if (!connect.isConnected()) {
                                connect.reConnect();
                            }
                            if (!connect.isLogin()) {
                                connect.reLogin();
                                refreshHandler.sendEmptyMessage(1);
                            }
                            connect.setState(new Presence(Presence.Type.available));
                            refreshHandler.sendEmptyMessage(1);
                        }
                        sleep(5000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    /**
     * 按钮点击事件
     */
    AdapterView.OnItemClickListener listListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            TextView textView = view.findViewById(R.id.item1);
            Drawable drawable2 = getContext().getDrawable(R.drawable.arrow_right);
            drawable2.setBounds(0, 0, 100, 100);
            textView.setCompoundDrawables(drawable2, null, null, null);
            RosterEntry temp = userItems.get(position);
            FriendJid = temp.getUser();
            try {
                showTalkWindow(temp.getName(), FriendJid);
                isTalking = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            /*Intent intent = new Intent(curActivity, TalkActivity.class);
            intent.putExtra("myname", USER_NAME);
            intent.putExtra("mypwd", USER_PWD);
            intent.putExtra("host", C_HOST);
            intent.putExtra("domain", C_DOMAIN);
            intent.putExtra("name", temp.getName());
            intent.putExtra("jid", temp.getJid().toString());
            startActivity(intent);*/
        }
    };

    SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            refreshHandler.sendEmptyMessage(1);
        }
    };
    Handler refreshHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 1:
                    refreshItems(connect);
                    break;
            }
        }
    };

    /**
     * 刷新用户列表
     *
     * @param smarkUtil
     */
    public void refreshItems(SmarkUtil smarkUtil) {
        userItems.clear();
        if (smarkUtil.isLogin()) {
            try {
                smarkUtil.getAllEntries(userItems);
            } catch (Exception e) {
            }
        }
        userListViewAdapter.notifyDataSetChanged();
        userRefresh.setRefreshing(false);
        try {
            String content = jsonUtil.read(curActivity.getExternalCacheDir().getAbsolutePath() + File.separator + "userState.json");
            if (content != null) {
                JsonArray jsonArray = new JsonParser().parse(content).getAsJsonArray();
                for (JsonElement element : jsonArray) {
                    String oJid = element.getAsJsonObject().get("jid").getAsString();
                    String flag = element.getAsJsonObject().get("flag").getAsString();
                    if (flag.equals(Flags.notRead)) {
                        updateUserList(oJid);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateUserList(String msgFrom) {
        int index = findItemByJid(msgFrom);
        if (userList.getChildAt(index) == null) return;
        TextView textView = userList.getChildAt(index).findViewById(R.id.item1);
        if (textView == null) return;
        Drawable drawable = curActivity.getDrawable(R.drawable.msg_tip);
        drawable.setBounds(0, 0, 30, 30);
        Drawable drawable2 = curActivity.getDrawable(R.drawable.arrow_right);
        drawable2.setBounds(0, 0, 100, 100);
        textView.setCompoundDrawables(drawable2, null, drawable, null);
    }

    /**
     * 通过Jid获取用户列表对应用户的位置
     *
     * @param jid
     * @return
     */
    public int findItemByJid(String jid) {
        int index = -1;
        int i;
        for (i = 0; i < userItems.size(); i++) {
            if (userItems.get(i).getJid().toString().equals(jid)) {
                index = i;
            }
        }
        return index;
    }

    /**
     * 显示会话窗口
     *
     * @param friendName
     * @param friendJid
     * @throws IOException
     */
    public PopupWindow talkWindow;

    public void showTalkWindow(String friendName, String friendJid) throws IOException {
        String StatePath = getActivity().getExternalCacheDir().getAbsolutePath() + File.separator + "userState.json";
        jsonUtil.writeUserMsgState(StatePath, friendJid, Flags.isRead);
        View popView = LayoutInflater.from(getContext()).inflate(R.layout.activity_talk, null);
        Rect outSize = new Rect();
        getActivity().getWindowManager().getDefaultDisplay().getRectSize(outSize);
        talkWindow = new PopupWindow(popView, outSize.right - 200, outSize.bottom - 1000);
        talkWindow.setFocusable(true);
        talkWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.session_list));
        talkWindow.showAtLocation(curLayout, Gravity.CENTER, 0, 0);
        talkWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_FROM_FOCUSABLE);
        talkWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        WindowManager.LayoutParams lp = curActivity.getWindow().getAttributes();
        lp.alpha = 0.7f;
        curActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        curActivity.getWindow().setAttributes(lp);
        //区分不同用户的消息
        int myFlag = 0, hisFlag = 1;
        path = curActivity.getExternalCacheDir().getAbsolutePath() + File.separator + friendJid + ".json";
        TextView toolbar = popView.findViewById(R.id.backToTalk);
        EditText msgContent = popView.findViewById(R.id.msg_edit);
        Button msgSend = popView.findViewById(R.id.msg_send);
        toolbar.setText(friendName);
        ImageView delete = popView.findViewById(R.id.delete_friend);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                dialog.setTitle("警告").setMessage("是否删除该好友？").setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            connect.removeUser(friendName);
                            refreshItems(connect);
                            talkWindow.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
            }
        });
        msgList = popView.findViewById(R.id.msg_list);
        msgList.setAdapter(msgListViewAdapter = new MsgListViewAdapter(getContext(), msgItems));
        msgRefresh = popView.findViewById(R.id.msg_list_refresh);
        /*msgContent.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                InputMethodManager manager = (InputMethodManager) curActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (hasFocus) {
                    talkWindow.update(LinearLayout.LayoutParams.MATCH_PARENT, outSize.bottom - 850);
                    manager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                } else {
                    talkWindow.update(LinearLayout.LayoutParams.MATCH_PARENT, outSize.bottom);
                    manager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });*/
        msgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = String.valueOf(msgContent.getText());
                msgContent.setText("");
                msgContent.clearFocus();
                if (str == null || str.equals("")) return;
                try {
                    connect.sendMessage(friendJid, str);
                    jsonUtil.write(path, friendJid, friendName, str, String.valueOf(myFlag));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                msgItems.add(myFlag + USER_NAME + ":\n      " + str);
                msgListViewAdapter.notifyDataSetChanged();
            }
        });
        msgRefresh.setOnRefreshListener(msgRefreshListener);
        LoadMsg();
        talkWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                isTalking = false;
                lp.alpha = 1.0f;
                curActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                curActivity.getWindow().setAttributes(lp);
            }
        });
    }

    /**
     * 加载历史消息
     *
     * @throws IOException
     */
    private void LoadMsg() throws IOException {
        msgItems.clear();
        String content = jsonUtil.read(path);
        if (content == null) return;
        Gson gson = new Gson();
        JsonArray jsonArray = new JsonParser().parse(content).getAsJsonArray();
        //List<UserMsg> userMsgs = new ArrayList<>();
        for (JsonElement jsonElement : jsonArray) {
            UserMsg user = gson.fromJson(jsonElement, UserMsg.class);
            //userMsgs.add(user);
            msgItems.add(user.getFlag() + user.getUser() + ":\n      " + user.getMsg());
        }
        msgListViewAdapter.notifyDataSetChanged();
    }

    SwipeRefreshLayout.OnRefreshListener msgRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            msgRefreshHandler.sendEmptyMessageDelayed(1, 5);
        }
    };

    /**
     * 更新消息列表
     */
    Handler msgRefreshHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 1:
                    try {
                        LoadMsg();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    msgRefresh.setRefreshing(false);
                    break;
            }
        }
    };


}