package com.loror.lororUtil.image;

import android.graphics.Bitmap;

public class Frame {
    public Bitmap image;
    public int delay;
    public Frame nextFrame = null;

    public Frame(Bitmap im, int del, int widthLimit) {
        if (im.getWidth() > widthLimit) {
            image = BitmapUtil.compessBitmap(im, widthLimit, false);
        } else {
            image = im;
        }
        delay = del;
    }
}
