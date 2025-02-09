package com.loror.lororUtil.http;

import com.loror.lororUtil.text.TextUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class FileBody extends StreamBody {

    private File file;

    public FileBody(String filePath) {
        this(filePath, null);
    }

    public FileBody(String filePath, String fileName) {
        this(filePath, fileName, null);
    }

    public FileBody(String filePath, String fileName, String contentType) {
        super(fileName, null, contentType);
        setFile(TextUtil.isEmpty(filePath) ? null : new File(filePath));
        setName(fileName);
        setContentType(contentType);
    }

    /**
     * 设置文件
     */
    public void setFile(File file) {
        if (file == null || !file.exists()) {
            this.file = null;
        } else {
            this.file = file;
            setName(file.getName());
        }
    }

    /**
     * 获取文件
     */
    public File getFile() {
        return file;
    }

    @Override
    public synchronized InputStream getInputStream() {
        if (file != null) {
            if (inputStream == null) {
                try {
                    inputStream = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            return inputStream;
        }
        return super.getInputStream();
    }

    @Override
    public long length() {
        if (file != null) {
            return file.length();
        }
        return super.length();
    }

}
