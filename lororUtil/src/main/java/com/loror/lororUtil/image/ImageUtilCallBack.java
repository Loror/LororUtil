package com.loror.lororUtil.image;

import android.graphics.Bitmap;
import android.widget.ImageView;

public interface ImageUtilCallBack {
	void onStart(ImageView imageView);

	void onLoadCach(ImageView imageView, Bitmap bitmap);

	void onFinish(ImageView imageView, ReadImageResult bitmap);

	void onFailed(ImageView imageView, String path);
}
