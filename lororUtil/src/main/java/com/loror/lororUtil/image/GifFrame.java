package com.loror.lororUtil.image;

import android.graphics.Bitmap;

public class GifFrame {
	public Bitmap image;
	public int delay;
	public GifFrame nextFrame = null;

	public GifFrame(Bitmap im, int del, int widthLimit) {
		image = BitmapUtil.compessBitmap(im, widthLimit, false);
		delay = del;
	}
}
