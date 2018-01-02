package com.loror.lororUtil.image;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;

public class SmartReadImage implements ReadImage {

	private String targetFilePath;
	private Context context;

	public SmartReadImage(Context context, String targetFilePath) {
		this.context = context;
		this.targetFilePath = targetFilePath;
	}

	@Override
	public ReadImageResult readImage(String url, int widthLimit) {
		File f;
		if (url.startsWith("http")) {
			f = new File(targetFilePath);
			ImageDownloader.download(context, url, f.getAbsolutePath(), false, false);
		} else {
			f = new File(url);
		}
		Bitmap bitmap = null;
		try {
			bitmap = BitmapUtil.compessBitmap(f.getAbsolutePath(), widthLimit);
			if (bitmap == null && url.startsWith("http")) {
				f.delete();
			}
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			System.gc();
		}
		ReadImageResult result = new ReadImageResult();
		result.setBitmap(bitmap);
		result.setPath(f.getAbsolutePath());
		return result;
	}

}
