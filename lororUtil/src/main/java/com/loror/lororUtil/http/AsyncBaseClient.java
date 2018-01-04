package com.loror.lororUtil.http;

import java.net.HttpURLConnection;

public class AsyncBaseClient<T extends HttpURLConnection> extends BaseClient<T> {

    private AsyncClient<Responce> asyncClient;

    @Override
    protected void postRunnable(Runnable runnable) {
        if (asyncClient == null) {
            super.postRunnable(runnable);
        } else {
            asyncClient.runFore(runnable);
        }
    }

    public void asyncPost(final String urlStr, final RequestParams parmas, final AsyncClient<Responce> asyncClient) {
        this.asyncClient = asyncClient;
        asyncClient.runBack(new Runnable() {

            @Override
            public void run() {
                final Responce responce = post(urlStr, parmas);
                AsyncBaseClient.this.asyncClient = null;
                asyncClient.runFore(new Runnable() {

                    @Override
                    public void run() {
                        asyncClient.callBack(responce);
                    }
                });
            }
        });
    }

    public void asyncGet(final String urlStr, final RequestParams parmas, final AsyncClient<Responce> asyncClient) {
        asyncClient.runBack(new Runnable() {

            @Override
            public void run() {
                final Responce responce = get(urlStr, parmas);
                asyncClient.runFore(new Runnable() {

                    @Override
                    public void run() {
                        asyncClient.callBack(responce);
                    }
                });
            }
        });
    }

    public void asyncDownload(final String urlStr, final String path, final boolean cover,
                              final AsyncClient<Responce> asyncClient) {
        this.asyncClient = asyncClient;
        asyncClient.runBack(new Runnable() {

            @Override
            public void run() {
                final Responce responce = download(urlStr, path, cover);
                AsyncBaseClient.this.asyncClient = null;
                asyncClient.runFore(new Runnable() {

                    @Override
                    public void run() {
                        asyncClient.callBack(responce);
                    }
                });
            }
        });
    }

    public void asyncDownloadInPeice(final String urlStr, final String path, final long start,
                                     final long end, final AsyncClient<Responce> asyncClient) {
        this.asyncClient = asyncClient;
        asyncClient.runBack(new Runnable() {

            @Override
            public void run() {
                final Responce responce = downloadInPeice(urlStr, path, start, end);
                AsyncBaseClient.this.asyncClient = null;
                asyncClient.runFore(new Runnable() {

                    @Override
                    public void run() {
                        asyncClient.callBack(responce);
                    }
                });
            }
        });
    }
}
