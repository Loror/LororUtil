package com.loror.lororUtil.image;

import android.graphics.Bitmap;

public interface BitmapTarget extends Target<Bitmap> {

    @Override
    void target(Bitmap bitmap);
}
