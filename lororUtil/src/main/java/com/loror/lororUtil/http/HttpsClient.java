package com.loror.lororUtil.http;

import com.loror.lororUtil.text.TextUtil;

import java.net.HttpURLConnection;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpsClient extends AsyncBaseClient<HttpsURLConnection> {
    //hostName校验
    private static String[] hostName;
    //是否为安卓4开启TSLv1.2
    private static boolean suiteTSLAndroid4 = false;
    //是否忽略证书校验
    private static boolean trustAll = false;
    private static HostnameVerifier hostnameVerifier;
    private static SSLSocketFactory socketFactory;

    /**
     * 设置域名校验
     */
    public static void setHostName(String... hostName) {
        HttpsClient.hostName = hostName;
        hostnameVerifier = null;
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
     * 设置是否忽略证书校验
     */
    @Deprecated
    public static void setTrustAll(boolean trustAll) {
        HttpsClient.trustAll = trustAll;
    }

    public static boolean isTrustAll() {
        return trustAll;
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

    public static class Config {
        /**
         * 证书配置处理
         */
        public static void httpsConfig(HttpURLConnection connection) throws Exception {
            if (connection instanceof HttpsURLConnection) {
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) connection;
                if (hostnameVerifier == null) {
                    hostnameVerifier = new HostnameVerifier() {
                        public boolean verify(String s, SSLSession sslsession) {
                            if (hostName == null) {
                                System.out.println("warning: Hostname is setted not verify.");
                                return true;
                            }
                            for (int i = 0; i < hostName.length; i++) {
                                String host = hostName[i];
                                if ((!TextUtil.isEmpty(host) && host.equals(s))) {
                                    return true;
                                }
                            }
                            System.out.println("error: Hostname is not matched for cert.");
                            return false;
                        }
                    };
                }
                httpsURLConnection.setHostnameVerifier(hostnameVerifier);
                if (socketFactory != null) {
                    if (suiteTSLAndroid4) {
                        httpsURLConnection.setSSLSocketFactory(new SSLSocketFactoryCompat(socketFactory));
                    } else {
                        httpsURLConnection.setSSLSocketFactory(socketFactory);
                    }
                } else if (trustAll) {
                    TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }};

                    SSLContext sc = SSLContext.getInstance("TLS");
                    // trustAllCerts信任所有的证书
                    sc.init(null, trustAllCerts, new SecureRandom());
                    SSLSocketFactory socketFactory = sc.getSocketFactory();
                    if (suiteTSLAndroid4) {
                        httpsURLConnection.setSSLSocketFactory(new SSLSocketFactoryCompat(socketFactory));
                    } else {
                        httpsURLConnection.setSSLSocketFactory(socketFactory);
                    }
                }
            }
        }
    }

}
