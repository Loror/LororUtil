package com.loror.lororUtil.http;

public interface ProgressListener {
	void transing(float progress, int speed, long length);

	void finish(boolean success);
}
