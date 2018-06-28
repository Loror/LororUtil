package com.loror.lororUtil.view;

import java.lang.reflect.Field;

import com.loror.lororUtil.text.TextUtil;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;

public class ViewUtil {
    private static Class<?> globalIdClass;
    private static boolean suiteHump;
    private static boolean notClassAnotation;

    /**
     * 设置是适配驼峰
     */
    public static void setSuiteHump(boolean suiteHump) {
        ViewUtil.suiteHump = suiteHump;
    }

    /**
     * 设置Id
     */
    public static void setGlobalIdClass(Class<?> globalIdClass) {
        ViewUtil.globalIdClass = globalIdClass;
    }

    /**
     * 抽取控件
     */
    public static void find(Activity activity, Class<?> idClass) {
        boolean thisCheck = false;
        if (!notClassAnotation) {
            try {
                injectOfClassAnotation(activity, null, activity);
                return;
            } catch (Exception e) {
                Log.e("TAG_", "进入反射模式");
                e.printStackTrace();
                notClassAnotation = true;
                thisCheck = true;
            }
        }
        if (!injectObject(activity, new ViewFinder(activity), idClass) && thisCheck) {
            Log.e("TAG_", "退出反射模式");
            notClassAnotation = false;
        }
    }

    /**
     * 抽取控件
     */
    public static void find(Activity activity) {
        find(activity, (Class<?>) null);
    }

    /**
     * 抽取控件
     */
    public static void find(Object holder, View view, Class<?> idClass) {
        boolean thisCheck = false;
        if (!notClassAnotation) {
            try {
                injectOfClassAnotation(null, view, holder);
                return;
            } catch (Exception e) {
                Log.e("TAG_", "进入反射模式");
                e.printStackTrace();
                notClassAnotation = true;
                thisCheck = true;
            }
        }
        if (!injectObject(holder, new ViewFinder(view), idClass) && thisCheck) {
            Log.e("TAG_", "退出反射模式");
            notClassAnotation = false;
        }
    }

    /**
     * 抽取控件
     */
    public static void find(Object holder, View view) {
        find(holder, view, null);
    }

    private static void injectOfClassAnotation(Activity activity, View parent, Object holder) throws Exception {
        Class<?> type = Class.forName(holder.getClass().getCanonicalName() + "$$Finder");
        ClassAnotationFinder finder = (ClassAnotationFinder) type.newInstance();
        if (activity != null) {
            finder.find(holder, activity);
        } else {
            finder.find(holder, parent);
        }
    }

    /**
     * 通过反射取出所有ViewInject注解，循环执行赋值
     */
    private static boolean injectObject(Object handler, ViewFinder finder, Class<?> idClass) {
        if (idClass == null) {
            if (globalIdClass == null) {
                try {
                    Context context = finder.getContext();
                    globalIdClass = Class.forName(context.getPackageName() + ".R$id");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            idClass = globalIdClass;
        }
        Class<?> handlerType = handler.getClass();
        Field[] fields = handlerType.getDeclaredFields();
        if (fields == null)
            return false;
        boolean injected = false;
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            String name = field.getName();
            Find find = (Find) field.getAnnotation(Find.class);
            if (find != null) {
                try {
                    View annotations = finder.findViewById(find.value() == -1 ? getResourceId(name, idClass) : find.value());
                    if (suiteHump && annotations == null) {
                        annotations = finder.findViewById(find.value() == -1 ? getResourceId(TextUtil.humpToUnderlineLowercase(name), idClass) : find.value());
                    }
                    if (annotations != null) {
                        field.setAccessible(true);
                        field.set(handler, annotations);
                    }
                    injected = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return injected;
    }

    /**
     * 获取资源id
     */
    private static int getResourceId(String variableName, Class<?> idClass) {
        try {
            Field idField = idClass.getDeclaredField(variableName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
