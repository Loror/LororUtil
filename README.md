## Studio中引入项目


dependencies {

    compile 'com.github.Loror:LororUtil:1.0.7'
 
}

allprojects {

	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}


## image包

* 类ImageUtil
    * 方法with(Context context) 获取ImageUtil对象
    * 方法setDefaultImage(int defaultImage) 设置读取图片开始时占位图
    * 方法setErrorImage(int errorImage) 设置读取失败时占位图
    * 方法from(String path) 设置图片来源
    * 方法to(ImageView imageView) 设置加载到控件
    * 方法setWidthLimit(int widthLimit) 设置加载图片宽度限制，默认200
    * 方法setReadImage(ReadImage readImage) 设置图片加载接口
    * 方法setRemoveOldTask(boolean removeOldTask) 设置是否移除被快速滑出的任务，任务一旦开始无法移除
    * 方法setCachUseAnimation(boolean cachUseAnimation) 是否为本地内存加载图片启用动画
    * 方法setIsGif(boolean isGif) 是否以gif方式加载图片，如图片不是gif图片将以静态图片加载，但不会进入内存的二级缓存
    * 方法setOnLoadListener(ImageUtilCallBack onLoadListener) 用于追踪图片加载的生命周期
    * 方法setTargetDir(File targetDir) 设置缓存路径
    * 方法setLoadAnimation(Animation loadAnimation) 设置加载成功动画
    * 方法loadImage() 开始加载图片


* 接口ImageUtilCallBack
ImageUtil回调接口
    * 方法onStart(ImageView imageView) 开始加载图片时回调
    * 方法onLoadCach(ImageView imageView, Bitmap bitmap) 从内存缓存获得图片时回调
    * 方法onFinish(ImageView imageView, Bitmap bitmap) 获取图片成功时回调
    * 方法onFailed(ImageView imageView, String path) 获取图片失败时回调


* 接口ReadImageImageUtil
后台读取图片接口
    * 抽象方法readImage(String path, int widthLimit) 子类重写读取图片方法
    * 已实现的实现类ReadHttpImage，读取网络图片，ReadSDCardImage，读取sd卡图片，ReadSDCardVideo，读取sd卡视频缩略图。

## http包

* 类HttpClient
    * 方法setTimeOut(int timeOut) 设置http连接超时时间
    * 方法setReadTimeOut(int readTimeOut) 设置读取超时时间
    * 方法post(String urlStr, RequestParmas parmas) post网络请求，返回responce对象
    * 方法get(String urlStr, RequestParmas parmas) get网络请求，返回responce对象
    * 方法asyncPost(final String urlStr, final RequestParams parmas, final AsyncClient<Responce> asyncClient) 异步Post请求，已有AsyncClient实现类DefaultAsyncClient可用
    * 方法asyncGet(final String urlStr, final RequestParams parmas, final AsyncClient<Responce> asyncClient) 异步Get请求，已有AsyncClient实现类DefaultAsyncClient可用
    * 方法download(String urlStr, String path, boolean cover) 通过http下载文件，路径为文件夹路径时将以服务器端文件名存储到该文件下，若为文件路径则直接存储，cover为false时先检查服务器端文件与本地文件大小是否一致，不一致方下载文件，否则忽略，cover为true时不检查直接覆盖下载
    * 方法downloadInPeice(String urlStr, String path, long start, long end) 断点下载，该方法含同步锁，若需多线程下载文件，应实例化多个HttpClient对象
    * 方法cancel() 取消当前请求
    * 方法setProgressListener(ProgressListener progressListener) 设置下载或上传文件进度监听


* 类HttpsClient
基本方法与HttpClient相同
    * 方法init(String keyPath, String password) 加载本地证书
    * 方法setHostName(String hostName) 设置hostname校验，不设置时不校验


* 接口ProgressListenerHttpClient
HttpsClient监听器
    * 抽象方法transing(int progress, int speed, long length) 参数为进度，传输速度（单位字节），文件长度（单位字节）
    * 抽象方法failed() 传输失败时被调用
    * 抽象方法finish(String result) 传输完成时被调用，result为服务器返回


* 类RequestParmas HttpClient 
HttpsClient使用参数类
    * 方法fromObject(Object object) 添加参数
    * 方法fromKeyValue(String params) 添加参数
    * 方法addParmas(String key, String value) 添加参数
    * 方法addParmas(String key, FileBody value) 添加参数
    * 方法getParmas() 获取参数类中所有参数
    * 方法getParma(String key) 通过键获取参数类中参数
    * 方法setHead(String name, String value) 设置请求头
    * 方法packetOutParmas() 打包参数，HttpClient类调用


* 类Responce 
    * 属性result 返回字节数组结果
    * 方法getHeaders() 获取http头
    * 方法getCookies() 获取cookies
    * 方法getThrowable() 获取异常信息
    * 方法getCookielist() 获取cookie列表
    * 方法getCode() 获取返回码
    * 方法toString() 获取字符串结果


* 类FileBody
HttpClient HttpsClient使用参数类
    * 构造方法FileBody(String filePath, String fileName, String contentType) 构造一个FileBody对象，post提交时会提交名字与文件流，fileName为空时默认以文件名构造，contentType为空时默认以"application/octet-stream"构造


## view包

* 注解Find
需配合工具类ViewUtil使用
    * @Find(R.id.xx) 用于查找控件注解


* 类ViewUtil
    * 静态方法find(Activity activity) 查找activity中find注解并为控件初始化
    * 静态方法find(Object holder, View view) 查找holder中注解并为控件初始化，需传入父控件view


## asynctask包


* 类AsyncUtil
    * 静态方法excute(final Excute<T> excute) 异步工作，子线程执行接口Excute中doBack()方法，执行完毕回调result(T result)至主线程


* 接口Excute<T>
    * T 泛型 异步工具类方法泛型与接口泛型保持一致，doBack()方法返回此类型参数，回调方法传回参数值
    * 方法doBack() 异步工具类调用此方法后台执行 方法result(T result) 异步工具类回调用此方法主线程执行


* 类ThreadPool线程池，继承自RemoveableThreadPool，内部维护一套简单线程池
    * 构造方法ThreadPool(int threadNumber) 构造一个最多包含threadNumber个线程的线程池
    * 方法excute(Runnable task, int excuteType) 向线程池添加任务，参数excuteType，三个参数，EXCUTETYPE_RANDOM随机执行，EXCUTETYPE_ORDER先进先出，EXCUTETYPE_BACK先进后出。
    * 方法removeTask(Runnable task) 移除任务，只能移除尚未开始的任务
