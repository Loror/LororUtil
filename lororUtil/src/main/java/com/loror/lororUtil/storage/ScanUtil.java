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

    private final Handler handler = ObjectPool.getInstance().getHandler();

    public interface ScanCallBack {
        void findOne(String path, int find, int scanned);

        void finish();
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

    private int total = 0;
    private int count = 0;

    /**
     * 扫描sd卡，传入参数，1，起点目录，2，需扫描后缀数组，3，回调接口
     */
    @Deprecated
    public void scanSDCard(final File rootFileDir, final String[] suffixs, final ScanCallBack callBack) {
        new Thread() {
            public void run() {
                total = 0;
                count = 0;
                initPaths(rootFileDir.listFiles(), suffixs, callBack);
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
    private void initPaths(final File[] files, String[] suffixes, final ScanCallBack callBack) {
        if (files != null && files.length > 0) {
            for (int i = 0, length = files.length; i < length; i++) {
                if (files[i].isFile()) {
                    final int index = i;
                    total++;
                    if (contains(files[i].getName(), suffixes)) {
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
                            initPaths(files[i].listFiles(), suffixes, callBack);
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
        String upperName = name.toUpperCase(Locale.CHINA);
        for (String suffix : suffixs) {
            if (upperName.endsWith(suffix.toUpperCase(Locale.CHINA))) {
                return true;
            }
        }
        return false;
    }
}
