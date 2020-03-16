package com.loror.lororUtil.http;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import com.loror.lororUtil.text.TextUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class HttpsClient extends AsyncBaseClient<HttpsURLConnection> {
    //hostName校验
    private static String hostName = "any";
    //是否为安卓4开启TSLv1.2
    private static boolean suiteTSLAndroid4 = false;
    private static SSLSocketFactory socketFactory;

    public static void setHostName(String hostName) {
        HttpsClient.hostName = hostName;
    }

    /**
     * 为安卓4开启TSLv1.2
     */
    public static void setSuiteTSLAndroid4(boolean suiteTSLAndroid4) {
        HttpsClient.suiteTSLAndroid4 = suiteTSLAndroid4;
    }

    public static boolean isSuiteTSLAndroid4() {
        return suiteTSLAndroid4;
    }

    /**
     * 配置外部SSLSocketFactory
     */
    public static void setSocketFactory(SSLSocketFactory socketFactory) {
        HttpsClient.socketFactory = socketFactory;
    }

    public static SSLSocketFactory getSocketFactory() {
        return socketFactory;
    }

    /**
     * 适配安卓4开启TSLv1.2
     */
    protected static void suiteTSLAndroid4(HttpURLConnection connection) throws Exception {
        if (suiteTSLAndroid4 && connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(
                    socketFactory != null ?
                            new SSLSocketFactoryCompat(socketFactory) :
                            new SSLSocketFactoryCompat());
        }
    }

    /**
     * 初始化证书
     */
    public static void init(String keyPath, String password) {
        try {
            init(new FileInputStream(keyPath), password);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化证书
     */
    public static void init(InputStream keyStream, String password) {
        try {
            SSLContext sslcontext = SSLContext.getInstance("SSL", "SunJSSE");
            sslcontext.init(null, new TrustManager[]{new LororX509TrustManager(keyStream, password)},
                    new java.security.SecureRandom());
            HostnameVerifier ignoreHostnameVerifier = new HostnameVerifier() {
                public boolean verify(String s, SSLSession sslsession) {
                    boolean verify = (!TextUtil.isEmpty(hostName) && hostName.equals(s)) || "any".equals(hostName);
                    if (!verify) {
                        System.out.println("WARNING: Hostname is not matched for cert.");
                    } else if ("any".equals(hostName)) {
                        System.out.println("WARNING: Hostname is setted not verify.");
                    }
                    return verify;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(ignoreHostnameVerifier);
            HttpsURLConnection.setDefaultSSLSocketFactory(socketFactory = sslcontext.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("init ssl failed");
        }
    }

}
