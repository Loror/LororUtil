package com.loror.lororUtil.dataBus;

import android.content.Intent;
import android.os.Looper;

import com.loror.lororUtil.flyweight.ObjectPool;
import com.loror.lororUtil.annotation.DataRun;
import com.loror.lororUtil.annotation.RunThread;

import java.lang.reflect.Method;

public class ThreadModeReceiver {
    private DataBusReceiver receiver;
    @RunThread
    private int thread = RunThread.LASTTHREAD;
    private boolean sticky;
    private String[] filter;

    public ThreadModeReceiver(DataBusReceiver receiver) {
        this.receiver = receiver;
        if (receiver != null) {
            try {
                Method method = receiver.getClass().getMethod("receiveData", String.class, Intent.class);
                DataRun dataRun = method.getAnnotation(DataRun.class);
                if (dataRun != null) {
                    this.thread = dataRun.thread();
                    this.sticky = dataRun.sticky();
                    this.filter = dataRun.filter();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void receiveData(final String name, final Intent data) {
        if (this.filter.length > 0) {
            boolean find = false;
            for (String s : this.filter) {
                if ((name == null && s == null) || (name != null && name.equals(s))) {
                    find = true;
                    break;
                }
            }
            if (!find) {
                return;
            }
        }
        if (receiver != null) {
            switch (thread) {
                case RunThread.MAINTHREAD:
                    if (Looper.myLooper() == Looper.getMainLooper()) {
                        receiver.receiveData(name, data);
                    } else {
                        ObjectPool.getInstance().getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                receiver.receiveData(name, data);
                            }
                        });
                    }
                    break;
                case RunThread.NEWTHREAD:
                    new Thread() {
                        @Override
                        public void run() {
                            receiver.receiveData(name, data);
                        }
                    }.start();
                    break;
                case RunThread.LASTTHREAD:
                default:
                    receiver.receiveData(name, data);
                    break;
            }
        }
    }

    public boolean isSticky() {
        return sticky;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ThreadModeReceiver) {
            return receiver == ((ThreadModeReceiver) obj).receiver;
        }
        return false;
    }
}
