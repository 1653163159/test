package com.example.test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.example.test.MainFragment.ExamFragment;
import com.example.test.MainFragment.IntegrationFragment;
import com.example.test.MainFragment.MineFragment;
import com.example.test.MainFragment.PracticeFragment;
import com.example.test.MainFragment.TalkFragment;
import com.example.test.Adapter.SearchListViewAdapter;
import com.example.test.Practice.Flags;
import com.example.test.pojo.userInform;
import com.example.test.tools.JsonUtil;
import com.example.test.tools.Notifications;
import com.example.test.tools.SmarkUtil;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    String prefix = "http://124.223.115.35/rest/";
    private PracticeFragment practiceFragment;
    private IntegrationFragment examFragment;
    private TalkFragment talkFragment;
    private MineFragment mineFragment;

    private int curID = R.id.tv_main;//设置默认主页
    private TextView tvMain, tvExam, tvTalk, tvMine;
    private ImageView search_friend, request_friend;
    private Spinner practiceLevel;

    //openfireConfig
    public String C_HOST = "124.223.115.35";//服务器在局域网中IP
    public String C_DOMAIN = "124.223.115.35";//服务器主机名
    public String USER_NAME = "hqx", USER_PWD = "123456";
    public SmarkUtil smarkUti;
    JsonUtil jsonUtil;

    //搜索弹窗相关
    PopupWindow popupWindow;
    Button sendSearch;
    EditText editText;
    ListView searchList;
    SearchListViewAdapter searchListViewAdapter;
    List<userInform> items = new ArrayList<>();

    private boolean isReady = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.cornFlowerBlue));
        }

        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        splashScreen.setKeepOnScreenCondition(() -> isReady);
        try {
            Thread.sleep(1000);
            isReady = false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    //初始化页面相关组件
    void initView() {
        prefix = Flags.PREFIX;

        //openfire相关
        USER_NAME = getString(R.string.userName);
        USER_PWD = getString(R.string.userPassword);
        C_HOST = getString(R.string.serverHost);
        C_DOMAIN = getString(R.string.serverDomain);
        jsonUtil = new JsonUtil();
        //底部导航栏
        tvMain = findViewById(R.id.tv_main);
        tvMain.setSelected(true);
        tvExam = findViewById(R.id.tv_exam);
        tvTalk = findViewById(R.id.tv_talk);
        tvMine = findViewById(R.id.tv_mine);

        practiceLevel = findViewById(R.id.practice_level);
        practiceLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String level = practiceLevel.getSelectedItem().toString();
                if (practiceFragment != null)
                    practiceFragment.setLevel(level);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //工具栏搜索和好友请求
        search_friend = findViewById(R.id.search_new_friend);
        request_friend = findViewById(R.id.friend_request);
        search_friend.setOnClickListener(imageClickListener);
        request_friend.setOnClickListener(imageClickListener);

        //默认加载页
        practiceFragment = new PracticeFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.main_container, practiceFragment).commit();

        tvMain.setOnClickListener(tabClickListener);
        tvExam.setOnClickListener(tabClickListener);
        tvTalk.setOnClickListener(tabClickListener);
        tvMine.setOnClickListener(tabClickListener);

    }

    //底部导航切换监听器
    private View.OnClickListener tabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() != curID) {
                changeSelect(view.getId());
                changeFragment(view.getId());
                curID = view.getId();
                if (view.getId() == R.id.tv_talk) {
                    if (smarkUti == null || !smarkUti.isConnected()) {
                        changeSelect(R.id.tv_mine);
                        changeFragment(R.id.tv_mine);
                        curID = R.id.tv_mine;
                        showLoginWindow();
                        Toast.makeText(MainActivity.this, "请先登录", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };

    /**
     * 切换Fragment显示
     */
    private void changeFragment(int id) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();//开启一个Fragment事务
        hideFragment(transaction);
        if (id == R.id.tv_main) {
            if (practiceFragment == null) {
                practiceFragment = PracticeFragment.newInstance(prefix, null);
                transaction.add(R.id.main_container, practiceFragment);
            } else {
                transaction.show(practiceFragment);
            }
        } else if (id == R.id.tv_exam) {
            if (examFragment == null) {
                examFragment = IntegrationFragment.newInstance(prefix, null);
                transaction.add(R.id.main_container, examFragment);
            } else {
                transaction.show(examFragment);
            }
        } else if (id == R.id.tv_talk) {
            if (talkFragment == null) {
                talkFragment = new TalkFragment();
                transaction.add(R.id.main_container, talkFragment);
            } else {
                transaction.show(talkFragment);
                talkFragment.refreshItems(smarkUti);
            }
        } else if (id == R.id.tv_mine) {
            if (mineFragment == null) {
                mineFragment = new MineFragment();
                transaction.add(R.id.main_container, mineFragment);
            } else {
                transaction.show(mineFragment);
            }
        }
        transaction.commit();
    }

    /**
     * 隐藏所有Fragment
     */
    private void hideFragment(FragmentTransaction transaction) {
        if (practiceFragment != null) transaction.hide(practiceFragment);
        if (examFragment != null) transaction.hide(examFragment);
        if (talkFragment != null) transaction.hide(talkFragment);
        if (mineFragment != null) transaction.hide(mineFragment);
    }

    /**
     * 改变选中导航的颜色
     */
    private void changeSelect(int id) {
        tvMain.setSelected(false);
        tvExam.setSelected(false);
        tvTalk.setSelected(false);
        tvMine.setSelected(false);
        practiceLevel.setVisibility(View.INVISIBLE);
        search_friend.setVisibility(View.INVISIBLE);
        request_friend.setVisibility(View.INVISIBLE);
        TextView title = findViewById(R.id.title_app);
        switch (id) {
            case R.id.tv_main:
                practiceLevel.setVisibility(View.VISIBLE);
                tvMain.setSelected(true);
                title.setText("练习");
                break;
            case R.id.tv_exam:
                tvExam.setSelected(true);
                title.setText("考级");
                break;
            case R.id.tv_talk:
                search_friend.setVisibility(View.VISIBLE);
                request_friend.setVisibility(View.VISIBLE);
                tvTalk.setSelected(true);
                title.setText("交流");
                break;
            case R.id.tv_mine:
                tvMine.setSelected(true);
                title.setText("我的");
                break;
        }
    }

    //顶部搜索监听器，显示弹窗
    View.OnClickListener imageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showPopWindow(v);
        }
    };


    /**
     * 显示搜索弹窗
     *
     * @param v
     */
    private void showPopWindow(View v) {
        View popView = null;
        switch (v.getId()) {
            case R.id.search_new_friend:
                popView = LayoutInflater.from(this).inflate(R.layout.search_friend, (ViewGroup) findViewById(R.id.Spopwindow_element));
                break;
            case R.id.friend_request:
                popView = LayoutInflater.from(this).inflate(R.layout.request_friend, (ViewGroup) findViewById(R.id.Spopwindow_element));
                break;
        }
        popupWindow = new PopupWindow(popView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        switch (v.getId()) {
            case R.id.search_new_friend:
                //弹窗组件获取,必须在popWindow的上下文中才能找到控件
                popupWindow.showAsDropDown(search_friend, 0, 50);
                searchList = popView.findViewById(R.id.search_list);
                searchList.setVisibility(View.INVISIBLE);
                sendSearch = popView.findViewById(R.id.send_search);
                editText = popView.findViewById(R.id.search_friend_name);
                searchList.setAdapter(searchListViewAdapter = new SearchListViewAdapter(getApplicationContext(), items));
                searchList.setOnItemClickListener(searchListListener);
                items.clear();
                searchListViewAdapter.notifyDataSetChanged();
                sendSearch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        searchList.setVisibility(View.VISIBLE);
                        items.clear();
                        String str = editText.getText().toString();
                        if (str == null || str.equals("") || str.equals(" ")) return;
                        editText.setText("");
                        try {
                            smarkUti.searchEntries(str, items);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (items.size() == 0) {
                            userInform user = new userInform("用户不存在", "请重新查找");
                            items.add(user);
                            searchList.setEnabled(false);
                        } else {
                            searchList.setEnabled(true);
                        }
                        searchListViewAdapter.notifyDataSetChanged();
                    }
                });
                break;
            case R.id.friend_request:
                popupWindow.showAsDropDown(request_friend);
                break;
        }
    }

    //监听搜索列表中选中数据
    AdapterView.OnItemClickListener searchListListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String jid = items.get(position).getJid();
            System.out.println("searchlist" + jid);
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setTitle("添加好友").setMessage("您确定要添加【" + jid + "】为好友吗？").setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        smarkUti.addSearchFriend(jid, items.get(position).getName());
                    } catch (XmppStringprepException e) {
                        e.printStackTrace();
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (XMPPException.XMPPErrorException e) {
                        e.printStackTrace();
                    } catch (SmackException.NoResponseException e) {
                        e.printStackTrace();
                    } catch (SmackException.NotLoggedInException e) {
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
    };

    /**
     * stanza监听器
     */
    StanzaListener stanzaListener = new StanzaListener() {
        @Override
        public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException, SmackException.NotLoggedInException {
            if (packet instanceof Presence) {
                Presence presence = (Presence) packet;
                try {
                    if (presence.getType().equals(Presence.Type.subscribe)) {
                        if (smarkUti.getUserByJid(presence.getFrom().toString())) {
                            System.out.println(presence.getFrom() + "已同意您的好友申请");
                        } else {
                            System.out.println(presence.getFrom() + "向您发送好友申请");
                            Message message = friendRequestTip.obtainMessage();
                            message.obj = presence.getFrom();
                            message.sendToTarget();
                        }
                    }
                    if (presence.getType().equals(Presence.Type.subscribed)) {
                        System.out.println(presence.getFrom() + "已同意您的好友申请");
                    } else if (presence.getType().equals(Presence.Type.unsubscribe)) {
                        System.out.println(presence.getFrom() + "拒绝了您的好友申请");
                        smarkUti.removeUser(presence.getFrom().toString().split("@")[0]);
                    }
                        /*if (presence.getType().equals(Presence.Type.available)) {
                            System.out.println(presence.getFrom() + "已上线");
                        } else if (presence.getType().equals(Presence.Type.unavailable)) {
                            System.out.println(presence.getFrom() + "已下线");
                        }*/
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    /**
     * 处理对方发送来的好友申请
     */
    Handler friendRequestTip = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            String str = msg.obj.toString();
            System.out.println(str);
            if (str == null || str.equals("")) return;
            showRequestDialog(str);
        }
    };

    private void showRequestDialog(String str) {
        String jid = str.split("/")[0];
        String name = str.split("@")[0];
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("好友申请").setMessage("【" + jid + "】向您发起好友申请，是否同意？").setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    smarkUti.addUser(name, name, "Friends");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    smarkUti.removeUser(name);
                } catch (XmppStringprepException e) {
                    e.printStackTrace();
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        }).create().show();
    }


    /**
     * 显示登录窗口
     */
    PopupWindow loginWindow;

    public void showLoginWindow() {
        View popView = LayoutInflater.from(this).inflate(R.layout.login, null);
        loginWindow = new PopupWindow(popView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        loginWindow.setOutsideTouchable(true);
        loginWindow.setFocusable(true);
        loginWindow.setBackgroundDrawable(new BitmapDrawable());
        loginWindow.showAsDropDown(search_friend);
        EditText username = popView.findViewById(R.id.get_username), userPwd = popView.findViewById(R.id.get_user_password);
        Button login = popView.findViewById(R.id.login), register = popView.findViewById(R.id.register);
        username.setText("hqx");
        userPwd.setText("123456");
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (username.getText().equals("") || userPwd.getText().equals("")) return;
                USER_NAME = String.valueOf(username.getText()).trim();
                USER_PWD = String.valueOf(userPwd.getText()).trim();
                link.sendEmptyMessage(1);
                mineFragment.afterLoginSet(USER_NAME);
                System.out.println(USER_NAME + "-----" + USER_PWD);
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    Handler link = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            linkToOpenfire();
        }
    };

    /**
     * 连接服务器,并更新控件
     */
    private void linkToOpenfire() {
        try {
            smarkUti = new SmarkUtil(USER_NAME, USER_PWD, C_DOMAIN, C_HOST);
            smarkUti.getOfflineMessage(getExternalCacheDir().getAbsolutePath());
            smarkUti.addPacketListener(stanzaListener);
            smarkUti.addIncomingChatMessageListener(getMsgListener);
            talkFragment.setOpenfireConfig(smarkUti, C_DOMAIN, C_HOST, USER_NAME, USER_PWD);
            if (smarkUti.isLogin()) {
                Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                loginWindow.dismiss();
            }
        } catch (Exception e) {
            System.out.println("Chat服务连接失败" + e.getLocalizedMessage());
            Toast.makeText(MainActivity.this, "登录失败，请检查用户名和密码", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * 登录后监听是否有消息发送，更新talkFragment列表，显示消息来源
     */
    IncomingChatMessageListener getMsgListener = new IncomingChatMessageListener() {
        @Override
        public void newIncomingMessage(EntityBareJid from, org.jivesoftware.smack.packet.Message message, Chat chat) {
            System.out.println("收到消息" + from + ":" + message.getBody());
            String path = getExternalCacheDir().getAbsolutePath() + File.separator + from + ".json";
            try {
                jsonUtil.write(path, from.toString(), from.toString().split("@")[0], message.getBody(), "1");
            } catch (IOException e) {
                e.printStackTrace();
            }

            Message message1 = getMsgHandler.obtainMessage();
            message1.obj = from + "&" + message.getBody();
            message1.sendToTarget();
        }
    };
    Handler getMsgHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            String res = msg.obj.toString();
            //System.out.println("jid:"+res.split("&")[0]);
            updateUserList(res.split("&")[0]);
            Toast.makeText(getApplicationContext(), "收到来自" + res.split("&")[0].split("@")[0] + "的消息:\n    " + res.split("&")[1], Toast.LENGTH_LONG).show();
            new Notifications("userMsg").show(res.split("&")[0].split("@")[0], res.split("&")[1], MainActivity.this);
            String StatePath = getExternalCacheDir().getAbsolutePath() + File.separator + "userState.json";
            if (talkFragment.isTalking && res.split("&")[0].equals(talkFragment.FriendJid)) {
                talkFragment.msgItems.add("1" + res.split("@")[0] + ":\n      " + res.split("&")[1]);
                talkFragment.msgListViewAdapter.notifyDataSetChanged();
                try {
                    jsonUtil.writeUserMsgState(StatePath, res.split("&")[0], Flags.isRead);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    jsonUtil.writeUserMsgState(StatePath, res.split("&")[0], Flags.notRead);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    /**
     * 收到新消息时，应当给出提醒
     *
     * @param msgFrom
     */
    private void updateUserList(String msgFrom) {
        if (talkFragment != null) {
            int index = talkFragment.findItemByJid(msgFrom);
            if (talkFragment.userList.getChildAt(index) == null) return;
            TextView textView = talkFragment.userList.getChildAt(index).findViewById(R.id.item1);
            if (textView == null) return;
            Drawable drawable = getDrawable(R.drawable.msg_tip);
            drawable.setBounds(0, 0, 30, 30);
            Drawable drawable2 = getDrawable(R.drawable.arrow_right);
            drawable2.setBounds(0, 0, 100, 100);
            textView.setCompoundDrawables(drawable2, null, drawable, null);
        }
    }

    /**
     * 退出登录
     */
    public void disconnectFromOpenfire() throws Exception {
        if (smarkUti != null) {
            smarkUti.close();
            mineFragment.afterLoginSet("");
            return;
        }
        Toast.makeText(MainActivity.this, "尚未登录", Toast.LENGTH_SHORT).show();
    }


    private int exitTime = 2;

    //重写系统返回键的功能（仅在当前页面生效）
    @Override
    public void onBackPressed() {
        if (talkFragment.isTalking) {
            System.out.println("asdahdsadhash ahdsaohs dladlsas ");
            talkFragment.talkWindow.dismiss();
            talkFragment.isTalking = false;
        }
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
        if (--exitTime == 0) {
            finish();
            return;
        }
        Toast.makeText(getApplicationContext(), "再次点击退出", Toast.LENGTH_SHORT).show();
    }

    /**
     * 获取点击事件
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (isHideInput(view, ev)) {
                HideSoftInput(view.getWindowToken());
                view.clearFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 判定是否需要隐藏
     */
    private boolean isHideInput(View v, MotionEvent ev) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left + v.getWidth();
            if (ev.getX() > left && ev.getX() < right && ev.getY() > top && ev.getY() < bottom) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * 隐藏软键盘
     */
    private void HideSoftInput(IBinder token) {
        if (token != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}