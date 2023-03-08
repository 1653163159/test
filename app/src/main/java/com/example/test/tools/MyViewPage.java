package com.example.test.tools;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

/**
 * @author : hqx
 * @date : 8/3/2023 上午 10:40
 * @descriptions: 1
 */
public class MyViewPage extends ViewPager {
    public MyViewPage(@NonNull Context context) {
        super(context);
    }

    public MyViewPage(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_MOVE) {
            return false;
        } else {
            return super.onInterceptTouchEvent(ev);
        }
    }
}
