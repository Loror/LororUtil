package com.loror.lororUtil.image;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import java.io.File;
import java.io.FileInputStream;
import java.util.Locale;

public class ReadImageUtil {

    public ReadImageResult getReadImageResult(String path, int widthLimit, boolean mutiCach) {
        return getReadImageResult(path, null, widthLimit, mutiCach);
    }

    public ReadImageResult getReadImageResult(byte[] resource, int widthLimit, boolean mutiCach) {
        return getReadImageResult(null, resource, widthLimit, mutiCach);
    }

    private ReadImageResult getReadImageResult(String path, byte[] resource, int widthLimit, boolean mutiCach) {
        ReadImageResult result = new ReadImageResult();
        if (path != null && path.toLowerCase(Locale.CHINA).endsWith(".mp4")) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                retriever.setDataSource(path);
                Bitmap bitmap = retriever.getFrameAtTime();
                result.setErrorCode(0);
                result.addFrame(new Frame(bitmap, 0, widthLimit));
                return result;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } finally {
                retriever.release();
            }
        }
        String type = mutiCach ? (resource != null ? BitmapUtil.getBitmapType(resource) : BitmapUtil.getBitmapType(path)) : null;
        if (type != null && type.contains("gif")) {
            try {
                GifDecoder decoder = resource != null ? new GifDecoder(resource) : new GifDecoder(new FileInputStream(new File(path)));
                decoder.setWidthLimit(widthLimit);
                decoder.decode();
                if (decoder.getStatus() == GifDecoder.STATUS_FINISH) {
                    for (int i = 0; i < decoder.getFrameCount(); i++) {
                        result.addFrame(decoder.getFrame(i));
                    }
                } else {
                    addFirstFrame(result, path, resource, widthLimit);
                }
            } catch (Throwable e) {
                System.gc();
                result.setErrorCode(e instanceof OutOfMemoryError ? 3 : 2);
                result.setThrowable(e);
                addFirstFrame(result, path, resource, widthLimit);
            }
        } else {
            addFirstFrame(result, path, resource, widthLimit);
        }
        result.setPath(path);
        return result;
    }

    private void addFirstFrame(ReadImageResult result, String path, byte[] resource, int widthLimit) {
        try {
            Bitmap bitmap = resource != null ?
                    BitmapUtil.compessBitmap(resource, widthLimit) :
                    BitmapUtil.compessBitmap(path, widthLimit);
            if (bitmap != null) {
                result.setErrorCode(0);
                result.addFrame(new Frame(bitmap, 0, widthLimit));
            } else {
                result.setErrorCode(2);
            }
        } catch (OutOfMemoryError e) {
            System.gc();
            result.setErrorCode(3);
            result.setThrowable(e);
        }
    }
}
