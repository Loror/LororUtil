package com.loror.lororUtil.image;

import android.graphics.Bitmap;

public class Frame {
    public Bitmap image;
    public int delay;
    public Frame nextFrame = null;

    public Frame(Bitmap bitmap, int delay, int widthLimit) {
        if (bitmap != null) {
            if (bitmap.getWidth() > widthLimit) {
                image = BitmapUtil.compessBitmap(bitmap, widthLimit <= 0 ? 720 : widthLimit, false);
            } else {
                image = bitmap;
            }
        }
        this.delay = delay;
    }
}
