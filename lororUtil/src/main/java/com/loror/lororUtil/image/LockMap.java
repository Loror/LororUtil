package com.loror.lororUtil.image;

import java.util.concurrent.ConcurrentHashMap;

public class LockMap {
    private static ConcurrentHashMap<String, SingleLock> locks = new ConcurrentHashMap<String, SingleLock>();

    public static synchronized SingleLock getLock(String key) {
        SingleLock lock = locks.get(key);
        if (lock == null) {
            lock = new SingleLock();
            locks.put(key, lock);
        }
        return lock;
    }

    public static synchronized void removeLock(String key) {
        locks.remove(key);
    }

    public static class SingleLock {
        public volatile int mark;
    }
}
