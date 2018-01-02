package com.loror.lororUtil.storage;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class MediaUtil {
	/**
	 * 回调接口
	 */
	public interface MediaUtilCallBack {
		void readOne(String path);

		void finish();
	}

	/**
	 * 获取SD卡所有图片
	 */
	public static void getLocalImagePaths(Context context, MediaUtilCallBack callBack) {
		Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		ContentResolver mContentResolver = context.getContentResolver();

		// 只查询jpeg,png,gif的图片
		Cursor mCursor = mContentResolver.query(mImageUri, null,
				MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=? or "
						+ MediaStore.Images.Media.MIME_TYPE + "=?",
				new String[] { "image/jpeg", "image/png", "image/gif" }, MediaStore.Images.Media.DATE_MODIFIED);
		while (mCursor.moveToNext()) {
			// 获取图片的路径
			String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
			callBack.readOne(path);
		}
		callBack.finish();
	}

}
