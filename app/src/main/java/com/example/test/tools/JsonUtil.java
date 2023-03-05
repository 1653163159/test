package com.example.test.tools;

import com.example.test.Practice.Flags;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;

/**
 * @author : hqx
 * @date : 6/2/2023 上午 11:10
 * @descriptions: Json操作
 */
public class JsonUtil {
    public void write(String path, String jid, String user, String msg, String flag) throws IOException {
        String content = read(path);
        JsonArray jsonArray;
        if (content == null) {
            jsonArray = new JsonArray();
        } else {
            jsonArray = new JsonParser().parse(content).getAsJsonArray();
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("jid", jid);
        jsonObject.addProperty("user", user);
        jsonObject.addProperty("msg", msg);
        jsonObject.addProperty("flag", flag);

        jsonArray.add(jsonObject);
        FileOutputStream fileOutputStream = new FileOutputStream(path);
        FileChannel fileChannel = fileOutputStream.getChannel();
        FileLock fileLock = fileChannel.tryLock();
        fileOutputStream.write(jsonArray.toString().getBytes(StandardCharsets.UTF_8));
        fileLock.release();
        fileOutputStream.close();
    }

    public void writeUserMsgState(String path, String jid, String flag) throws IOException {
        String content = read(path);
        JsonArray jsonArray;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("jid", jid);
        jsonObject.addProperty("flag", flag);
        if (content == null) {
            jsonArray = new JsonArray();
            jsonArray.add(jsonObject);
        } else {
            jsonArray = new JsonParser().parse(content).getAsJsonArray();
            int isSelect = -1;
            for (int i = 0; i < jsonArray.size(); i++) {
                String oJid = jsonArray.get(i).getAsJsonObject().get("jid").getAsString();
                //System.out.println("存在用户的id为" + oJid);
                if (oJid.equals(jid)) {
                    isSelect = i;
                }
            }
            if (isSelect == -1) {
                jsonArray.add(jsonObject);
            } else {
                jsonArray.remove(isSelect);
                jsonArray.add(jsonObject);
            }
        }
        FileOutputStream fileOutputStream = new FileOutputStream(path);
        FileChannel fileChannel = fileOutputStream.getChannel();
        FileLock fileLock = fileChannel.tryLock();
        fileOutputStream.write(jsonArray.toString().getBytes(StandardCharsets.UTF_8));
        fileLock.release();
        fileOutputStream.close();
    }

    public String read(String path) throws IOException {
        File dir = new File(path);
        // 一、检查放置文件的文件夹路径是否存在，不存在则创建
        if (!dir.exists()) {
            //dir.mkdirs();// mkdirs创建多级目录
            System.out.println("文件不存在");
            return null;
        }
        BufferedReader fileInputStream = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
        StringBuilder builder = new StringBuilder();
        int ch;
        new String();
        while ((ch = fileInputStream.read()) != -1) {
            builder.append((char) ch);
        }
        String result = builder.toString();
        //result = result.substring(1, result.length() - 1);
        fileInputStream.close();
        return result;
    }
}
