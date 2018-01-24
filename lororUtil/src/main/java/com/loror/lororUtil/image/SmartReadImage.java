package com.loror.lororUtil.image;

import java.io.File;
import java.io.FileInputStream;

import android.content.Context;
import android.graphics.Bitmap;

public class SmartReadImage implements ReadImage {

	private String targetFilePath;
	private Context context;

	public SmartReadImage(Context context, String targetFilePath, boolean mitiCach) {
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
		String type = BitmapUtil.getBitmapType(f.getPath());
		ReadImageResult result = new ReadImageResult();
		if (type != null && type.contains("gif")) {
			GifDecoder decoder;
			try {
				decoder = new GifDecoder(new FileInputStream(f));
				decoder.setWidthLimit(widthLimit);
				decoder.decode();
				if (decoder.getStatus() == GifDecoder.STATUS_FINISH) {
					for (int i = 0; i < decoder.getFrameCount(); i++) {
						result.addFrame(decoder.getFrame(i));
					}
				} else {
					result.addFrame(new Frame(getFirstFrame(f, url, widthLimit), 0, widthLimit));
				}
			} catch (Exception e) {
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
