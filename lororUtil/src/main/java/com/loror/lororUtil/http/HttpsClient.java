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

import okhttp3.OkHttpClient;

public class HttpsClient extends HttpClient {

    public interface OnHttpsConfig {

        void onHttpsConfig(Object connection);
    }

    /*********************************以下为当前HttpsClient设置*******************************/

    //hostName校验
    private String[] singleHostName;
    //是否为安卓4开启TSLv1.2
    private boolean singleSuiteTSLAndroid4 = false;
    private SSLSocketFactory singleSocketFactory;
    private OnHttpsConfig singleOnHttpsConfig;

    public void setSingleOnHttpsConfig(OnHttpsConfig singleOnHttpsConfig) {
        this.singleOnHttpsConfig = singleOnHttpsConfig;
    }

    @Override
    protected void httpsConfig(HttpURLConnection conn) throws Exception {
        if (singleOnHttpsConfig != null) {
            singleOnHttpsConfig.onHttpsConfig(conn);
            return;
        }
        if (singleSocketFactory != null) {
            if (conn instanceof HttpsURLConnection) {
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) conn;
                httpsURLConnection.setHostnameVerifier(buildVerifier());
                if (singleSuiteTSLAndroid4) {
                    httpsURLConnection.setSSLSocketFactory(new SSLSocketFactoryCompat(singleSocketFactory));
                } else {
                    httpsURLConnection.setSSLSocketFactory(singleSocketFactory);
                }
            }
            return;
        }
        super.httpsConfig(conn);
    }

    @Override
    protected void httpsConfig(OkHttpClient.Builder builder) throws Exception {
        if (singleOnHttpsConfig != null) {
            singleOnHttpsConfig.onHttpsConfig(builder);
            return;
        }
        if (singleSocketFactory != null) {
            builder.hostnameVerifier(buildVerifier());
            if (singleSuiteTSLAndroid4) {
                builder.sslSocketFactory(new SSLSocketFactoryCompat(singleSocketFactory));
            } else {
                builder.sslSocketFactory(singleSocketFactory);
            }
            return;
        }
        super.httpsConfig(builder);
    }

    private HostnameVerifier buildVerifier() {
        return new HostnameVerifier() {
            public boolean verify(String s, SSLSession sslsession) {
                if (singleHostName == null) {
                    System.out.println("warning: Hostname is setted not verify.");
                    return true;
                }
                for (int i = 0; i < singleHostName.length; i++) {
                    String host = singleHostName[i];
                    if ((!TextUtil.isEmpty(host) && host.equals(s))) {
                        return true;
                    }
                }
                System.out.println("error: Hostname is not matched for cert.");
                return false;
            }
        };
    }

    public void setSingleHostName(String... singleHostName) {
        this.singleHostName = singleHostName;
    }

    public void setSingleSuiteTSLAndroid4(boolean singleSuiteTSLAndroid4) {
        this.singleSuiteTSLAndroid4 = singleSuiteTSLAndroid4;
    }

    public void setSingleSocketFactory(SSLSocketFactory singleSocketFactory) {
        this.singleSocketFactory = singleSocketFactory;
    }

    /*********************************以下为全局设置*******************************/

    //hostName校验
    private static String[] hostName;
    //是否为安卓4开启TSLv1.2
    private static boolean suiteTSLAndroid4 = false;
    //是否忽略证书校验
    private static boolean trustAll = false;
    private static HostnameVerifier hostnameVerifier;
    private static SSLSocketFactory socketFactory;
    private static OnHttpsConfig onHttpsConfig;

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

    /**
     * 设置监听https配置
     * */
    public static void setOnHttpsConfig(OnHttpsConfig onHttpsConfig) {
        HttpsClient.onHttpsConfig = onHttpsConfig;
    }

    public static SSLSocketFactory getSocketFactory() {
        return socketFactory;
    }

    public static class Config {

        /**
         * 证书配置处理
         */
        public static void httpsConfig(HttpURLConnection connection) throws Exception {
            if (onHttpsConfig != null) {
                onHttpsConfig.onHttpsConfig(connection);
                return;
            }
            if (connection instanceof HttpsURLConnection) {
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) connection;
                SSLSocketFactory factory = buildSSL();
                if (factory != null) {
                    httpsURLConnection.setHostnameVerifier(buildVerifier());
                    httpsURLConnection.setSSLSocketFactory(factory);
                }
            }
        }

        /**
         * 证书配置处理
         */
        public static void httpsConfig(OkHttpClient.Builder builder) throws Exception {
            if (onHttpsConfig != null) {
                onHttpsConfig.onHttpsConfig(builder);
                return;
            }
            SSLSocketFactory factory = buildSSL();
            if (factory != null) {
                builder.hostnameVerifier(buildVerifier());
                builder.sslSocketFactory(factory);
            }
        }

        private static HostnameVerifier buildVerifier() {
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
            return hostnameVerifier;
        }

        private static SSLSocketFactory buildSSL() throws Exception {
            if (socketFactory != null) {
                if (suiteTSLAndroid4) {
                    return new SSLSocketFactoryCompat(socketFactory);
                } else {
                    return socketFactory;
                }
            } else if (trustAll) {
                SSLSocketFactory socketFactory = buildUnsafeSSLSocket();
                if (suiteTSLAndroid4) {
                    return new SSLSocketFactoryCompat(socketFactory);
                } else {
                    return socketFactory;
                }
            } else {
                return null;
            }
        }
    }

    /*********************************信任所有证书*******************************/

    private static SSLSocketFactory buildUnsafeSSLSocket() throws Exception {
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
        return sc.getSocketFactory();
    }

}
