package com.example.test.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.test.R;
import com.example.test.pojo.Hsk;

import java.util.List;

/**
 * @author : hqx
 * @date : 15/2/2023 下午 12:36
 * @descriptions:
 */
public class ExamListViewAdapter extends BaseAdapter {
    LayoutInflater layoutInflater;
    List<Hsk> items;

    public ExamListViewAdapter(Context context, List<Hsk> items) {
        this.items = items;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.exam_list, parent, false);
            textView = convertView.findViewById(R.id.exam_id);
            convertView.setTag(textView);
        } else {
            textView = (TextView) convertView.getTag();
        }
        textView.setText(items.get(position).getIdhsk());
        textView.setVisibility(View.VISIBLE);
        return convertView;
    }
}
