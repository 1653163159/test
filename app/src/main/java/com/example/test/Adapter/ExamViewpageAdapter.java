package com.example.test.Adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

/**
 * @author : hqx
 * @date : 17/2/2023 下午 9:29
 * @descriptions: 0
 */
public class ExamViewpageAdapter extends FragmentPagerAdapter {
    List<String> titleList;
    List<Fragment> viewList;

    public ExamViewpageAdapter(FragmentManager fragmentManager, List<Fragment> viewList, List<String> titleList) {
        super(fragmentManager);
        this.viewList = viewList;
        this.titleList = titleList;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return viewList.get(position);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titleList.get(position);
    }

    @Override
    public int getCount() {
        return viewList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
