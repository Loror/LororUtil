package com.loror.lororUtil.asynctask;

import android.os.Handler;

import com.loror.lororUtil.flyweight.ObjectPool;

public class MainHandlerSchedule implements Schedule {

    private static final Handler handler = ObjectPool.getInstance().getHandler();

    @Override
    public void schedule(Runnable runnable) {
        handler.post(runnable);
    }
}
