package com.loror.lororUtil.view;

import android.app.Activity;
import android.content.Context;
import android.view.View;

public class ViewFinder {
    private Activity activity;
    private View view;

    public ViewFinder(Activity activity) {
        this.activity = activity;
    }

    public ViewFinder(View view) {
        this.view = view;
    }

    /**
     * 通过id查找控件
     */
    public View findViewById(int id) {
        View view;
        if (this.activity != null) {
            view = this.activity.findViewById(id);
        } else {
            view = this.view.findViewById(id);
        }
        return view;
    }

    /**
     * 获取上下文
     */
    public Context getContext() {
        Context context;
        if (this.view != null) {
            context = this.view.getContext();
        } else {
            context = this.activity;
        }
        return context;
    }

}
