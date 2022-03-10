# Android辅助开发Library-LororUtil

[![License](https://img.shields.io/badge/License%20-Apache%202-337ab7.svg)](https://www.apache.org/licenses/LICENSE-2.0)

## Studio中引入项目

```
dependencies {
    compile 'com.github.Loror:LororUtil:1.9.33'
}

allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```


## image包

* 类ImageUtil
    * 方法with(Context context) 获取ImageUtil对象
    * 方法setDefaultImage(int defaultImage) 设置读取图片开始时占位图
    * 方法setErrorImage(int errorImage) 设置读取失败时占位图
    * 方法from(String path) 设置图片来源
    * 方法to(ImageView imageView) 设置加载到控件
    * 方法setWidthLimit(int widthLimit) 设置加载图片宽度限制，默认获取imageView宽度，如获取不到使用默认值500
    * 方法setReadImage(ReadImage readImage) 设置自定义图片加载接口，内部已有实现接口，如您不清楚如何自定义，请谨慎使用
    * 方法setBitmapConverter(BitmapConverter bitmapConverter) 设置图片显示预处理接口
    * 方法setRemoveOldTask(boolean removeOldTask) 设置是否移除被快速滑出的任务，任务一旦开始无法移除，默认移除
    * 方法setCachUseAnimation(boolean cachUseAnimation) 是否为本地内存加载图片启用动画
    * 方法setIsGif(boolean isGif) 是否以gif方式加载图片，如图片不是gif图片将以静态图片加载，但不会进入内存的二级缓存
    * 方法setOnLoadListener(ImageUtilCallBack onLoadListener) 用于追踪图片加载的生命周期
    * 方法setTargetDir(File targetDir) 设置缓存路径
    * 方法setLoadAnimation(Animation loadAnimation) 设置加载成功动画
    * 方法loadImage() 开始加载图片
    * 方法releaseTag() 释放tag，若出现本地(drawable/mipmap等同步加载方式)与网络混加载图片，请为本地图片加载时调用该方法释放网络tag，避免图片显示错乱


* 接口ImageUtilCallBack
ImageUtil回调接口
    * 方法onStart(ImageView imageView) 开始加载图片时回调
    * 方法onLoadCach(ImageView imageView, Bitmap bitmap) 从内存缓存获得图片时回调
    * 方法onFinish(ImageView imageView, Bitmap bitmap) 获取图片成功时回调
    * 方法onFailed(ImageView imageView, String path) 获取图片失败时回调


* 接口ReadImage
ImageUtil后台读取图片接口
    * 抽象方法readImage(String path, int widthLimit, boolean asGif) 子类重写读取图片方法
    * 已实现的实现类SmartReadImage，读取网络图片，ReadSDCardImage，读取sd卡图片，ReadSDCardVideo，读取sd卡视频缩略图。

## 网络访问框架

* 类HttpClient
    * 方法setTimeOut(int timeOut) 设置http连接超时时间
    * 方法setReadTimeOut(int readTimeOut) 设置读取超时时间
    * 方法post(String urlStr, RequestParams params) post网络请求，返回responce对象
    * 方法get(String urlStr, RequestParams params) get网络请求，返回responce对象
    * 方法put(String urlStr, RequestParams params) put网络请求，返回responce对象
    * 方法delete(String urlStr, RequestParams params) delete网络请求，返回responce对象
    * 方法asyncPost(final String urlStr, final RequestParams params, final AsyncClient<Responce> asyncClient) 异步Post请求，已有AsyncClient实现类DefaultAsyncClient可用
    * 方法asyncGet(final String urlStr, final RequestParams params, final AsyncClient<Responce> asyncClient) 异步Get请求，已有AsyncClient实现类DefaultAsyncClient可用
    * 方法asyncPut(final String urlStr, final RequestParams params, final AsyncClient<Responce> asyncClient) 异步Put请求，已有AsyncClient实现类DefaultAsyncClient可用
    * 方法asyncDelete(final String urlStr, final RequestParams params, final AsyncClient<Responce> asyncClient) 异步Delete请求，已有AsyncClient实现类DefaultAsyncClient可用
    * 方法download(String urlStr, String path, boolean cover) 通过http下载文件，路径为文件夹路径时将以服务器端文件名存储到该文件下，若为文件路径则直接存储，cover为false时先检查服务器端文件与本地文件大小是否一致，不一致方下载文件，否则忽略，cover为true时不检查直接覆盖下载
    * 方法downloadInPeice(String urlStr, String path, long start, long end) 断点下载
    * 方法cancel() 取消当前请求
    * 方法setProgressListener(ProgressListener progressListener) 设置下载或上传文件进度监听
    * 一个对象只能注册一个监听，若需监听多个请求，应实例化多个HttpClient对象避免可能出现的混乱


* 类HttpsClient
基本方法与HttpClient相同
    * 方法setSocketFactory(SSLSocketFactory socketFactory) 设置SSLSocketFactory，用于加载证书
    * 方法setHostName(String hostName) 设置hostname校验，不设置时不校验
    * 方法setSuiteTSLAndroid4(boolean suiteTSLAndroid4) 设置是否为安卓4开启TSL v1.2支持


* 接口ProgressListenerHttpClient
HttpsClient监听器
    * 抽象方法transing(int progress, int speed, long length) 参数为进度，传输速度（单位字节），文件长度（单位字节）
    * 抽象方法failed() 传输失败时被调用
    * 抽象方法finish(String result) 传输完成时被调用，result为服务器返回


* 类RequestParmas HttpClient 
HttpsClient使用参数类
    * 方法setJson(String json) 以json形式上传json，param中参数将自动追加为url中参数，仅对post请求生效
    * 方法setAsJson(boolean asJson) 以json形式上传param中参数，仅对post请求生效
    * 方法fromObject(Object object) 添加参数
    * 方法fromKeyValue(String params) 添加参数
    * 方法addParmas(String key, String value) 添加参数
    * 方法addParmas(String key, FileBody value) 添加参数
    * 方法getParmas() 获取参数类中所有参数
    * 方法getParma(String key) 通过键获取参数类中参数
    * 方法addHead(String name, String value) 添加请求头
    * 方法packetOutParmas() 打包参数，HttpClient类调用
    * 静态方法setDefaultNullToEmpty(boolean defaultNullToEmpty) 是否将上传的参数中null值转换为空串，默认为true


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

## 二次封装

* 注解@GET @POST @PUT @DELETE
    * 网络访问注解封装，类似retrofit，修饰于接口上的方法
    * 支持返回类型原生responce（Responce），字符串（String），对象（将使用Json解释器生成对象）
    * 返回Observable对象时为异步请求，直接返回所需对象将同步请求
    * 内部未内置json解析框架，请在Application中指定Json解释器

* 注解@Url @Path
    * 重新指定url地址/url通配替换

* 注解@BaseUrl
    * 网络访问注解封装，修饰于接口上

* 注解@DefaultHeaders @DefaultParams
    * 网络访问注解封装，修饰于方法上，用于标示请求时传递的默认参数

* 注解@Header @Param @ParamObject @ParamJson
    * 网络访问注解封装，修饰于方法中参数，用于标示请求时传递的参数
    * 分别为添加到header，添加一个参数，抽取对象中所有属性到参数，以Json方式上传（指定传参为json后，其他参数将被拼接到url中）

* 注解@AsJson
    * 修饰于方法上，网络访问参数将以json形式上传，仅对post生效

* 注解@UrlEncode
    * 修饰于方法上，对网络访问参数进行url编码

* 注解@Gzip
    * 修饰于方法上，上传参数进行gzip压缩

* 示例代码

```
@BaseUrl("http://127.0.0.1")
public interface ServerApi {
    @GET("/test")
    @DefaultParams(keys = "key", values = "123")//用于指定固定参数，key，value位置需一一对应
    Observable<Responce> getResult(@Header("token") String token, @Param("id") String id);
    //支持返回类型原生responce（Responce），字符串（String），对象（将使用Json解释器生成对象）
    //请求类型@GET，@POST，@DELETE，@PUT
    //参数@Header，@Param，@ParamObject，@ParamJson
}
```

```
new ApiClient()
    .setBaseUrl("https://www.baidu.com") //可在此设置，也可使用注解，注解优先度较高，会覆盖此处设置
    .setOnRequestListener(new OnRequestListener() {//监听请求的生命周期，可做公共处理
        @Override
        public void onRequestBegin(HttpClient client, ApiRequest request) {
            Log.e("RESULT_", request.getUrl() + " " + request.getParams());
        }

        @Override
        public void onRequestEnd(HttpClient client, ApiResult result) {

        }
    })
    .create(ServerApi.class)
    .getResult("xxxx",1)
    .subscribe(new Observer<Responce>() {
        @Override
        public void success(Responce data) {
            Log.e("RESULT_", data.toString() + " ");
        }

        @Override
        public void failed(int code, Throwable e) {
            Log.e("RESULT_", code + " = " + e);
        }
    });
```

注：网络请求包含多个默认配置
</br>
GET/DELETE请求，默认参数将进行url编码
</br>
POST/PUT，默认参数不进行url编码，参数中带有文件时将使用multipart/form-data进行传参；@AsJson将param组合为json进行提交，
    @ParamJson指定了json参数时，其他参数将拼接到url中进行提交，@ParamJson会覆盖@AsJson使其失效，json传参仅对post生效，post参数中携带文件时无法使用json传参，json相关注解将失效
</br>
框架内部未引入json解析器，请用你使用的json解析器配置json解析，推荐在application中指定

```
//如要使用注解形式网络访问，必须实现Json解释器
ApiClient.setJsonParser(new JsonParser() {
    @Override
    public Object jsonToObject(String json, Class<?> classType) {
        return JSON.parseObject(json, classType);
    }

    @Override
    public String objectToJson(Object object) {
        return JSON.toJSONString(object);
    }
});
```

## view包
##### 以下注解需配合工具类ViewUtil使用

* 注解Find
    * @Find(R.id.xx) 用于查找控件注解
    
* 注解Click
    * @Click(id = R.id.xx, clickSpace = 0) 用于绑定控件点击事件，clickSpace用于限制两次点击间隔时间，默认为0
    
* 注解LongClick
    * @LongClick(id = R.id.xx) 用于绑定控件点击事件
    
* 注解ItemClick
    * @ItemClick(id = R.id.xx) 用于绑定控件点击事件
    
* 注解ItemLongClick
    * @ItemLongClick(id = R.id.xx) 用于绑定控件点击事件


* 类ViewUtil
    * 静态方法find(Object holder) 查找holder中find注解并为控件初始化
    * 静态方法find(Object holder, View view) 查找holder中注解并为控件初始化，需传入父控件view
    * 静态方法click(Object holder) 查找holder中click注解并绑定监听
    * 静态方法click(Object holder, View view) 查找holder中click注解并绑定监听，需传入父控件view
    
##### 配合LororUtilCompiler可使libary在编译期生成代码实现绑定提高效率，若未使用LororUtilCompiler将以反射方式处理注解

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

## sql包


* 注解@Table
    * 数据库表名，指定表名。SQLiteUtil会抽取该注解中name值生成表名。
    
* 注解@Column
    * 数据库条目，指定column。SQLiteUtil会抽取该注解中colume值生成键名。
    * encryption() 可指定加密解密方式，需要参数Class<? extends Encryption>，需继承Encryption实现加密解密方法。
    
* 注解@Id
    * 主键，一个表只能指定一个主键，多主键会抛出异常。SQLiteUtil发现该注解会生成一个自增int型主键。

* 类SQLiteUtil   数据库辅助类
* 注：一个类中只能指定一个主键，主键被内部取名id，因此其他各键名不应再以id命名，下列所有涉及到主键的方法若使用在未定义主键的表中，将抛出异常，请谨慎使用，建议为每张表建立主键。
    * 构造方法SQLiteUtil(Context context, String dbName, Class<?> table, int version, OnChange onChange)  构造sqlite工具，泛型，操作对象类型，参数，1，上下文，2，数据库名，3，操作对象类型，5，版本号，6，回调，数据库创建、更新时回调，可为空。table被传入时创建数据库时将同时创建该表。
    * 方法dropTable(Class<?> table)  删除表
    * 方法createTableIfNotExists(Class<?> table) 如果不存在表时创建表
    * 方法changeTableIfColumnAdd(Class<?> table) 如果表字段增加时插入字段
    * 方法insert(Object entity)  插入对象到数据库
    * 方法lastInsertId(Class<?> table) 获取最后插入主键id
    * 方法delete(Object entity)  删除数据库条目，只删除与对象中所有参数相同的条目
    * 方法deleteById(String id, Class<?> table)  根据主键id删除数据
    * 方法deleteAll(Class<?> table) 清除表中所有数据
    * 方法updateById(Object entity) 更新数据，将根据主键id更新所有数据
    * 方法getAll(Class<T> table) 获取所有条目，返回对象数组
    * 方法count(Class<?> table) 获取总条目数
    * 方法model(Class<T> table, boolean checkTable) 获取条件查询类Model
    * 方法close() 关闭数据库

* 类Model
    * 方法type(int type) 设置类型
    * 方法where(String key, String operator, Object column) 添加and条件(type为0)，追加and条件(type为1)
    * 方法where(String key, Object column) 添加and条件，默认以=构造操作符
    * 方法whereOr(String key, String operator, Object column) 添加or条件(type为0)，追加or条件(type为1)
    * 方法whereOr(String key, Object column) 添加or条件，默认以=构造操作符
    * 方法whereIn(String key, String operator, String... vars) 添加in条件，list为空将出现异常，list的size为1时将自动退化为=，operator支持in/not in
    * 方法orderBy(String key, int orderType) 设置排序条件
    * 方法page(int page, int number) 设置分页，第page页，每页number个
    * 方法get() 查询结果
    * 方法first() 查询第一条结果
    * 方法count() 查询结果数量
    
* 注：上面类所用orderType可用两个参数Order.ORDER_DESC（反序）与Order.ORDER_ASC（正序）
注：用于创建表的类权限需为public，并必须包含无参构造，若为内部类必须为静态内部类

License
-------

    Copyright 2018 Loror

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
