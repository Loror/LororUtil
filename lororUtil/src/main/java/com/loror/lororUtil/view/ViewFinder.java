package com.loror.lororUtil.view;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.view.View;

public class ViewFinder {
    private final Object source;

    public ViewFinder(Object source) {
        this.source = source;
    }

    public Object getSource() {
        return source;
    }

    /**
     * 通过id查找控件
     */
    public View findViewById(int id) {
        View view = null;
        if (source instanceof Activity) {
            view = ((Activity) source).findViewById(id);
        } else if (source instanceof Fragment) {
            view = ((Fragment) source).getView().findViewById(id);
//        } else if (source instanceof android.support.v4.app.Fragment) {
//            view = ((android.support.v4.app.Fragment) source).getView().findViewById(id);
        } else if (source instanceof Dialog) {
            view = ((Dialog) source).findViewById(id);
        } else if (source instanceof View) {
            view = ((View) source).findViewById(id);
        }
        return view;
    }

    /**
     * 获取上下文
     */
    public Context getContext() {
        Context context = null;
        if (source instanceof Activity) {
            context = ((Activity) source);
        } else if (source instanceof Fragment) {
            context = ((Fragment) source).getActivity();
//        } else if (source instanceof android.support.v4.app.Fragment) {
//            context = ((android.support.v4.app.Fragment) source).getActivity();
        } else if (source instanceof Dialog) {
            context = ((Dialog) source).getContext();
        } else if (source instanceof View) {
            context = ((View) source).getContext();
        }
        return context;
    }

}
