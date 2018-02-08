package com.loror.lororUtil.image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.loror.lororUtil.sql.ConditionBuilder;
import com.loror.lororUtil.sql.SQLiteUtil;

import android.content.Context;
import android.os.Environment;

public class ImageDownloader {
	private static final Lock lock = new ReentrantLock();

	/**
	 * 将网络图片存储到sd卡
	 */
	public static boolean download(Context context, String urlStr, String path, boolean cover, boolean checkNet) {
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return false;
		}
		SQLiteUtil<Compare> sqLiteUtil = new SQLiteUtil<>(context, "image_compare", Compare.class, 1);
		try {
			File file = new File(path);
			Compare compare = sqLiteUtil.getFirstByCondition(ConditionBuilder.builder().addCondition("url", urlStr));
			if (!checkNet && file.exists() && !cover && compare != null && compare.length == file.length()) {
				return true;
			}
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(10000);
			conn.setDoInput(true);
			long length = conn.getContentLength();
			lock.lock();
			sqLiteUtil.deleteByCondition(ConditionBuilder.builder().addCondition("url", urlStr));
			compare = new Compare();
			compare.length = length;
			compare.url = urlStr;
			sqLiteUtil.insert(compare);
			lock.unlock();
			if (file.exists() && !cover && compare.length == file.length()) {
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
