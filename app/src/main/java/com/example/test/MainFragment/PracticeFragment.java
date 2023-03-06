package com.example.test.MainFragment;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerTabStrip;
import androidx.viewpager.widget.ViewPager;

import android.text.Selection;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test.Adapter.PracticeViewpageAdapter;
import com.example.test.Practice.Flags;
import com.example.test.Practice.PracticeActivity;
import com.example.test.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PracticeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PracticeFragment extends Fragment {
    private View practiceLayout;
    private Activity curActivity;

    TextView listening, writing, speaking;
    private View listen_view, write_view, speak_view;
    String prefix = "http://10.27.199.250:8080/rest/";
    String level = "初级";
    TextView Lone, Ltwo, Sone, Stwo, Wone, Wtwo, Wthree;

    private List<View> viewList;
    private List<String> titleList;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PracticeFragment() {
        // Required empty public constructor
    }

    public void setLevel(String level) {
        this.level = level;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PracticeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PracticeFragment newInstance(String param1, String param2) {
        PracticeFragment fragment = new PracticeFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (practiceLayout == null) {
            practiceLayout = inflater.inflate(R.layout.fragment_practice, container, false);
        }
        if (curActivity == null) {
            curActivity = getActivity();
        }
        viewList = new ArrayList<>();
        listen_view = LayoutInflater.from(getContext()).inflate(R.layout.practice_view_hear, null);
        write_view = LayoutInflater.from(getContext()).inflate(R.layout.practice_view_write, null);
        speak_view = LayoutInflater.from(getContext()).inflate(R.layout.practice_view_speak, null);
        initLisView();
        viewList.add(listen_view);
        viewList.add(write_view);
        viewList.add(speak_view);
        titleList = new ArrayList<>();
        titleList.add("听力练习");
        titleList.add("阅读练习");
        titleList.add("口语练习");
        ViewPager viewPager = practiceLayout.findViewById(R.id.subject_viewpage);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setCurrentItem(0);
        PracticeViewpageAdapter viewpageAdapter = new PracticeViewpageAdapter(viewList, titleList);
        viewPager.setAdapter(viewpageAdapter);
        PagerTabStrip tabStrip = practiceLayout.findViewById(R.id.page_title);
        tabStrip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        tabStrip.setTextColor(Color.BLACK);
        tabStrip.setTabIndicatorColorResource(R.color.cornFlowerBlue);
        return practiceLayout;
    }

    void initLisView() {
        Lone = listen_view.findViewById(R.id.listen_type1);
        Ltwo = listen_view.findViewById(R.id.listen_type2);
        Lone.setOnClickListener(l1);
        Ltwo.setOnClickListener(l2);

        Sone = speak_view.findViewById(R.id.speak_type1);
        Stwo = speak_view.findViewById(R.id.speak_type2);
        Sone.setOnClickListener(s1);
        Stwo.setOnClickListener(s2);

        Wone = write_view.findViewById(R.id.write_type1);
        Wtwo = write_view.findViewById(R.id.write_type2);
        Wthree = write_view.findViewById(R.id.write_type3);
        Wone.setOnClickListener(w1);
        Wtwo.setOnClickListener(w2);
        Wthree.setOnClickListener(w3);
    }

    View.OnClickListener l1 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navigation(Lone.getText().toString(), Flags.LISTEN_TYPE1);
        }
    };
    View.OnClickListener l2 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navigation(Ltwo.getText().toString(), Flags.LISTEN_TYPE2);
        }
    };
    View.OnClickListener s1 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navigation(Sone.getText().toString(), Flags.SPEAK_TYPE1);
        }
    };
    View.OnClickListener s2 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navigation(Stwo.getText().toString(), Flags.SPEAK_TYPE2);
        }
    };
    View.OnClickListener w1 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navigation(Wone.getText().toString(), Flags.WRITE_TYPE1);
        }
    };
    View.OnClickListener w2 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navigation(Wtwo.getText().toString(), Flags.WRITE_TYPE2);
        }
    };
    View.OnClickListener w3 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navigation(Wthree.getText().toString(), Flags.WRITE_TYPE3);
        }
    };

    /**
     * 等级文本变换
     *
     * @param text
     * @return
     */
    private String getLevel(String text) {
        String level = "";
        switch (text) {
            case "低级":
                level = Flags.BEGINNER;
                break;
            case "中级":
                level = Flags.INTERMEDIATE;
                break;
            case "高级":
                level = Flags.ADVANCED;
                break;
        }
        return level;
    }


    private void navigation(String title, String type) {
        Intent intent = new Intent(getActivity(), PracticeActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("type", type);
        intent.putExtra("prefix", prefix);
        intent.putExtra("level", getLevel(level));
        startActivity(intent);
    }
}