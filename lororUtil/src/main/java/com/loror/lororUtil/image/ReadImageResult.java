package com.loror.lororUtil.image;

import android.graphics.Bitmap;

public class ReadImageResult {
	private Bitmap bitmap;
	private String path;

	public Bitmap getBitmap() {
		return bitmap;
	}

	public String getPath() {
		return path;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
