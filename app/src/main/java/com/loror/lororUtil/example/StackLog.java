package com.loror.lororUtil.example;

import android.util.Log;

public class StackLog {

    private static final int line = 10;

    public static void e(String tag, String message, Throwable e) {
        e(tag, message + Log.getStackTraceString(e));
    }

    public static void e(String tag, String message) {
        StringBuilder builder = new StringBuilder(message).append("|").append("\n");
        int index = 0;
        for (StackTraceElement stackTraceElement : new RuntimeException().getStackTrace()) {
            if (stackTraceElement.getClassName().equals(StackLog.class.getName())) {
                continue;
            }
            builder.append(stackTraceElement.getClassName()).append(".")
                    .append(stackTraceElement.getMethodName()).append("(")
                    .append(stackTraceElement.getFileName()).append(":")
                    .append(stackTraceElement.getLineNumber()).append(")\n");
            index++;
            if (index >= line) {
                break;
            }
        }
        Log.e(tag, builder.toString());
    }
}
