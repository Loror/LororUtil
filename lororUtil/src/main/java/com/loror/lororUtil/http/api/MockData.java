package com.loror.lororUtil.http.api;

import android.content.Context;
import android.content.res.AssetManager;

import com.loror.lororUtil.annotation.MocKType;
import com.loror.lororUtil.http.Responce;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MockData {

    private final ApiRequest apiRequest;
    private final ApiTask apiTask;
    private Responce responce;

    private static Context context;

    /**
     * 初始化
     */
    public static void init(Context context) {
        MockData.context = context.getApplicationContext();
    }

    protected MockData(ApiRequest apiRequest, ApiTask apiTask) {
        this.apiRequest = apiRequest;
        this.apiTask = apiTask;
    }

    /**
     * 获取Responce
     */
    public Responce getResponce() {
        return responce;
    }

    /**
     * 获取结果
     */
    public String getResult() {
        int type = apiRequest.mockType;
        if (apiRequest.mockType == MocKType.AUTO) {
            if (apiRequest.mockData.trim().startsWith("{") || apiRequest.mockData.trim().startsWith("[")) {
                type = MocKType.DATA;
            } else if (apiRequest.mockData.startsWith("http") || apiRequest.mockData.startsWith("https:")) {
                type = MocKType.NET;
            } else {
                type = MocKType.ASSERT;
            }
        }

        String result = null;
        if (type == MocKType.ASSERT) {
            result = readAssetsFile(apiRequest.mockData);
        } else if (type == MocKType.NET) {
            if (apiRequest.mockData.startsWith("http://") || apiRequest.mockData.startsWith("https://")) {
                apiRequest.setAnoBaseUrl(null);
                apiRequest.setBaseUrl(null);
            }
            apiRequest.setUrl(apiRequest.mockData);
            responce = apiTask.connect();
        } else if (type == MocKType.DATA) {
            result = apiRequest.mockData;
        }
        return result;
    }

    /**
     * 读取assets文件
     */
    private String readAssetsFile(String fileName) {
        if (context == null) {
            return "need call init before";
        }
        AssetManager manager = context.getResources().getAssets();
        try {
            InputStream inputStream = manager.open(fileName);
            InputStreamReader isr = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String length;
            while ((length = br.readLine()) != null) {
                sb.append(length).append("\n");
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            //关流
            br.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
