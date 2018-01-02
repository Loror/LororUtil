package com.loror.lororUtil.http;

public interface AsyncClient<T> {
	void runBack(Runnable runnable);

	void runFore(Runnable runnable);

	void callBack(T t);
}
