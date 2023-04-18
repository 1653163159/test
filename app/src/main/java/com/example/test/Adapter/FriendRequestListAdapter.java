package com.example.test.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.test.MainActivity;
import com.example.test.R;
import com.example.test.pojo.FriendRequest;
import com.example.test.tools.SmarkUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

/**
 * @author : hqx
 * @date : 18/4/2023 下午 12:30
 * @descriptions:
 */
public class FriendRequestListAdapter extends BaseExpandableListAdapter {
    SmarkUtil smarkUtil;
    MainActivity context;
    List<String> groupList = new ArrayList<>();
    List<List<FriendRequest>> childList = new ArrayList<>();
    List<FriendRequest> requests;
    List<FriendRequest> receiver = new ArrayList<>();
    List<FriendRequest> promoter = new ArrayList<>();

    public FriendRequestListAdapter(List<FriendRequest> items, SmarkUtil smarkUtil, MainActivity context) {
        requests = items;
        this.smarkUtil = smarkUtil;
        this.context = context;
        groupList.add("我申请的");
        groupList.add("我收到的");
        for (int i = 0; i < requests.size(); i++) {
            if (requests.get(i).getPromoter().equals(smarkUtil.getUserByString())) {
                promoter.add(requests.get(i));
            } else {
                receiver.add(requests.get(i));
            }
        }
        childList.add(promoter);
        childList.add(receiver);
    }

    @Override
    public int getGroupCount() {
        return groupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childList.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childList.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewHolderItem item;
        if (convertView == null) {
            item = new ViewHolderItem();
            convertView = LayoutInflater.from(context).inflate(R.layout.request_list, null);
            item.img_icon = convertView.findViewById(R.id.pic);
            item.tv_name = convertView.findViewById(R.id.name);
            item.temp = convertView.findViewById(R.id.temp);
            item.agree = convertView.findViewById(R.id.agree);
            item.reject = convertView.findViewById(R.id.reject);
            convertView.setTag(item);
        } else {
            item = (ViewHolderItem) convertView.getTag();
        }
        item.img_icon.setVisibility(View.INVISIBLE);
        item.tv_name.setText(groupList.get(groupPosition));
        item.temp.setVisibility(View.INVISIBLE);
        item.agree.setVisibility(View.INVISIBLE);
        item.reject.setVisibility(View.INVISIBLE);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolderItem item;
        if (convertView == null) {
            item = new ViewHolderItem();
            convertView = LayoutInflater.from(context).inflate(R.layout.request_list, null);
            item.img_icon = convertView.findViewById(R.id.pic);
            item.tv_name = convertView.findViewById(R.id.name);
            item.temp = convertView.findViewById(R.id.temp);
            item.agree = convertView.findViewById(R.id.agree);
            item.reject = convertView.findViewById(R.id.reject);
            convertView.setTag(item);
        } else {
            item = (ViewHolderItem) convertView.getTag();
        }
        item.img_icon.setImageDrawable(context.getDrawable(R.drawable.pic));
        if (childList.get(groupPosition).get(childPosition).getPromoter().equals(smarkUtil.getUserByString())) {
            item.tv_name.setText(childList.get(groupPosition).get(childPosition).getReceiver());
            item.agree.setVisibility(View.INVISIBLE);
            item.temp.setVisibility(View.VISIBLE);
            item.reject.setText("取消");
        } else {
            item.tv_name.setText(childList.get(groupPosition).get(childPosition).getPromoter());
            item.agree.setVisibility(View.VISIBLE);
            item.temp.setVisibility(View.INVISIBLE);
            item.reject.setText("拒绝");
        }
        item.reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String user = childList.get(groupPosition).get(childPosition).getPromoter();
                    context.delete_friend_request(user, childList.get(groupPosition).get(childPosition).getReceiver());
                    context.friendRequestList(smarkUtil.getUserByString(), smarkUtil.getUserByString());
                    if (user.equals(smarkUtil.getUserByString())) {
                        if (smarkUtil.getUserByJid(childList.get(groupPosition).get(childPosition).getReceiver()))
                            smarkUtil.removeUser(childList.get(groupPosition).get(childPosition).getReceiver().split("@")[0]);
                    } else {
                        if (smarkUtil.getUserByJid(childList.get(groupPosition).get(childPosition).getPromoter()))
                            smarkUtil.removeUser(childList.get(groupPosition).get(childPosition).getPromoter().split("@")[0]);
                    }
                    childList.get(groupPosition).remove(childPosition);
                    notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        item.agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String promoter = childList.get(groupPosition).get(childPosition).getPromoter();
                context.delete_friend_request(promoter, childList.get(groupPosition).get(childPosition).getReceiver());
                context.friendRequestList(smarkUtil.getUserByString(), smarkUtil.getUserByString());
                try {
                    smarkUtil.addUser(promoter.split("@")[0], promoter.split("@")[0], "Friends");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                childList.get(groupPosition).remove(childPosition);
                notifyDataSetChanged();
            }
        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private static class ViewHolderItem {
        private ImageView img_icon;
        private TextView tv_name, temp;
        private Button agree, reject;
    }
}
