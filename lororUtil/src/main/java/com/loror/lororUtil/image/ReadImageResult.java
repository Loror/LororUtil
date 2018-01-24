package com.loror.lororUtil.image;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;

public class ReadImageResult {
	private List<Frame> frames = new ArrayList<>();
	private String path;
	private boolean repeate;

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

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void setRepeate(boolean repeate) {
		this.repeate = repeate;
	}

	public boolean isRepeate() {
		return repeate;
	}

	public int getCount() {
		return frames.size();
	}
}
