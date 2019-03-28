package com.loror.lororUtil.http;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import com.loror.lororUtil.text.TextUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class HttpsClient extends AsyncBaseClient<HttpsURLConnection> {
    private static String hostName = "any";

    public static void setHostName(String hostName) {
        HttpsClient.hostName = hostName;
    }

    public static void init(String keyPath, String password) {
        try {
            init(new FileInputStream(keyPath), password);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

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
            HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("init ssl failed");
        }
    }

}
