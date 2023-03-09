package com.example.test.Practice;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.example.test.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SpeechMaterialFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SpeechMaterialFragment extends Fragment {
    View curLayout;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SpeechMaterialFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SpeechMeteriaFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SpeechMaterialFragment newInstance(String param1, String param2) {
        SpeechMaterialFragment fragment = new SpeechMaterialFragment();
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
        curLayout = inflater.inflate(R.layout.fragment_speech_meteria, container, false);
        TextView textView = curLayout.findViewById(R.id.speech_material);
        ListView listView = curLayout.findViewById(R.id.speech_chapter_list);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listView.getVisibility() == View.VISIBLE) {
                    listView.setVisibility(View.INVISIBLE);
                } else {
                    startAnimation(listView);
                }
            }
        });
        return curLayout;
    }

    private void startAnimation(ListView view) {
        Handler handler = new Handler();
        view.setVisibility(View.GONE);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.VISIBLE);
                ValueAnimator anim = ObjectAnimator.ofFloat(view, "translationX", -500, 0);
                anim.setDuration(200);
                anim.start();
            }
        }, 0);
    }

}