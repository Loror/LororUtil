package com.loror.lororUtil.image;

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileInputStream;

/**
 * 读取SD卡图片
 */
public class ReadSDCardImage implements ReadImage {

    private ReadSDCardImage() {
        // TODO Auto-generated constructor stub
    }

    private static class SingletonFactory {
        private static ReadSDCardImage instance = new ReadSDCardImage();
    }

    public static ReadSDCardImage getInstance() {
        return SingletonFactory.instance;
    }

    @Override
    public ReadImageResult readImage(String path, int widthLimit, boolean mitiCach) {
        String type = mitiCach ? BitmapUtil.getBitmapType(path) : null;
        ReadImageResult result = new ReadImageResult();
        if (type != null && type.contains("gif")) {
            try {
                GifDecoder decoder = new GifDecoder(new FileInputStream(new File(path)));
                decoder.setWidthLimit(widthLimit);
                decoder.decode();
                if (decoder.getStatus() == GifDecoder.STATUS_FINISH) {
                    for (int i = 0; i < decoder.getFrameCount(); i++) {
                        result.addFrame(decoder.getFrame(i));
                    }
                } else {
                    result.addFrame(new Frame(getFirstFrame(path, widthLimit), 0, widthLimit));
                }
            } catch (Throwable e) {
                System.gc();
                result.setErrorCode(e instanceof OutOfMemoryError ? 3 : 2);
                result.setThrowable(e);
                result.addFrame(new Frame(getFirstFrame(path, widthLimit), 0, widthLimit));
            }
        } else {
            result.addFrame(new Frame(getFirstFrame(path, widthLimit), 0, widthLimit));
        }
        result.setPath(path);
        return result;
    }

    private Bitmap getFirstFrame(String path, int widthLimit) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapUtil.compessBitmap(path, widthLimit);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            System.gc();
        }
        return bitmap;
    }
}
