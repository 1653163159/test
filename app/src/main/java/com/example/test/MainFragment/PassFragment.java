package com.example.test.MainFragment;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test.Practice.Flags;
import com.example.test.R;
import com.example.test.pojo.Pass;
import com.example.test.tools.JsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PassFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PassFragment extends Fragment {
    public static final String STATE_LOCK = "locked", STATE_UNLOCK = "unlocked";

    View curLayout, view;
    TextView pass1, pass2, pass3, pass4, pass5, pass6, pass7, pass8, name, time;
    List<TextView> viewList;
    List<Pass> passStateList;
    List<ImageView> stars;
    Drawable drawable;
    String path;
    int index = 0;//用来标识当前选中的是哪个关卡
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String prefix;
    private String mParam2;

    public PassFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PassFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PassFragment newInstance(String param1, String param2) {
        PassFragment fragment = new PassFragment();
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
    public void onResume() {
        super.onResume();
        LoadPassState();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        curLayout = inflater.inflate(R.layout.fragment_pass, container, false);
        initView();
        LoadPassState();
        return curLayout;
    }

    private void initView() {
        viewList = new ArrayList<>();
        passStateList = new ArrayList<>();
        stars = new ArrayList<>();
        pass1 = curLayout.findViewById(R.id.pass_1);
        pass2 = curLayout.findViewById(R.id.pass_2);
        pass3 = curLayout.findViewById(R.id.pass_3);
        pass4 = curLayout.findViewById(R.id.pass_4);
        pass5 = curLayout.findViewById(R.id.pass_5);
        pass6 = curLayout.findViewById(R.id.pass_6);
        pass7 = curLayout.findViewById(R.id.pass_7);
        pass8 = curLayout.findViewById(R.id.pass_8);
        viewList.add(pass1);
        viewList.add(pass2);
        viewList.add(pass3);
        viewList.add(pass4);
        viewList.add(pass5);
        viewList.add(pass6);
        viewList.add(pass7);
        viewList.add(pass8);
        for (int i = 0; i < viewList.size(); i++) {
            viewList.get(i).setOnClickListener(clickListener);
        }
        path = getActivity().getExternalCacheDir().getAbsolutePath() + File.separator + "PassState.json";

        view = LayoutInflater.from(getContext()).inflate(R.layout.pass_overview, null);
        name = view.findViewById(R.id.pass_name);
        time = view.findViewById(R.id.pass_times);
        stars.add(view.findViewById(R.id.im1));
        stars.add(view.findViewById(R.id.im2));
        stars.add(view.findViewById(R.id.im3));

        drawable = getActivity().getDrawable(R.drawable.ic_baseline_hexagon_pass);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
    }

    /**
     * 加载关卡状态，目前采用本地保存，日后用户系统完善后可以将本地文件传给服务器，
     * 一个用户对应一个文件，并且通过对比服务器文件保证本地文件未被修改
     * 在此方法中可以更换为向服务器请求加载数据
     */
    void LoadPassState() {
        passStateList.clear();
        System.out.println("----------------------文件加载中------------------------");
        File dir = new File(path);
        JsonArray jsonArray = null;
        // 一、检查放置文件的文件夹路径是否存在，不存在则创建
        if (!dir.exists()) {
            //dir.mkdirs();// mkdirs创建多级目录
            jsonArray = new JsonArray();
            System.out.println("文件不存在");
            for (int i = 0; i < viewList.size(); i++) {
                Pass pass = new Pass(i, 3, viewList.get(i).getText().toString(), STATE_LOCK);
                if (i == 0)
                    pass.setState(STATE_UNLOCK);
                passStateList.add(pass);
                jsonArray.add(new Gson().toJsonTree(pass));
            }
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(path);
                FileChannel fileChannel = fileOutputStream.getChannel();
                FileLock fileLock = fileChannel.tryLock();
                fileOutputStream.write(jsonArray.toString().getBytes(StandardCharsets.UTF_8));
                fileLock.release();
                fileOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                String content = new JsonUtil().read(path);
                jsonArray = new JsonParser().parse(content).getAsJsonArray();
                for (JsonElement jsonElement : jsonArray) {
                    Pass pass = new Gson().fromJson(jsonElement, Pass.class);
                    passStateList.add(pass);
                }
                updatePopWindow();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < viewList.size(); i++) {
            if (passStateList.get(i).getTimes() == 0)
                viewList.get(i).setCompoundDrawables(drawable, null, null, null);
        }
    }

    View.OnClickListener clickListener = v -> {
        TextView view = null;
        for (int i = 0; i < viewList.size(); i++) {
            if (v.getId() == viewList.get(i).getId()) {
                view = viewList.get(i);
                index = i;
            }
        }
        if (view == null) return;
        if (passStateList.get(index).getState().equals(STATE_UNLOCK)) {
            showPopWindow();
        } else {
            Toast.makeText(getContext(), "关卡未解锁", Toast.LENGTH_SHORT).show();
        }
    };

    void showPopWindow() {
        /*WindowManager wm = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            wm = (WindowManager) getActivity().getSystemService(getContext().WINDOW_SERVICE);
        }
        int height = wm.getDefaultDisplay().getHeight();*/
        PopupWindow popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAtLocation(curLayout, Gravity.BOTTOM, 0, 0);
        Pass pass = passStateList.get(index);
        name.setText(pass.getName());
        Button button = view.findViewById(R.id.pass_start);
        button.setOnClickListener(v -> {
            String level = "";
            if (index < 3) level = Flags.BEGINNER;
            if (index >= 3 && index < 6) level = Flags.INTERMEDIATE;
            if (index >= 6) level = Flags.ADVANCED;
            navigation(pass.getName(), index, level);
            /*int count = passStateList.get(index).getTimes();
            if (count > 0) {
                count -= 1;
                passStateList.get(index).setTimes(count);
            }
            if (count == 0) {
                if (index < passStateList.size() - 1) {
                    passStateList.get(index + 1).setState(STATE_UNLOCK);
                }
            }
            UpdateJson();*/
        });
        updatePopWindow();
    }

    /**
     * 更新本地文件
     */
    void UpdateJson() {
        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < passStateList.size(); i++) {
            jsonArray.add(new Gson().toJsonTree(passStateList.get(i)));
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(path);
            FileChannel fileChannel = fileOutputStream.getChannel();
            FileLock fileLock = fileChannel.tryLock();
            fileOutputStream.write(jsonArray.toString().getBytes(StandardCharsets.UTF_8));
            fileLock.release();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新关卡弹窗的信息
     */
    void updatePopWindow() {
        time.setText("当前仍需通过" + passStateList.get(index).getTimes() + "次才能进行下一关");
        for (int i = 0; i < stars.size(); i++) {
            stars.get(i).setImageResource(R.drawable.ic_baseline_hexagon_default);
        }
        for (int i = 0; i < 3 - passStateList.get(index).getTimes(); i++) {
            stars.get(i).setImageResource(R.drawable.ic_baseline_hexagon_pass);
        }
    }

    /**
     * 跳转到答题界面
     *
     * @param title
     * @param index
     * @param level
     */
    private void navigation(String title, int index, String level) {
        Intent intent = new Intent(getActivity(), PassActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("prefix", prefix);
        intent.putExtra("index", index);
        intent.putExtra("level", level);
        startActivity(intent);
    }
}