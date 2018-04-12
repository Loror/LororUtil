package com.loror.lororUtil.storage;

import java.io.File;
import java.util.Locale;

import com.loror.lororUtil.flyweight.ObjectPool;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;

public class ScanUtil {
    private Handler handler;

    public interface ScanCallBack {
        void findOne(String path, int find, int scaned);

        void finish();
    }

    public ScanUtil() {
        handler = ObjectPool.getInstance().getHandler();
    }

    /**
     * 获取SD卡所有图片
     */
    public void scanProviderImages(final Context context, final ScanCallBack callBack) {
        new Thread() {
            @Override
            public void run() {
                int count = 0;

                Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver mContentResolver = context.getContentResolver();

                // 只查询jpeg,png,gif的图片
                Cursor mCursor = mContentResolver.query(mImageUri, null,
                        MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=? or "
                                + MediaStore.Images.Media.MIME_TYPE + "=?",
                        new String[]{"image/jpeg", "image/png", "image/gif"}, MediaStore.Images.Media.DATE_MODIFIED);
                if (mCursor != null) {
                    while (mCursor.moveToNext()) {
                        // 获取图片的路径
                        final String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                        count++;
                        final int finalCount = count;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callBack.findOne(path, finalCount, 0);
                            }
                        });
                    }
                    mCursor.close();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callBack.finish();
                    }
                });
            }
        }.start();

    }

    /**
     * 扫描sd卡，传入参数，1，起点目录，2，需扫描后缀数组，3，回调接口
     */
    public void scanSDCard(final File rootFileDir, final String[] suffixs, final ScanCallBack callBack) {
        new Thread() {
            public void run() {
                inintPaths(rootFileDir.listFiles(), suffixs, callBack);
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        callBack.finish();
                    }
                });
            }
        }.start();
    }

    /**
     * 递归扫描
     */
    private void inintPaths(final File[] files, String[] suffixs, final ScanCallBack callBack) {
        int total = 0;
        int count = 0;
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    final int index = i;
                    total++;
                    if (contains(files[i].getName(), suffixs)) {
                        count++;
                        final int finalCount = count;
                        final int finalTotal = total;
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                callBack.findOne(files[index].getAbsolutePath(), finalCount, finalTotal);
                            }
                        });
                    }
                } else {
                    try {
                        String get = files[i].getName();
                        if (!get.startsWith(".")) {
                            inintPaths(files[i].listFiles(), suffixs, callBack);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 检查后缀
     */
    private boolean contains(String name, String[] suffixs) {
        String uperName = name.toUpperCase(Locale.CHINA);
        for (int i = 0; i < suffixs.length; i++) {
            if (uperName.endsWith(suffixs[i].toUpperCase(Locale.CHINA))) {
                return true;
            }
        }
        return false;
    }
}
