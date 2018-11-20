package com.loror.lororUtil.image;

import android.content.Context;
import android.graphics.Bitmap;

public interface BitmapConverter {
    Bitmap convert(Context context, Bitmap original);
}
