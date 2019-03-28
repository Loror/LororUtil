package com.loror.lororUtil.http;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class DefaultAsyncClient implements AsyncClient<Responce> {
	private static ExecutorService server;

	@Override
	public void runBack(Runnable runnable) {
		if (server == null) {
			server = Executors.newFixedThreadPool(5);
		}
		server.execute(runnable);
	}

}
