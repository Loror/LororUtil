package com.loror.lororUtil.image;

import android.graphics.Bitmap;

public class Frame {
	public Bitmap image;
	public int delay;
	public Frame nextFrame = null;

	public Frame(Bitmap im, int del, int widthLimit) {
		image = BitmapUtil.compessBitmap(im, widthLimit, false);
		delay = del;
	}
}
