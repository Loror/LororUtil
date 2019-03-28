package com.loror.lororUtil.http;

public interface AsyncClient<T> {
	void runBack(Runnable runnable);

	void callBack(T t);
}
