package com.loror.lororUtil.image;

import android.widget.ImageView;

public interface ImageUtilCallBack {
	void onStart(ImageView imageView);

	void onLoadCach(ImageView imageView, ReadImageResult bitmap);

	void onFinish(ImageView imageView, ReadImageResult bitmap);

	void onFailed(ImageView imageView, String path);
}
