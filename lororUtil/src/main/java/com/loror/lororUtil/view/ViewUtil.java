package com.loror.lororUtil.view;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.loror.lororUtil.text.TextUtil;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;

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
    private static void find(Object object, Class<?> idClass) {
        boolean thisCheck = false;
        if (!notClassAnotation) {
            try {
                injectOfClassAnotation(object, null, object);
            } catch (Exception e) {
                Log.e("TAG_", "进入反射模式");
                e.printStackTrace();
                notClassAnotation = true;
                thisCheck = true;
            }
        }
        if (thisCheck && !injectFind(object, new ViewFinder(object), idClass)) {
            Log.e("TAG_", "退出反射模式");
            notClassAnotation = false;
        }
        click(object);
    }

    /**
     * 抽取控件
     */
    public static void find(Activity activity, Class<?> idClass) {
        find((Object) activity, idClass);
    }

    /**
     * 抽取控件
     */
    public static void find(Activity activity) {
        find((Object) activity, (Class<?>) null);
    }

    /**
     * 抽取控件
     */
    public static void find(Fragment fragment, Class<?> idClass) {
        find((Object) fragment, idClass);
    }

    /**
     * 抽取控件
     */
    public static void find(Fragment fragment) {
        find((Object) fragment, (Class<?>) null);
    }

    /**
     * 抽取控件
     */
    public static void find(android.support.v4.app.Fragment fragment, Class<?> idClass) {
        find((Object) fragment, idClass);
    }

    /**
     * 抽取控件
     */
    public static void find(android.support.v4.app.Fragment fragment) {
        find((Object) fragment, (Class<?>) null);
    }

    /**
     * 抽取控件
     */
    public static void find(Dialog dialog, Class<?> idClass) {
        find((Object) dialog, idClass);
    }

    /**
     * 抽取控件
     */
    public static void find(Dialog dialog) {
        find((Object) dialog, (Class<?>) null);
    }

    /**
     * 抽取控件
     */
    public static void find(Object holder, View view, Class<?> idClass) {
        boolean thisCheck = false;
        if (!notClassAnotation) {
            try {
                injectOfClassAnotation(null, view, holder);
            } catch (Exception e) {
                Log.e("TAG_", "进入反射模式");
                e.printStackTrace();
                notClassAnotation = true;
                thisCheck = true;
            }
        }
        if (thisCheck && !injectFind(holder, new ViewFinder(view), idClass)) {
            Log.e("TAG_", "退出反射模式");
            notClassAnotation = false;
        }
        click(holder, view);
    }

    /**
     * 抽取控件
     */
    public static void find(Object holder, View view) {
        find(holder, view, null);
    }

    private static void injectOfClassAnotation(Object source, View parent, Object holder) throws Exception {
        Class<?> type = Class.forName(holder.getClass().getName() + "$$Finder");
        ClassAnotationFinder finder = (ClassAnotationFinder) type.newInstance();
        if (parent != null) {
            finder.find(holder, parent);
        } else {
            finder.find(holder, source);
        }
    }

    /**
     * 通过反射取出所有Find注解，循环执行赋值
     */
    private static boolean injectFind(Object handler, ViewFinder finder, Class<?> idClass) {
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

    /**
     * 抽取控件
     */
    private static void click(Object Object) {
        boolean thisCheck = false;
        if (!notClassAnotation) {
            try {
                injectClickOfClassAnotation(Object, null, Object);
                return;
            } catch (Exception e) {
                Log.e("TAG_", "进入反射模式");
                e.printStackTrace();
                notClassAnotation = true;
                thisCheck = true;
            }
        }
        if (!injectClick(Object, new ViewFinder(Object)) && thisCheck) {
            Log.e("TAG_", "退出反射模式");
            notClassAnotation = false;
        }
    }

    /**
     * 抽取控件
     */
    public static void click(Activity activity) {
        click((Object) activity);
    }

    /**
     * 抽取控件
     */
    public static void click(Fragment fragment) {
        click((Object) fragment);
    }

    /**
     * 抽取控件
     */
    public static void click(android.support.v4.app.Fragment fragment) {
        click((Object) fragment);
    }

    /**
     * 抽取控件
     */
    public static void click(Dialog dialog) {
        click((Object) dialog);
    }

    /**
     * 抽取控件
     */
    public static void click(Object holder, View view) {
        boolean thisCheck = false;
        if (!notClassAnotation) {
            try {
                injectClickOfClassAnotation(null, view, holder);
                return;
            } catch (Exception e) {
                Log.e("TAG_", "进入反射模式");
                e.printStackTrace();
                notClassAnotation = true;
                thisCheck = true;
            }
        }
        if (!injectClick(holder, new ViewFinder(view)) && thisCheck) {
            Log.e("TAG_", "退出反射模式");
            notClassAnotation = false;
        }
    }

    private static void injectClickOfClassAnotation(Object source, View parent, Object holder) throws Exception {
        Class<?> type = Class.forName(holder.getClass().getName() + "$$Finder");
        ClassAnotationFinder finder = (ClassAnotationFinder) type.newInstance();
        if (parent != null) {
            finder.click(holder, parent);
        } else {
            finder.click(holder, source);
        }
    }

    /**
     * 通过反射取出所有Click,ItemClick注解，循环执行赋值
     */
    private static boolean injectClick(final Object holder, ViewFinder finder) {
        Method[] methods = holder.getClass().getDeclaredMethods();
        if (methods == null)
            return false;
        boolean injected = false;
        for (int i = 0; i < methods.length; i++) {
            final Method method = methods[i];
            Click click = (Click) method.getAnnotation(Click.class);
            ItemClick itemClick = (ItemClick) method.getAnnotation(ItemClick.class);
            if (click != null) {
                int id = click.id();
                View view = finder.findViewById(id);
                if (view != null) {
                    method.setAccessible(true);
                    final long clickSpace = click.clickSpace();
                    view.setOnClickListener(new View.OnClickListener() {
                        long clickTime;

                        @Override
                        public void onClick(View v) {
                            if (System.currentTimeMillis() - clickTime > clickSpace) {
                                clickTime = System.currentTimeMillis();
                                try {
                                    method.invoke(holder, v);
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    });
                }
                injected = true;
            } else if (itemClick != null) {
                int id = itemClick.id();
                final long clickSpace = itemClick.clickSpace();
                if (id != 0) {
                    View view = finder.findViewById(id);
                    if (view != null) {
                        method.setAccessible(true);
                        if (view instanceof AbsListView) {
                            ((AbsListView) view).setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                long clickTime;

                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    if (System.currentTimeMillis() - clickTime > clickSpace) {
                                        clickTime = System.currentTimeMillis();
                                        try {
                                            method.invoke(holder, view, position);
                                        } catch (IllegalAccessException e) {
                                            e.printStackTrace();
                                        } catch (InvocationTargetException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                        } else if (view instanceof ItemClickAble) {
                            ((ItemClickAble) view).setOnItemClickListener(new OnItemClickListener() {
                                long clickTime;

                                @Override
                                public void onItemClick(View view, int position) {
                                    if (System.currentTimeMillis() - clickTime > clickSpace) {
                                        clickTime = System.currentTimeMillis();
                                        try {
                                            method.invoke(holder, view, position);
                                        } catch (IllegalAccessException e) {
                                            e.printStackTrace();
                                        } catch (InvocationTargetException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                        }
                    }
                } else if (finder.getSource() instanceof View) {
                    ((View) finder.getSource()).setOnClickListener(new View.OnClickListener() {
                        long clickTime;

                        @Override
                        public void onClick(View v) {
                            if (System.currentTimeMillis() - clickTime > clickSpace) {
                                clickTime = System.currentTimeMillis();
                                try {
                                    method.invoke(holder, v);
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
                injected = true;
            }
        }
        return injected;
    }
}
