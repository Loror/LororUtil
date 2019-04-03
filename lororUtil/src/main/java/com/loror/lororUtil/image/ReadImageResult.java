package com.loror.lororUtil.image;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;

public class ReadImageResult {
	private List<Frame> frames = new ArrayList<>();
	private String originPath;
	private String path;
	//-1，加载路径为空 0，成功 1，网络下载失败 2,图片解析错误 3，内存溢出 4，超时无法获取
	private int errorCode;
	private Throwable throwable;
	private boolean repeate = true;
	private boolean pause;

	public Bitmap getBitmap() {
		return frames.size() == 0 ? null : frames.get(0).image;
	}

	public void addFrame(Frame frame) {
		if (frame != null) {
			frames.add(frame);
		}
	}

	public Frame getFrame(int position) {
		return frames.get(position);
	}

	public void setOriginPath(String originPath) {
		this.originPath = originPath;
	}

	public String getOriginPath() {
		return originPath;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setRepeate(boolean repeate) {
		this.repeate = repeate;
	}

	public boolean isRepeate() {
		return repeate;
	}

	public void setPause(boolean pause) {
		this.pause = pause;
	}

	public boolean isPause() {
		return pause;
	}

	public void setThrowable(Throwable throwable) {
		this.throwable = throwable;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public int getCount() {
		return frames.size();
	}
}
