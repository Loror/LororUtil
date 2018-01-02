package com.loror.lororUtil.asynctask;

import java.util.Vector;

public class ThreadPool implements RemoveableThreadPool {

    private boolean isEnd;// 是否所有任务结束
    private int excuteType;// 执行模式
    private Thread[] threads;// 线程组
    private boolean[] alive;// 线程活动状态
    private Vector<Runnable> tasks;// 任务池

    private int delay = 50;

    /**
     * 初始化线程池
     */
    public ThreadPool(int threadNumber) {
        threads = new Thread[threadNumber];
        alive = new boolean[threadNumber];
        tasks = new Vector<Runnable>();
        initThreadPool();
    }

    /**
     * 初始化线程池线程
     */
    private void initThreadPool() {
        isEnd = false;
        for (int i = 0; i < threads.length; i++) {
            threads[i] = initThread(i);
        }
        alive[0] = true;
        threads[0].start();
    }

    /**
     * 初始化线程
     */
    private Thread initThread(final int index) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                for (; ; ) {
                    try {
                        sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Runnable runnable = null;
                    synchronized (ThreadPool.this) {
                        int size = tasks.size();
                        if (size == 0) {
                            if (index != 0) {
                                alive[index] = false;
                                threads[index] = initThread(index);
                            } else {
                                isEnd = true;
                            }
                            break;
                        } else {// 无任务，标记并线程结束
                            if (index == 0) {
                                awakeThreads(size);
                            }
                            try {
                                switch (excuteType) {
                                    case EXCUTETYPE_ORDER:
                                        runnable = tasks.get(0);
                                        break;
                                    case EXCUTETYPE_BACK:
                                        runnable = tasks.get(size - 1);
                                        break;
                                    default:
                                        runnable = tasks.get((int) (Math.random() * tasks.size()));
                                        break;
                                }
                                if (tasks.contains(runnable)) {
                                    tasks.remove(runnable);
                                } else {
                                    runnable = null;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } // 有任务，取出任务
                    }
                    if (runnable != null) {
                        try {
                            runnable.run();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        return thread;
    }

    /**
     * 唤醒线程
     */
    private void awakeThreads(int size) {
        for (int i = 1; i < threads.length && i < size / 3 + 1; i++) {
            if (!alive[i]) {
                alive[i] = true;
                threads[i].start();
            }
        }
    }

    /**
     * 设置延时
     */
    public void setDelay(int delay) {
        if (delay < 50) {
            this.delay = 50;
        } else {
            this.delay = delay;
        }
    }

    /**
     * 添加任务
     */
    public synchronized void excute(Runnable task, int excuteType) throws IllegalStateException {
        this.excuteType = excuteType;
        if (isEnd) {
            initThreadPool();// 已结束重新实例线程池
        }
        tasks.add(task);
    }

    /**
     * 移除任务
     */
    public synchronized void removeTask(Runnable task) {
        tasks.remove(task);
    }

    /**
     * 释放资源
     */
    @Override
    public void finalize() throws Throwable {

    }
}
