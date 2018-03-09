package com.loror.lororUtil.image;

import java.io.File;
import java.io.FileInputStream;

import android.content.Context;
import android.graphics.Bitmap;

public class SmartReadImage implements ReadImage, Cloneable {

	private String targetFilePath;
	private Context context;

	private SmartReadImage() {
		// TODO Auto-generated constructor stub
	}

	private static class SingletonFactory {
		private static SmartReadImage instance = new SmartReadImage();
	}

	public static SmartReadImage getInstance(Context context, String targetFilePath) {
		try {
			SmartReadImage smartReadImage = (SmartReadImage) SingletonFactory.instance.clone();
			smartReadImage.context = context;
			smartReadImage.targetFilePath = targetFilePath;
			return smartReadImage;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public ReadImageResult readImage(String url, int widthLimit, boolean mitiCach) {
		File f;
		if (url.startsWith("http")) {
			f = new File(targetFilePath);
			ImageDownloader.download(context, url, f.getAbsolutePath(), false, false);
		} else {
			f = new File(url);
		}
		String type = mitiCach ? BitmapUtil.getBitmapType(f.getAbsolutePath()) : null;
		ReadImageResult result = new ReadImageResult();
		if (type != null && type.contains("gif")) {
			try {
				GifDecoder decoder = new GifDecoder(new FileInputStream(f));
				decoder.setWidthLimit(widthLimit);
				decoder.decode();
				if (decoder.getStatus() == GifDecoder.STATUS_FINISH) {
					for (int i = 0; i < decoder.getFrameCount(); i++) {
						result.addFrame(decoder.getFrame(i));
					}
				} else {
					result.addFrame(new Frame(getFirstFrame(f, url, widthLimit), 0, widthLimit));
				}
			} catch (Throwable e) {
				System.gc();
				result.setErrorCode(e instanceof OutOfMemoryError ? 1 : 2);
				result.setThrowable(e);
				result.addFrame(new Frame(getFirstFrame(f, url, widthLimit), 0, widthLimit));
			}
		} else {
			result.addFrame(new Frame(getFirstFrame(f, url, widthLimit), 0, widthLimit));
		}
		result.setPath(f.getAbsolutePath());
		return result;
	}

	private Bitmap getFirstFrame(File f, String url, int widthLimit) {
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
		return bitmap;
	}

}
