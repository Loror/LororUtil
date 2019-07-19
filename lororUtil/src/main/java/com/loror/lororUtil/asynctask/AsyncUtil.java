package com.loror.lororUtil.asynctask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.loror.lororUtil.flyweight.ObjectPool;

public class AsyncUtil {
    private static ExecutorService server = Executors.newFixedThreadPool(3);

    /**
     * 回调接口
     */
    public interface Excute<T> {
        T doBack();

        void result(T result);
    }

    /**
     * 异步执行
     */
    public static <T> void excute(final Excute<T> excute) {
        server.execute(new Runnable() {

            @Override
            public void run() {
                T t = null;
                try {
                    t = excute.doBack();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                final T result = t;
                ObjectPool.getInstance().getHandler().post(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            excute.result(result);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
}
