package com.loror.lororUtil.image;

import java.io.File;
import android.content.Context;

public class SmartReadImage extends ReadImageUtil implements ReadImage, Cloneable {

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
    public ReadImageResult readImage(String url, int widthLimit, boolean mutiCach) {
        File f;
        ReadImageResult result;
        if (url.startsWith("http")) {
            f = new File(targetFilePath);
            if (!ImageDownloader.download(context, url, f.getAbsolutePath(), false, false)) {
                result = new ReadImageResult();
                result.setErrorCode(1);//网络下载失败，标注errorCode为1
                result.setPath(f.getAbsolutePath());
                return result;
            }
        } else {
            f = new File(url);
        }
        result = getReadImageResult(f.getAbsolutePath(), widthLimit, mutiCach);
        if (result.getErrorCode() == 2 && url.startsWith("http")) {
            f.delete();//无法解析的图片，删除
        }
        return result;
    }

}
