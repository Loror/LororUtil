package com.loror.lororUtil.asynctask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IoSchedule implements Schedule {

    private static final ExecutorService server = Executors.newFixedThreadPool(3);

    @Override
    public void schedule(Runnable runnable) {
        server.execute(runnable);
    }
}
