package com.loror.lororUtil.http;

import com.loror.lororUtil.text.TextUtil;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class StreamBody {

    private String key;
    private String name;
    private String contentType;
    protected InputStream inputStream;

    public StreamBody(InputStream inputStream) {
        this(inputStream, null);
    }

    public StreamBody(InputStream inputStream, String contentType) {
        this(null, inputStream, contentType);
    }

    public StreamBody(String name, InputStream inputStream, String contentType) {
        setKey("file");
        setName(name);
        setContentType(contentType);
        setInputStream(inputStream);
    }

    /**
     * 设置键名
     */
    public void setKey(String key) {
        this.key = key == null ? "file" : key;
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
     * 设置multipartName
     */
    public void setName(String name) {
        if (!TextUtil.isEmpty(name)) {
            this.name = name;
        }
    }

    /**
     * 获取multipartName
     */
    public String getName() {
        return name == null ? "undefine" : name;
    }

    /**
     * 设置流
     */
    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * 获取流
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    public byte[] getBytes() {
        try {
            if (inputStream != null) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] data = new byte[1024 * 1024];
                int read = 0;
                while ((inputStream.read(data)) != -1) {
                    outputStream.write(data, 0, read);
                }
                return outputStream.toByteArray();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return null;
    }

    /**
     * 长度
     */
    public long length() {
        try {
            return inputStream != null ? inputStream.available() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 关闭流
     */
    public void close() {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        inputStream = null;
    }
}
