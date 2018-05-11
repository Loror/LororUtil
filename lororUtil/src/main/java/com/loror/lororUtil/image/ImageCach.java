package com.loror.lororUtil.image;

public class ImageCach {
    /**
     * 图片缓存
     */
    private static Cache<ReadImageResult> cach = new Cache<ReadImageResult>() {

        @Override
        protected int sizeOf(ReadImageResult value) {
            int size = 0;
            for (int i = 0; i < value.getCount(); i++) {
                size += value.getFrame(i).image.getByteCount();
            }
            return size / 1024;
        }

        @Override
        protected void moveToWeak(String key, ReadImageResult value) {
            LockMap.removeLock(key);
        }
    };

    /**
     * 获取缓存
     */
    public static ReadImageResult getFromCache(String key) {
        return cach.getFromCache(key);
    }

    /**
     * 加入缓存
     */
    public static void pushToCach(String key, ReadImageResult value) {
        if (key != null && value != null) {
            cach.pushToCach(key, value);
        }
    }

    /**
     * 清空缓存
     */
    public static void clearCach() {
        cach.clearCach();
    }
}
