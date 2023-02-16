package com.example.test.tools;

import android.app.Activity;
import android.util.Log;

import com.baidu.ocr.sdk.*;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.GeneralBasicParams;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.WordSimple;
import com.google.gson.Gson;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class ORCApplication {
    private String answerPictureToSting;

    public String recWords(Activity activity, String filePath) {
        OCR.getInstance(activity.getApplicationContext()).initAccessToken(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                // 调用成功，返回AccessToken对象
                String token = result.getAccessToken();
            }

            @Override
            public void onError(OCRError error) {
                // 调用失败，返回OCRError子类SDKError对象
            }
        }, activity.getApplicationContext());
        GeneralBasicParams param = new GeneralBasicParams();
        param.setDetectDirection(true);
        param.setImageFile(new File(filePath));
        Log.e("TAG", "recWords: " + filePath);
        StringBuilder sb = new StringBuilder();
        // 调用通用文字识别服务
        OCR.getInstance(activity.getApplicationContext()).recognizeGeneralBasic(param, new OnResultListener<GeneralResult>() {
            @Override
            public void onResult(GeneralResult result) {
                // 调用成功，返回GeneralResult对象
                for (WordSimple wordSimple : result.getWordList()) {
                    // wordSimple不包含位置信息
                    WordSimple word = wordSimple;
                    sb.append(word.getWords());
                }
                answerPictureToSting = sb.toString();
                Log.e("String", "onResult: " + answerPictureToSting);
                // json格式返回字符串
                //listener.onResult(result.getJsonRes());
                Log.e("Json", "onResult: " + result.getJsonRes());
            }

            @Override
            public void onError(OCRError ocrError) {

            }
        });
        return answerPictureToSting;
    }

}

