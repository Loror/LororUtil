package com.loror.lororUtil.http;

import com.loror.lororUtil.flyweight.ObjectPool;

import java.net.HttpURLConnection;

public class AsyncBaseClient extends BaseClient {

    /**
     * 设置回调主线程执行
     */
    private void initCallbackActuator() {
        if (callbackActuator == null) {
            callbackActuator = new Actuator() {
                @Override
                public void run(Runnable runnable) {
                    ObjectPool.getInstance().getHandler().post(runnable);
                }
            };
        }
    }

    public void asyncPost(final String urlStr, final RequestParams parmas, final AsyncClient<Responce> asyncClient) {
        initCallbackActuator();
        asyncClient.runBack(new Runnable() {

            @Override
            public void run() {
                final Responce responce = post(urlStr, parmas);
                callbackActuator.run(new Runnable() {

                    @Override
                    public void run() {
                        asyncClient.callBack(responce);
                    }
                });
            }
        });
    }

    public void asyncGet(final String urlStr, final RequestParams parmas, final AsyncClient<Responce> asyncClient) {
        initCallbackActuator();
        asyncClient.runBack(new Runnable() {

            @Override
            public void run() {
                final Responce responce = get(urlStr, parmas);
                callbackActuator.run(new Runnable() {

                    @Override
                    public void run() {
                        asyncClient.callBack(responce);
                    }
                });
            }
        });
    }

    public void asyncPut(final String urlStr, final RequestParams parmas, final AsyncClient<Responce> asyncClient) {
        initCallbackActuator();
        asyncClient.runBack(new Runnable() {

            @Override
            public void run() {
                final Responce responce = put(urlStr, parmas);
                callbackActuator.run(new Runnable() {

                    @Override
                    public void run() {
                        asyncClient.callBack(responce);
                    }
                });
            }
        });
    }

    public void asyncDelete(final String urlStr, final RequestParams parmas, final AsyncClient<Responce> asyncClient) {
        initCallbackActuator();
        asyncClient.runBack(new Runnable() {

            @Override
            public void run() {
                final Responce responce = delete(urlStr, parmas);
                callbackActuator.run(new Runnable() {

                    @Override
                    public void run() {
                        asyncClient.callBack(responce);
                    }
                });
            }
        });
    }

    public void asyncDownload(final String urlStr, final RequestParams parmas, final String path, final boolean cover,
                              final AsyncClient<Responce> asyncClient) {
        initCallbackActuator();
        asyncClient.runBack(new Runnable() {

            @Override
            public void run() {
                final Responce responce = download(urlStr, parmas, path, cover);
                callbackActuator.run(new Runnable() {

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
        initCallbackActuator();
        asyncClient.runBack(new Runnable() {

            @Override
            public void run() {
                final Responce responce = downloadInPiece(urlStr, path, start, end);
                callbackActuator.run(new Runnable() {

                    @Override
                    public void run() {
                        asyncClient.callBack(responce);
                    }
                });
            }
        });
    }
}
