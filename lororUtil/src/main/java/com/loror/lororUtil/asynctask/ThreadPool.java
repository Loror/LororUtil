package com.loror.lororUtil.asynctask;

import java.util.LinkedList;
import java.util.List;

public class ThreadPool implements RemoveableThreadPool {

    private int excuteType = EXCUTETYPE_ORDER;// 执行模式
    private final Thread[] threads;// 线程组
    private final boolean[] alive;// 线程活动状态
    private final List<Runnable> tasks = new LinkedList<>();// 任务池

    private int delay = 50;
    private Catcher catcher;

    /**
     * 初始化线程池
     */
    public ThreadPool(int threadNumber) {
        threads = new Thread[threadNumber];
        alive = new boolean[threadNumber];
    }

    /**
     * 初始化线程
     */
    private Thread initThread(final int index) {
        return new Thread() {
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
                        if (size == 0 || size < index) {
                            alive[index] = false;
                            break;
                        } else {// 无任务，标记并线程结束
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
                        if (catcher != null) {
                            try {
                                runnable.run();
                            } catch (Exception e) {
                                catcher.catchException(e);
                            }
                        } else {
                            runnable.run();
                        }
                    }
                }
            }
        };
    }

    /**
     * 获取当前运行线程数
     */
    public int getAliveThread() {
        int count = 0;
        for (int i = 0; i < alive.length; i++) {
            if (alive[i]) {
                count++;
            }
        }
        return count;
    }

    /**
     * 唤醒线程
     */
    private void awakeThreads(int size) {
        for (int i = 0; i < threads.length && i < size; i++) {
            if (!alive[i]) {
                threads[i] = initThread(i);
                alive[i] = true;
                threads[i].start();
            }
        }
    }

    /**
     * 设置延时
     */
    public void setDelay(int delay) {
        if (delay < 0) {
            this.delay = 0;
        } else {
            this.delay = delay;
        }
    }

    /**
     * 设置异常捕获器
     */
    public void setCatcher(Catcher catcher) {
        this.catcher = catcher;
    }

    @Override
    public void setExcuteType(int excuteType) {
        this.excuteType = excuteType;
    }

    /**
     * 添加任务
     */
    @Override
    public synchronized void excute(Runnable task) {
        tasks.add(task);
        awakeThreads(tasks.size());
    }

    /**
     * 移除任务
     */
    @Override
    public synchronized void removeTask(Runnable task) {
        tasks.remove(task);
    }

    /**
     * 释放资源
     */
    public synchronized void release() {
        tasks.clear();
    }
}
