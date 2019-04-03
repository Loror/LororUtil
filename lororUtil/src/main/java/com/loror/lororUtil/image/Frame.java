package com.loror.lororUtil.image;

import android.graphics.Bitmap;

public class Frame {
    public Bitmap image;
    public int delay;
    public Frame nextFrame = null;

    public Frame(Bitmap bitmap, int delay, int widthLimit) {
        if (bitmap != null) {
            if (bitmap.getWidth() > widthLimit) {
                if (widthLimit <= 0) {
                    widthLimit = EfficientImageUtil.DEFAULT_WIDTH;
                }
                image = BitmapUtil.compessBitmap(bitmap, widthLimit, false);
            } else {
                image = bitmap;
            }
        }
        this.delay = delay;
    }
}
