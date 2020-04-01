package com.loror.lororUtil.http;

public class MultipartConfig {

    protected static final String LINEEND = "\r\n";// 换行符
    protected static final String PREFIX = "--";
    protected static String BOUNDARY = java.util.UUID.randomUUID().toString();// 定义数据分隔线

    /**
     * 重置分隔线
     */
    public static void resetBoundary() {
        BOUNDARY = java.util.UUID.randomUUID().toString();
    }
}
