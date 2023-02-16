package com.example.test.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerTabStrip;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.test.Adapter.PracticeViewpageAdapter;
import com.example.test.R;
import com.example.test.SubjectFragment.S1Fragment;
import com.example.test.SubjectFragment.S2Fragment;
import com.example.test.SubjectFragment.S3Fragment;
import com.example.test.SubjectFragment.SubjectActivity;

import org.minidns.record.Record;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PracticeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PracticeFragment extends Fragment {
    private View practiceLayout;
    private Activity curActivity;

    TextView listening, writing, speaking;
    private S1Fragment listenFragment;
    private S2Fragment writeFragment;
    private S3Fragment speakFragment;
    String prefix = "http://10.27.199.250:8080/rest/";

    private List<Fragment> viewList;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (practiceLayout == null) {
            practiceLayout = inflater.inflate(R.layout.fragment_practice, container, false);
        }
        if (curActivity == null) {
            curActivity = getActivity();
        }
        viewList = new ArrayList<>();
        viewList.add(listenFragment = S1Fragment.newInstance(prefix, null));
        viewList.add(writeFragment = S2Fragment.newInstance(prefix, null));
        viewList.add(speakFragment = S3Fragment.newInstance(prefix, null));
        titleList = new ArrayList<>();
        titleList.add("听力练习");
        titleList.add("书写练习");
        titleList.add("口语练习");
        ViewPager viewPager = practiceLayout.findViewById(R.id.subject_viewpage);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setCurrentItem(0);
        PracticeViewpageAdapter viewpageAdapter = new PracticeViewpageAdapter(getFragmentManager(), viewList, titleList);
        viewPager.setAdapter(viewpageAdapter);
        PagerTabStrip tabStrip = practiceLayout.findViewById(R.id.page_title);
        tabStrip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        tabStrip.setTextColor(Color.BLACK);
        tabStrip.setTabIndicatorColorResource(R.color.cornFlowerBlue);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                listenFragment.onDestroy();
                writeFragment.onDestroy();
                speakFragment.onDestroy();
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        return practiceLayout;
    }

    void initView() {
        listening = practiceLayout.findViewById(R.id.practice_hear);
        writing = practiceLayout.findViewById(R.id.practice_write);
        speaking = practiceLayout.findViewById(R.id.practice_spoke);
        listening.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SubjectActivity.class);
                intent.putExtra("type", "听力练习");
                startActivityForResult(intent, 11);

            }
        });
        writing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SubjectActivity.class);
                intent.putExtra("type", "书法练习");
                startActivityForResult(intent, 11);

            }
        });
        speaking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SubjectActivity.class);
                intent.putExtra("type", "口语练习");
                startActivityForResult(intent, 11);

            }
        });
    }
}