package com.example.test.Adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.test.R;

import org.jivesoftware.smack.roster.RosterEntry;

import java.util.List;

public class UserListViewAdapter extends BaseAdapter {
    List<RosterEntry> data;
    LayoutInflater inflater;

    public UserListViewAdapter(Context context, List<RosterEntry> items) {
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
            convertView = inflater.inflate(R.layout.fragment_talk, parent, false);
            textView = convertView.findViewById(R.id.item1);
            convertView.setTag(textView);
        } else {
            textView = (TextView) convertView.getTag();
        }
        textView.setText(data.get(position).getName());
        textView.setVisibility(View.VISIBLE);
        return convertView;
    }

}
