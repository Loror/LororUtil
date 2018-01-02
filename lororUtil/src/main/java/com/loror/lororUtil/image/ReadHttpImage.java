package com.loror.lororUtil.image;

import android.graphics.Bitmap;

/**
 * 读取网络图片
 */
public class ReadHttpImage implements ReadImage {
	private ReadHttpImage() {
		// TODO Auto-generated constructor stub
	}

	private static class SingletonFactory {
		private static ReadHttpImage instance = new ReadHttpImage();
	}

	public static ReadHttpImage getInstance() {
		return SingletonFactory.instance;
	}

	@Override
	public ReadImageResult readImage(String path, int widthLimit) {
		Bitmap bitmap = BitmapUtil.getBitmapByUrl(path);
		try {
			bitmap = BitmapUtil.compessBitmap(bitmap, widthLimit, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ReadImageResult result = new ReadImageResult();
		result.setBitmap(bitmap);
		return result;
	}

}
