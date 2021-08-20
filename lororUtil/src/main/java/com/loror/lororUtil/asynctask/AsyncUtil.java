package com.loror.lororUtil.asynctask;

/**
 * 请使用AsyncTask
 */
@Deprecated
public class AsyncUtil extends AsyncTask {

    /**
     * 回调接口
     */
    @Deprecated
    public interface Excute<T> extends Task<T> {

    }

    /**
     * 异步执行
     */
    @Deprecated
    public static <T> void excute(final Excute<T> excute) {
        AsyncTask.run(excute);
    }
}
