package com.loror.lororUtil.http;

import java.net.HttpURLConnection;

public class AsyncBaseClient<T extends HttpURLConnection> extends BaseClient<T> {

    public void asyncPost(final String urlStr, final RequestParams parmas, final AsyncClient<Responce> asyncClient) {
        asyncClient.runBack(new Runnable() {

            @Override
            public void run() {
                final Responce responce = post(urlStr, parmas, new Excutor() {
                    @Override
                    public void run(Runnable runnable) {
                        asyncClient.runFore(runnable);
                    }
                });
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

    public void asyncPut(final String urlStr, final RequestParams parmas, final AsyncClient<Responce> asyncClient) {
        asyncClient.runBack(new Runnable() {

            @Override
            public void run() {
                final Responce responce = put(urlStr, parmas, new Excutor() {
                    @Override
                    public void run(Runnable runnable) {
                        asyncClient.runFore(runnable);
                    }
                });
                asyncClient.runFore(new Runnable() {

                    @Override
                    public void run() {
                        asyncClient.callBack(responce);
                    }
                });
            }
        });
    }

    public void asyncDelete(final String urlStr, final RequestParams parmas, final AsyncClient<Responce> asyncClient) {
        asyncClient.runBack(new Runnable() {

            @Override
            public void run() {
                final Responce responce = delete(urlStr, parmas);
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
        asyncClient.runBack(new Runnable() {

            @Override
            public void run() {
                final Responce responce = download(urlStr, path, cover, new Excutor() {
                    @Override
                    public void run(Runnable runnable) {
                        asyncClient.runFore(runnable);
                    }
                });
                asyncClient.runFore(new Runnable() {

                    @Override
                    public void run() {
                        asyncClient.callBack(responce);
                    }
                });
            }
        });
    }

    public void asyncDownloadInPiece(final String urlStr, final String path, final long start,
                                     final long end, final AsyncClient<Responce> asyncClient) {
        asyncClient.runBack(new Runnable() {

            @Override
            public void run() {
                final Responce responce = downloadInPiece(urlStr, path, start, end, new Excutor() {
                    @Override
                    public void run(Runnable runnable) {
                        asyncClient.runFore(runnable);
                    }
                });
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
