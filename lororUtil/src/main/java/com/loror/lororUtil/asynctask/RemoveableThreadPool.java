package com.loror.lororUtil.asynctask;

public interface RemoveableThreadPool {

    int EXCUTETYPE_RANDOM = 0;// 随机
    int EXCUTETYPE_ORDER = 1;// 顺序
    int EXCUTETYPE_BACK = 2;// 反序

    void setExcuteType(int excuteType);

    void excute(Runnable task);

    void removeTask(Runnable task);
}
