package com.example.test.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test.Practice.PracticeActivity;
import com.example.test.R;
import com.example.test.pojo.Stack;
import com.example.test.tools.MyTextView;

import java.util.List;

/**
 * @author : hqx
 * @date : 8/3/2023 下午 9:48
 * @descriptions: 0
 */
public class BookListViewAdapter extends BaseAdapter {
    List<Stack> list;
    LayoutInflater inflater;
    Context context;
    String title, type, prefix, level;

    public BookListViewAdapter(Context context, List<Stack> list) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.list = list;
    }

    public void setParams(String title, String type, String prefix, String level) {
        this.title = title;
        this.type = type;
        this.level = level;
        this.prefix = prefix;
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
        MyTextView name;
        ImageView imageView;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.book_view, null);
            name = convertView.findViewById(R.id.book_name);
            imageView = convertView.findViewById(R.id.book_image);
            convertView.setTag(name);
        } else {
            name = (MyTextView) convertView.getTag();
            imageView = convertView.findViewById(R.id.book_image);
        }
        name.setText(list.get(position).getBookname().toString());
        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(name.getText().toString());
            }
        });
        switch (position % 3) {
            case 0:
                imageView.setBackgroundResource(R.drawable.d4c);
                break;
            case 1:
                imageView.setBackgroundResource(R.drawable.sl);
                break;
            case 2:
                imageView.setBackgroundResource(R.drawable.eula);
                break;
        }
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(name.getText().toString());
            }
        });
        return convertView;
    }

    void showDialog(String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("提示")
                .setMessage("是否选择此书：" + name)
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        click(name);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    void click(String bookname) {
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(context, PracticeActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("type", type);
                intent.putExtra("prefix", prefix);
                intent.putExtra("level", level);
                intent.putExtra("name", bookname);
                context.startActivity(intent);
            }
        }.start();
    }
}
