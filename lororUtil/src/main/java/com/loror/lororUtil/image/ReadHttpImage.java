package com.loror.lororUtil.image;

import android.graphics.Bitmap;

import com.loror.lororUtil.http.HttpClient;

import java.io.File;
import java.io.FileInputStream;

/**
 * 读取SD卡图片
 */
public class ReadHttpImage extends ReadImageUtil implements ReadImage {

    private ReadHttpImage() {
        // TODO Auto-generated constructor stub
    }

    private static class SingletonFactory {
        private static ReadHttpImage instance = new ReadHttpImage();
    }

    public static ReadHttpImage getInstance() {
        return SingletonFactory.instance;
    }

    @Override
    public ReadImageResult readImage(String path, int widthLimit, boolean mutiCach) {
        HttpClient client = new HttpClient();
        return getReadImageResult(client.get(path, null).result, widthLimit, mutiCach);
    }

}
