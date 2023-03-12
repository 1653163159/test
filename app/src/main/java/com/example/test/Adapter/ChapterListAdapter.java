package com.example.test.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.test.R;
import com.example.test.pojo.Stack;

import java.util.List;

/**
 * @author : hqx
 * @date : 10/3/2023 上午 10:37
 * @descriptions:
 */
public class ChapterListAdapter extends BaseAdapter {
    List<Stack> list;
    Context context;

    public ChapterListAdapter(Context context, List<Stack> list) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView view;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.chapter_list, parent, false);
            view = convertView.findViewById(R.id.chapter);
            convertView.setTag(view);
        } else {
            view = (TextView) convertView.getTag();
        }
        view.setText(list.get(position).getChaptername());
        return convertView;
    }
}
