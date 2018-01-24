package com.loror.lororUtil.image;

import android.graphics.Bitmap;

/**
 * 读取SD卡图片
 */
public class ReadSDCardImage implements ReadImage {
	private ReadSDCardImage() {
		// TODO Auto-generated constructor stub
	}

	private static class SingletonFactory {
		private static ReadSDCardImage instance = new ReadSDCardImage();
	}

	public static ReadSDCardImage getInstance() {
		return SingletonFactory.instance;
	}

	@Override
	public ReadImageResult readImage(String path, int widthLimit) {
		Bitmap bitmap = BitmapUtil.compessBitmap(path, widthLimit);
		bitmap = BitmapUtil.compessBitmap(bitmap, widthLimit, true);
		ReadImageResult result = new ReadImageResult();
		result.addFrame(new Frame(bitmap, 0, widthLimit));
		result.setPath(path);
		return result;
	}

}
