package com.loror.lororUtil.http;

public interface ProgressListener {
	void transing(int progress, int speed, long length);

	void failed();

	void finish(String result);
}
