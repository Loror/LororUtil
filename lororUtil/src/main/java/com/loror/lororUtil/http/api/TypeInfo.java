package com.loror.lororUtil.http.api;

import android.support.annotation.NonNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TypeInfo {

    private Type type;
    private static Class<?>[] rawType;

    public TypeInfo(@NonNull Type type) {
        if (type instanceof ParameterizedType) {
            if (((ParameterizedType) type).getRawType() == Observable.class) {
                this.type = ((ParameterizedType) type).getActualTypeArguments()[0];
            } else if (rawType != null) {
                boolean oneOf = false;
                for (Class<?> pop : rawType) {
                    if (pop == ((ParameterizedType) type).getRawType()) {
                        oneOf = true;
                        break;
                    }
                }
                this.type = oneOf ? ((ParameterizedType) type).getActualTypeArguments()[0] : type;
            }
        } else {
            this.type = type;
        }
    }

    /**
     * 设置忽略头
     */
    public static void setRawType(Class<?>... rawType) {
        TypeInfo.rawType = rawType;
    }

    /**
     * 如该类型在首位移除
     */
    public void pop(Class<?> head) {
        if (this.type instanceof ParameterizedType) {
            if (((ParameterizedType) this.type).getRawType() == head) {
                this.type = ((ParameterizedType) type).getActualTypeArguments()[0];
            }
        }
    }

    /**
     * 动态获取所有类型
     */
    public Class<?>[] getAllClass() {
        List<Class<?>> classes = new ArrayList<>();
        getAllClass(type, classes, true);
        return classes.toArray(new Class<?>[0]);
    }

    /**
     * 动态获取泛型
     */
    public Class<?>[] getTClass() {
        List<Class<?>> classes = new ArrayList<>();
        getAllClass(type, classes, false);
        return classes.toArray(new Class<?>[0]);
    }

    /**
     * 递归获取所有类型
     */
    private void getAllClass(Type type, List<Class<?>> classes, boolean containRaw) {
        if (type instanceof ParameterizedType) {
            if (containRaw) {
                classes.add((Class<?>) ((ParameterizedType) type).getRawType());
            }
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            for (int i = 0; i < types.length; i++) {
                getAllClass(types[i], classes, containRaw);
            }
        } else if (type instanceof Class) {
            classes.add((Class<?>) type);
        }
    }

    /**
     * 获取最后一个类型
     */
    public Class<?> getTypeClass() {
        Class<?>[] types = getAllClass();
        return types == null || types.length == 0 ? null : types[types.length - 1];
    }

    /**
     * Class是否为List或者List子类
     */
    public boolean isList() {
        Class<?>[] types = getAllClass();
        if (types.length == 1) {
            return false;
        }
        return ClassUtil.instanceOf(types[0], List.class);
    }

    /**
     * 获取范型
     */
    public Type getType() {
        return type;
    }

    /**
     * 修改解析结果
     * 确定您了解框架工作原理再修改
     */
    @Deprecated
    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "TypeInfo{" +
                "type=" + type +
                '}';
    }
}
