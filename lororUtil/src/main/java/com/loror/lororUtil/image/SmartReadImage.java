package com.loror.lororUtil.image;

import java.io.File;
import java.io.FileInputStream;

import android.content.Context;
import android.graphics.Bitmap;

public class SmartReadImage implements ReadImage, Cloneable {

    private String targetFilePath;
    private Context context;

    private SmartReadImage() {
        // TODO Auto-generated constructor stub
    }

    private static class SingletonFactory {
        private static SmartReadImage instance = new SmartReadImage();
    }

    public static SmartReadImage getInstance(Context context, String targetFilePath) {
        SmartReadImage smartReadImage = null;
        try {
            smartReadImage = (SmartReadImage) SingletonFactory.instance.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            smartReadImage = new SmartReadImage();
        }
        smartReadImage.context = context;
        smartReadImage.targetFilePath = targetFilePath;
        return smartReadImage;
    }

    @Override
    public ReadImageResult readImage(String url, int widthLimit, boolean mitiCach) {
        File f;
        ReadImageResult result = new ReadImageResult();
        if (url.startsWith("http")) {
            f = new File(targetFilePath);
            if (!ImageDownloader.download(context, url, f.getAbsolutePath(), false, false)) {
                result.setErrorCode(1);//网络下载失败，标注errorCode为1
                result.setPath(f.getAbsolutePath());
                return result;
            }
        } else {
            f = new File(url);
        }
        String type = mitiCach ? BitmapUtil.getBitmapType(f.getAbsolutePath()) : null;
        if (type != null && type.contains("gif")) {
            try {
                GifDecoder decoder = new GifDecoder(new FileInputStream(f));
                decoder.setWidthLimit(widthLimit);
                decoder.decode();
                if (decoder.getStatus() == GifDecoder.STATUS_FINISH) {
                    for (int i = 0; i < decoder.getFrameCount(); i++) {
                        result.addFrame(decoder.getFrame(i));
                    }
                } else {
                    Bitmap firstFrame = getFirstFrame(f, url, widthLimit);
                    if (firstFrame != null) {
                        result.addFrame(new Frame(firstFrame, 0, widthLimit));
                    } else {
                        result.setErrorCode(2);
                    }
                }
            } catch (Throwable e) {
                System.gc();
                result.setErrorCode(e instanceof OutOfMemoryError ? 3 : 2);
                result.setThrowable(e);
                result.addFrame(new Frame(getFirstFrame(f, url, widthLimit), 0, widthLimit));
            }
        } else {
            Bitmap firstFrame = getFirstFrame(f, url, widthLimit);
            if (firstFrame != null) {
                result.addFrame(new Frame(firstFrame, 0, widthLimit));
            } else {
                result.setErrorCode(2);
            }
        }
        result.setPath(f.getAbsolutePath());
        return result;
    }

    private Bitmap getFirstFrame(File f, String url, int widthLimit) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapUtil.compessBitmap(f.getAbsolutePath(), widthLimit);
            if (bitmap == null && url.startsWith("http")) {
                f.delete();//无法解析的图片，删除
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            System.gc();
        }
        return bitmap;
    }

}
