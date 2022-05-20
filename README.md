# 简介
Lemon 是一款针对于 Android 使用注解形式使接口适用于 Http 请求的网络请求库,内部 client 默认使用 HttpURLConnection。本库主要参考 Retorfit 和 Okhttp。

### 实现的原因
个人认为接口注解形式是客户端对服务端接口最好的一种表达方式，所以参考了 Retorfit 和 Okhttp。以前的开发中一直使用 Retrofit + Okhttp + RxJava，中途开发 SDK 时也使用相同的技术栈，觉得整个使 SDK 太重且容易与应用依赖的版本冲突，但 SDK 又觉得简单对 HttpURLConnection 进行封装太简陋（强迫症+完美主义），找了一圈，未找到一款轻量、接口注解形式且使用 HttpURLConnection 的网络库，索性自己造轮子，这就是 Lemon 产生的原因。

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
val apiService = lemon.create(xxx::class.java)

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



# TODO

* Http 请求日志拦截器 (已支持 2022年4月14日)
* 支持 Multipart 数据 (已支持 2022年4月8日)
* 验证 sdk 依赖是否会传递(已验证，会传递依赖，已通过修改Api和注解名称避免冲突，2022年4月11日)
* 研究协程方式调用的封装 (已添加简单封装 2022年5月19日)
* 支持类似 Chuck 功能，便于手机本地查看请求日志
* 支持 Gzip 压缩
* 分离 Disposer (已完成 2022年4月29日)
* 改名 (已完成，2022年5月16日)
* 添加 LemonSpace 封装类，可用于 UI 生命周期绑定、UI 进度切换、自动切换 UI 与 IO 线程场景 (2022年5月19日)