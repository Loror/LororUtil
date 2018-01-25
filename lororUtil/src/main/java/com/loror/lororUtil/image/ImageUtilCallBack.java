package com.loror.lororUtil.image;

import android.widget.ImageView;

public interface ImageUtilCallBack {
	void onStart(ImageView imageView);

	void onLoadCach(ImageView imageView, ReadImageResult result);

	void onFinish(ImageView imageView, ReadImageResult result);

	void onFailed(ImageView imageView, ReadImageResult result);
}
