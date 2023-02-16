package com.example.test.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.ColorInt;

import com.example.test.R;


import java.util.List;

public class MsgListViewAdapter extends BaseAdapter {
    List<String> data;
    LayoutInflater inflater;

    public MsgListViewAdapter(Context context, List<String> items) {
        inflater = LayoutInflater.from(context);
        this.data = items;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.msg_list, parent, false);
            textView = convertView.findViewById(R.id.msg1);

            convertView.setTag(textView);
        } else {
            textView = (TextView) convertView.getTag();
        }
        String str = data.get(position);
        switch (str.charAt(0)) {
            case '0':
                textView.setTextColor(Color.parseColor("#FF32CD32"));
                break;
            case '1':
                textView.setTextColor(Color.BLACK);
        }
        textView.setText(str.substring(1));
        return convertView;
    }
}
