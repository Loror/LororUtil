package com.loror.lororUtil.http.api;

public class ClassUtil {

    /**
     * child是否为parent类型或者之类型
     */
    public static boolean instanceOf(Class child, Class parent) {
        if (child != null) {
            do {
                if (child == parent) {
                    return true;
                }
                child = child.getSuperclass();
            } while (child != null);
        }
        return false;
    }
}
