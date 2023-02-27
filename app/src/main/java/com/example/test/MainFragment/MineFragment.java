package com.example.test.MainFragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.test.MainActivity;
import com.example.test.R;
import com.example.test.pojo.UserMsg;
import com.example.test.tools.JsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MineFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MineFragment extends Fragment {
    View view;

    TextView userName, login, logout;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MineFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MineFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MineFragment newInstance(String param1, String param2) {
        MineFragment fragment = new MineFragment();
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
        view = inflater.inflate(R.layout.fragment_mine, container, false);
        initView();

        return view;
    }

    void initView() {
        userName = view.findViewById(R.id.user_id);
        login = view.findViewById(R.id.login_in_mine);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).showLoginWindow();
            }
        });
        logout = view.findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).disconnectFromOpenfire();
            }
        });
        TextView jsonTest = view.findViewById(R.id.json);
        jsonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JsonUtil jsonUtil = new JsonUtil();
                String path = getActivity().getExternalCacheDir().getAbsolutePath() + File.separator + "hqx1" + ".json";
                try {
                    jsonUtil.write(path,"007","admin","Dirty deeds done dirt cheap","1");
                    String content = jsonUtil.read(path);
                    Gson gson = new Gson();
                    JsonArray jsonArray = new JsonParser().parse(content).getAsJsonArray();
                    List<UserMsg> userMsgs = new ArrayList<>();
                    for (JsonElement jsonElement : jsonArray) {
                        UserMsg user = gson.fromJson(jsonElement, UserMsg.class);
                        userMsgs.add(user);
                    }
                    System.out.println(userMsgs.get(0).getJid() + ":" + userMsgs.get(1).getMsg());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 登录后更新相关控件
     *
     * @param username
     */
    public void afterLoginSet(String username) {
        userName.setText(username);
    }

    /**
     * 添加登录按钮的监听器
     *
     * @param listener
     */
    public void setLoginListener(View.OnClickListener listener) {
        login.setOnClickListener(listener);
    }

}