package com.loror.lororUtil.http;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.loror.lororUtil.flyweight.ObjectPool;

public abstract class DefaultAsyncClient implements AsyncClient<Responce> {
	private static ExecutorService server;

	@Override
	public void runFore(Runnable runnable) {
		ObjectPool.getInstance().getHandler().post(runnable);
	}

	@Override
	public void runBack(Runnable runnable) {
		if (server == null) {
			server = Executors.newFixedThreadPool(3);
		}
		server.execute(runnable);
	}

}
