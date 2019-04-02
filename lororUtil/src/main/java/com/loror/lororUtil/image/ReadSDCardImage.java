package com.loror.lororUtil.image;

/**
 * 读取SD卡图片
 */
public class ReadSDCardImage extends ReadImageUtil implements ReadImage {

    private ReadSDCardImage() {

    }

    private static class SingletonFactory {
        private static ReadSDCardImage instance = new ReadSDCardImage();
    }

    public static ReadSDCardImage getInstance() {
        return SingletonFactory.instance;
    }

    @Override
    public ReadImageResult readImage(String path, int widthLimit, boolean mutiCach) {
        return getReadImageResult(path, widthLimit, mutiCach);
    }

}
