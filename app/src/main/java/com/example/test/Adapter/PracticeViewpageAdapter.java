package com.example.test.Adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * @author : hqx
 * @date : 10/2/2023 上午 9:48
 * @descriptions: 翻页视图适配器
 */
public class PracticeViewpageAdapter extends FragmentPagerAdapter {
    List<Fragment> viewList;
    List<String> titleList;

    public PracticeViewpageAdapter(FragmentManager fragmentManager, List<Fragment> viewList, List<String> titleList) {
        super(fragmentManager);
        this.viewList = viewList;
        this.titleList = titleList;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return viewList.get(position);
    }

    @Override
    public int getCount() {
        return viewList.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titleList.get(position);
    }
}
