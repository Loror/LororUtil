package com.loror.lororUtil.image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.loror.lororUtil.sql.ConditionBuilder;
import com.loror.lororUtil.sql.SQLiteUtil;

import android.content.Context;
import android.os.Environment;

public class ImageDownloader {

    /**
     * 删除sql缓存，files为保留路径
     */
    public static void tryClearSqlCach(Context context, File[] files) {
        if (files == null || files.length == 0) {
            return;
        }
        SQLiteUtil sqLiteUtil = new SQLiteUtil(context, "image_compare", 2);
        try {
            List<String> paths = new ArrayList<>();
            for (int i = 0; i < files.length; i++) {
                paths.add(files[i].getAbsolutePath());
            }
            List<Compare> compares = sqLiteUtil.getAll(Compare.class);
            List<Compare> removes = new ArrayList<>();
            removes.addAll(compares);
            for (int i = 0; i < compares.size(); i++) {
                for (int j = 0; j < paths.size(); j++) {
                    String path = paths.get(j);
                    if (path.equals(compares.get(i).path)) {
                        removes.remove(compares.get(i));
                        paths.remove(path);
                        break;
                    }
                }
            }
            ConditionBuilder conditionBuilder = ConditionBuilder.builder();
            for (int i = 0; i < removes.size(); i++) {
                conditionBuilder.addOrCondition("path", removes.get(i));
            }
            sqLiteUtil.deleteByCondition(conditionBuilder, Compare.class);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteUtil.close();
        }
    }

    /**
     * 删除所有sql缓存
     */
    public static void tryClearAllSqlCach(Context context) {
        SQLiteUtil sqLiteUtil = new SQLiteUtil(context, "imageCompare");
        sqLiteUtil.deleteAll(Compare.class);
        sqLiteUtil.close();
    }

    /**
     * 将网络图片存储到sd卡
     */
    public static boolean download(Context context, String urlStr, String path, boolean cover, boolean checkNet) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return false;
        }
        SQLiteUtil sqLiteUtil = new SQLiteUtil(context, "imageCompare");
        sqLiteUtil.createTableIfNotExists(Compare.class);
        try {
            File file = new File(path);
            Compare compare = sqLiteUtil.getFirstByCondition(ConditionBuilder.builder().addCondition("url", urlStr),
                    Compare.class);
            if (!checkNet && file.exists() && !cover && compare != null && compare.length == file.length()) {
                return true;
            }
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setDoInput(true);
            long length = conn.getContentLength();
            if (compare == null) {
                compare = new Compare();
            }
            compare.length = length;
            compare.url = urlStr;
            compare.path = file.getAbsolutePath();
            if (compare.id == 0) {
                sqLiteUtil.insert(compare);
            } else {
                sqLiteUtil.updateById(compare);
            }
            if (file.exists() && !cover && compare.length == file.length()) {
                file.setLastModified(System.currentTimeMillis());
                conn.disconnect();
                return true;
            }
            InputStream is = conn.getInputStream();
            FileOutputStream fos = new FileOutputStream(file);
            byte[] out = new byte[2048];
            int total = 0;
            while ((total = is.read(out)) != -1) {
                fos.write(out, 0, total);
                fos.flush();
            }
            is.close();
            fos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteUtil.close();
        }
        return false;
    }
}
