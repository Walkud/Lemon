# 简介
Lemon 是一款针对于 Android 使用注解形式使接口适用于 Http 请求的轻量网络请求库,内部 client 默认使用 HttpURLConnection。本库主要参考 Retorfit 和 Okhttp。

Lemon 可用于 SDK 网络请求库，如果你习惯使用注解形式接口的 Http 请求，可以选择此库。

### 实现的原因
个人认为接口注解形式是客户端对服务端接口最好的一种表达方式，所以参考了 Retorfit 和 Okhttp。以前的开发中一直使用 Retrofit + Okhttp + RxJava，中途开发 SDK 时也使用相同的技术栈，觉得整个使 SDK 太重且容易与应用依赖的版本冲突，但 SDK 又觉得简单对 HttpURLConnection 进行封装太简陋（强迫症+完美主义），找了一圈，未找到一款轻量、接口注解形式且使用 HttpURLConnection 的网络库，索性自己造轮子，这就是 Lemon 产生的原因。

### 学到了什么
* 重新认识了 Retrofit 和 OkHttp
* 对 Http 进一步认识，对 Multipart 数据结构有了更清晰的认识
* 对部分开发模式有了进一步的了解。
* 对 HttpURLConnection 使用有了进一步的了解。
* 对 Kotlin 协程有了全新的认识，直接影响我对 RxJava 的依赖。

# 说明文档

### 简单使用

```
//第一步：初始化 Lemon
private val lemon = Lemon.build {
    //(必选)设置 ApiBaseUrl
    setApiUrl("https://api.test.com”)
    //(可选)添加转换工厂
    addConverterFactory(……)
    //(可选)添加请求拦截器
    addInterceptor(……)
    //(可选)设置 HttpClient，内部默认使用LemonClient，使用HttpURLConnection
    setHttpClient(……)
}

//第二步：定义接口类，暂定接口类名为 ApiService
interface ApiService {
    @Api("xxx/xxx") 
    fun submitContent(@ApiField("content") content: String): String
}

//第三步：创建 ApiService 动态代理类, xxx 为 Api 接口定义类
val apiService = lemon.create<ApiService>()

//第四步：发起请求(异步执行)
val result = apiService. submitContent("Hello Lemon")
```

### 接口定义说明

```
@ApiUrl：作用于类，表示接口绝对 Path 路径或统一的 Api 相对路径。
@Api：作用于方法，表示该方法为一个 Api 接口。
@ApiBody：作用于方法参数，表示请求消息体，用于使用 application/json 类型提交参数字段。
@ApiField：作用于方法参数，表示一个 Api 接口参数字段，不管是 GET 或 POST 方式都用此字段。
@ApiHeader：作用于方法参数，用于添加请求头一个或多个参数。
@ApiPath：作用于方法参数，用于动态替换 url 中占位符。
@ApiPart：作用于方法参数，表示一个 Api 接口参数字段，该字段为普通参数或为文件，用于使用 multipart/form-data 类型提交参数字段。

```

详细接口定义注解说明请参考 LemonAnnotation.kt 文件。[传送门](./lemon/src/main/java/com/lemon/http/LemonAnnotation.kt)

### 原理说明及高级用法

[传送门](./material/LemonAdvanced.md)

# TODO

* 默认支持 Gzip 压缩
* 支持类似 Chuck 功能，便于手机本地查看请求日志

# 时间线
整个过程中对细节进行打磨。

* 2022-05-19 
	* 添加 LemonSpace 协程方式简易封装，可用于 UI 生命周期绑定、UI 进度切换、自动切换 UI 与 IO 线程场景
	* 移除 Disposer 模块
	* 第一版基本成型
*  2022-05-16 
	*  Tube 改名为 Lemon
* 2022-04-29 
	* 分离 Disposer 模块
*  2022-04-14 
	*  添加 Http 请求日志拦截器
* 2022-04-11 
	* TubeHttp 改名为 Tube
	* 修改 Api 及注解名称，避免名称冲突
* 2022-04-08 
	* 支持 Part 、PartMap 注解 支持 multipart/form-data 数
* 2022-03-31 
	* 添加 git 管理
* 2022-03-14 
	* 创建 Disposer
* 2022-03-11 
	* 简易版成型
* 2022-03-02 
	* 创建项目
