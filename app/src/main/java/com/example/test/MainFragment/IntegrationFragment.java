package com.example.test.MainFragment;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerTabStrip;
import androidx.viewpager.widget.ViewPager;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.test.Adapter.ExamViewpageAdapter;
import com.example.test.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link IntegrationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class IntegrationFragment extends Fragment {
    View curLayout;
    ViewPager viewPager;
    List<String> title;
    List<Fragment> viewList;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String prefix;
    private String mParam2;

    public IntegrationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment IntegrationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static IntegrationFragment newInstance(String param1, String param2) {
        IntegrationFragment fragment = new IntegrationFragment();
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
        curLayout = inflater.inflate(R.layout.fragment_integration, container, false);

        initView();
        return curLayout;
    }

    private void initView() {
        viewPager = curLayout.findViewById(R.id.viewpage);
        title = new ArrayList<>();
        title.add("闯关");
        title.add("考级");
        viewList = new ArrayList<>();
        ExamFragment examFragment = ExamFragment.newInstance(prefix, null);
        PassFragment passFragment = PassFragment.newInstance(prefix, null);
        viewList.add(passFragment);
        viewList.add(examFragment);
        viewPager.setAdapter(new ExamViewpageAdapter(getActivity().getSupportFragmentManager(), viewList, title));
        PagerTabStrip tabStrip = curLayout.findViewById(R.id.page_title);
        tabStrip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        tabStrip.setTextColor(Color.BLACK);
        tabStrip.setTabIndicatorColorResource(R.color.cornFlowerBlue);
    }
}