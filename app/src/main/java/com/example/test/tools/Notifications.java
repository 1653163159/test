package com.example.test.tools;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.test.R;

import java.util.Random;

/**
 * @author : hqx
 * @date : 15/3/2023 上午 9:50
 * @descriptions:
 */
public class Notifications {
    NotificationCompat.Builder builder;
    NotificationManager manager;
    String channelId = "";

    public Notifications(String channel) {
        this.channelId = channel;
    }

    public void show(String title, String content, Context context) {
        manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //适配 Android 8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //创建     NotificationChannel 对象
            NotificationChannel channel = new NotificationChannel(channelId,
                    "chat message", NotificationManager.IMPORTANCE_HIGH);
            //创建通知渠道
            manager.createNotificationChannel(channel);
        }
        builder = new NotificationCompat.Builder(context, channelId)
                .setAutoCancel(true)
                .setPriority(2)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content);
        manager.notify(1, builder.build());
    }

    public void update(String title, String content) {
        if (builder != null) {
            builder.setContentTitle(title)
                    .setContentText(content);
            manager.notify(1, builder.build());
        }
    }
}
