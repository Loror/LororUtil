package com.loror.lororUtil.image;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;

/**
 * 读取SD卡视频缩略图
 */
public class ReadSDCardVideo implements ReadImage {
    private ReadSDCardVideo() {
        // TODO Auto-generated constructor stub
    }

    private static class SingletonFactory {
        private static ReadSDCardVideo instance = new ReadSDCardVideo();
    }

    public static ReadSDCardVideo getInstance() {
        return SingletonFactory.instance;
    }

    @Override
    public ReadImageResult readImage(String path, int widthLimit, boolean mutiCache) {
        Bitmap bitmap = BitmapUtil.compessBitmap(ThumbnailUtils.createVideoThumbnail(path, 320), widthLimit, true);
        ReadImageResult result = new ReadImageResult();
        result.addFrame(new Frame(bitmap, 0, widthLimit));
        result.setPath(path);
        return result;
    }

}
