package com.loror.lororUtil.image;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import com.loror.lororUtil.http.HttpsClient;
import com.loror.lororUtil.sql.Model;
import com.loror.lororUtil.sql.SQLiteUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class ImageDownloader {

    private static ImageDownloaderConfig imageDownloaderConfig;

    public interface ImageDownloaderConfig {
        void beforeLoad(String url, HttpURLConnection connection);

        void whenLoading(String url, float progress, long length);
    }

    /**
     * 设置请求预处理
     */
    public static void setImageDownloaderConfig(ImageDownloaderConfig imageDownloaderConfig) {
        ImageDownloader.imageDownloaderConfig = imageDownloaderConfig;
    }

    /**
     * 删除sql缓存，files为保留路径
     */
    public static void tryClearSqlCache(Context context, File[] files) {
        if (files == null || files.length == 0) {
            return;
        }
        SQLiteUtil sqLiteUtil = new SQLiteUtil(context, "imageCompare");
        sqLiteUtil.createTableIfNotExists(Compare.class);
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
            Model<Compare> model = sqLiteUtil.model(Compare.class);
            for (int i = 0; i < removes.size(); i++) {
                model.whereOr("path", removes.get(i));
            }
            model.delete();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteUtil.close();
        }
    }

    /**
     * 删除所有sql缓存
     */
    public static void tryClearAllSqlCache(Context context) {
        SQLiteUtil sqLiteUtil = new SQLiteUtil(context, "imageCompare");
        sqLiteUtil.createTableIfNotExists(Compare.class);
        List<Compare> compares = sqLiteUtil.getAll(Compare.class);
        sqLiteUtil.deleteAll(Compare.class);
        sqLiteUtil.close();
        for (Compare compare : compares) {
            try {
                File file = new File(compare.path);
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将网络图片存储到sd卡
     */
    public static boolean download(Context context, final String urlStr, String path, boolean cover, boolean checkNet) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return false;
        }
        SQLiteUtil sqLiteUtil = new SQLiteUtil(context, "imageCompare");
        sqLiteUtil.createTableIfNotExists(Compare.class);
        try {
            File file = new File(path);
            Compare compare = sqLiteUtil.model(Compare.class).where("url", urlStr).first();
            if (!checkNet && file.exists() && !cover && compare != null && compare.length == file.length()) {
                return true;
            }
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setDoInput(true);
//            conn.setRequestProperty("Accept-Encoding", "identity");
            HttpsClient.Config.httpsConfig(conn);
            if (imageDownloaderConfig != null) {
                imageDownloaderConfig.beforeLoad(urlStr, conn);
            }
            long length = 0;
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    length = conn.getContentLengthLong();
                } else {
                    length = conn.getContentLength();
                }
            } catch (Throwable e) {
                length = conn.getContentLength();
            }
            if (compare == null) {
                compare = new Compare();
            }
            compare.length = length;
            compare.url = urlStr;
            compare.path = file.getAbsolutePath();
            if (file.exists() && !cover && compare.length == file.length()) {
                file.setLastModified(System.currentTimeMillis());
                conn.disconnect();
                return true;
            }
            //适配部分机型需手动创建文件
            if (!file.exists()) {
                file.createNewFile();
            }
            InputStream is = conn.getInputStream();
            String contentEncoding = conn.getContentEncoding();
            if ("gzip".equalsIgnoreCase(contentEncoding)) {
                is = new GZIPInputStream(is);
            }
            FileOutputStream fos = new FileOutputStream(file);
            byte[] out = new byte[1024 * 10];
            long last = System.currentTimeMillis(), transed = 0;
            int total = 0;
            while ((total = is.read(out)) != -1) {
                fos.write(out, 0, total);
                fos.flush();
                transed += total;
                if (imageDownloaderConfig != null) {
                    long now = System.currentTimeMillis();
                    final long timeGo = now - last;
                    if (timeGo > 30) {
                        final float progress = (float) (transed * 1.0 / length * 100);
                        imageDownloaderConfig.whenLoading(urlStr, progress, length);
                        last = now;
                    }
                }
            }
            is.close();
            fos.close();
            if ("gzip".equalsIgnoreCase(contentEncoding)) {
                compare.length = file.length();
            }
            if (compare.id == 0) {
                sqLiteUtil.insert(compare);
            } else {
                sqLiteUtil.updateById(compare);
            }
            if (imageDownloaderConfig != null) {
                imageDownloaderConfig.whenLoading(urlStr, 100f, length);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (imageDownloaderConfig != null) {
                imageDownloaderConfig.whenLoading(urlStr, -1f, -1);
            }
        } finally {
            sqLiteUtil.close();
        }
        return false;
    }
}
