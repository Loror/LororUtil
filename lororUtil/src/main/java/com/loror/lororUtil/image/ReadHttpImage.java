package com.loror.lororUtil.image;

import com.loror.lororUtil.http.HttpClient;

/**
 * 读取SD卡图片
 */
public class ReadHttpImage extends ReadImageUtil implements ReadImage {

    private ReadHttpImage() {

    }

    private static class SingletonFactory {
        private static final ReadHttpImage instance = new ReadHttpImage();
    }

    public static ReadHttpImage getInstance() {
        return SingletonFactory.instance;
    }

    @Override
    public ReadImageResult readImage(String path, int widthLimit, boolean mutiCach) {
        if (isNetMp4(path)) {
            return getNetMp4ReadImageResult(path, widthLimit);
        }
        HttpClient client = new HttpClient();
        return getReadImageResult(client.get(path, null).result, widthLimit, mutiCach);
    }

}
