package com.example.test.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.test.R;
import com.example.test.pojo.userInform;

import org.jivesoftware.smack.roster.RosterEntry;

import java.util.List;

public class SearchListViewAdapter extends BaseAdapter {
    List<userInform> data;
    LayoutInflater inflater;

    public SearchListViewAdapter(Context context, List<userInform> items) {
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
            convertView = inflater.inflate(R.layout.search_list, parent, false);
            textView = convertView.findViewById(R.id.search1);
            convertView.setTag(textView);
        } else {
            textView = (TextView) convertView.getTag();
        }
        String jid = data.get(position).getJid();
        textView.setText("JID:" + data.get(position).getJid() + "\nName:" + data.get(position).getName());
        return convertView;
    }
}
