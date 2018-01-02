package com.loror.lororUtil.http;

import java.io.File;

import com.loror.lororUtil.text.TextUtil;

public class FileBody {
	private String key;
	private String fileName;
	private String contentType;
	private File file;

	public FileBody(String filePath) {
		this(filePath, null, null);
	}

	public FileBody(String filePath, String fileName) {
		this(filePath, fileName, null);
	}

	public FileBody(String filePath, String fileName, String contentType) {
		this.key = "file";
		setFile(TextUtil.isEmpty(filePath) ? null : new File(filePath));
		setName(fileName);
		setContentType(contentType);
	}

	/**
	 * 设置键名
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * 获取键名
	 */
	public String getKey() {
		return key;
	}

	/**
	 * 设置类型
	 */
	public void setContentType(String contentType) {
		if (TextUtil.isEmpty(contentType)) {
			this.contentType = "application/octet-stream";
		} else {
			this.contentType = contentType;
		}
	}

	/**
	 * 获取类型
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * 设置名字
	 */
	public void setName(String fileName) {
		if (!TextUtil.isEmpty(fileName)) {
			this.fileName = fileName;
		}
	}

	/**
	 * 获取名字
	 */
	public String getName() {
		return fileName;
	}

	/**
	 * 设置文件
	 */
	public void setFile(File file) {
		if (file == null || !file.exists()) {
			this.file = null;
		} else {
			this.file = file;
			this.fileName = file.getName();
		}
	}

	/**
	 * 获取文件
	 */
	public File getFile() {
		return file;
	}
}
