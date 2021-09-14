package com.loror.lororUtil.asynctask;

/**
 * 请使用AsyncTask
 */
@Deprecated
public class AsyncUtil {

    /**
     * 回调接口
     */
    @Deprecated
    public interface Excute<T> {
        T doBack();

        void result(T result);
    }

    /**
     * 异步执行
     */
    @Deprecated
    public static <T> void excute(final Excute<T> excute) {
        new FlowTask()
                .ioSchedule()
                .catcher(new Catcher() {
                    @Override
                    public void catchException(Exception e) {
                        e.printStackTrace();
                    }
                })
                .create(new Func0<T>() {
                    @Override
                    public T func() {
                        return excute.doBack();
                    }
                })
                .mainHandlerSchedule()
                .call(new Func1<T>() {
                    @Override
                    public void func(T it) {
                        excute.result(it);
                    }
                });
    }
}
