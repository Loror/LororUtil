package com.loror.lororUtil.http;
public interface Client {
	/**
	 * post请求，参数，1，url地址，2，提交参数，无回调需开线程处理
	 */
	Responce post(String urlStr, RequestParams parmas);

	/**
	 * get请求，参数，1，url地址，2，提交参数，无回调需开线程处理
	 */
	Responce get(String urlStr, RequestParams parmas);

	/**
	 * 取消当前请求
	 */
	boolean cancel();
}
