# 简介
Lemon 是一款针对于 Android 使用注解形式使接口适用于 Http 请求的轻量网络请求库,内部请求 Client 客户端默认使用 HttpURLConnection。本库主要参考 Retorfit 和 Okhttp。

Lemon 可用于 SDK 网络请求库，如果你习惯使用注解形式接口的 Http 请求，可以选择此库。

# 特性

### 支持

* 支持接口注解形式
* 支持 GET、HEAD、OPTIONS、POST、PUT、PATCH、DELETE 请求方法
* 支持表单、multipart/form-data、application/json
* 支持自定义请求拦截器、序列化转换器、返回类型适配器，默认提供日志拦截器
* 支持自定义请求 Client 客户端，默认提供 LemonClient（使用HttpURLConnection）, LemonClient 支持自定义 SSL，默认不校验证书
* 默认支持同步调用，线程调度建议使用协程控制，也可以使用 LemonSpace 封装类进行线程调度、生命周期管理、进度显示控制
* 支持 Gzip 自动解压，可根据需求自行添加。

### 不支持
* 不支持取消执行中的请求
* 暂不支持 Cookie 管理
* 暂不支持缓存
* 暂不支持文件下载
* 不支持 TRACE 请求方式，存在风险

# 说明文档

### 依赖

```
//核心(必选)
implementation 'com.github.Walkud.Lemon:core:0.1.4'

//日志输出(可选)
implementation 'com.github.Walkud.Lemon:log:0.1.4'
//LemonSpace(可选)
implementation 'com.github.Walkud.Lemon:space:0.1.4'


```

2022 年 6 月已使用该库替换掉 [JudyKotlinMvp](https://github.com/Walkud/JudyKotlinMvp) 仿猫眼的实际项目中的 RxJava + Retrofit + OkHttp。

### 简单使用

```
//第一步：初始化 Lemon
private val lemon = Lemon.build {
    //(必选)设置 ApiBaseUrl
    setApiUrl("https://api.test.com")
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

//第四步：发起请求(请在异步线程中执行)
val result = apiService.submitContent("Hello Lemon")
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

详细接口定义注解说明请参考 LemonAnnotation.kt 文件。[传送门](./lemon/src/main/java/com/lemon/core/LemonAnnotation.kt)

### 原理说明及高级用法

[传送门](./material/LemonAdvanced.md)

### 实现的原因
个人认为接口注解形式是客户端对服务端接口最好的一种表达方式，所以参考了 Retorfit 和 Okhttp。以前的开发中一直使用 Retrofit + Okhttp + RxJava，中途开发 SDK 时也使用相同的技术栈，觉得整个使 SDK 太重且容易与应用依赖的版本冲突，但 SDK 又觉得简单对 HttpURLConnection 进行封装太简陋（强迫症+完美主义），找了一圈，未找到一款轻量、接口注解形式且使用 HttpURLConnection 的网络库，索性自己造轮子，这就是 Lemon 产生的原因。

有人会疑惑，Retorfit + OkHttp 已经非常完美了，性能也比 HttpURLConnection 强，已经成为了 Android 开发标配，同时现在 Android 也没以前火了，为啥还要造一个轮子。首先我也认为 Retorfit 最佳搭配，不管是使用上还是内部代码解耦都非常牛逼，因为我太喜欢 Retorfit 所以通过站在巨人的肩膀上学习，学习不能看到什么火就学什么，反而应该持续深耕，正好在开发 SDK 中遇到了一些疑问，正好立个目标，参考 Retorfit 重新使用 Kotlin 实现一下，动手还是比较重要的；其次 Android 早在 Kitkat 4.4 (Api Level 19) 版本源码中就已经添加了 OkHttp，可以查看源码 ，但手上已经没有该系统版本的手机了，无法印证，但使用 Android 10 手机通过断网发起请求后，根据抛出的异常栈信息判断底层确实使用的是 OkHttp 的 HttpEngine，只是依赖的 OkHttp2 ，且支持能力有限(源码注释来看只支持Http/1.1， 而 SPDY 和 Http/2.0 被禁用了)。

[HttpURLConnection 与 OkHttp 调用栈](./material/OkHttpRelation.md)

### 学到了什么
* 重新认识了 Retrofit 和 OkHttp
* 对 Http 进一步认识，对 Multipart 数据结构有了更清晰的认识
* 对部分开发模式有了进一步的了解。
* 对 HttpURLConnection 使用有了进一步的了解。
* 对 Kotlin 协程有了全新的认识，直接影响我对 RxJava 的依赖。

# 时间线
整个过程中对细节进行打磨。

* 2023-09-21
	* 修复方法动态添加请求头多次调用出现多个请求头 Bug
* 2023-09-20
	* 修改 SDK 支持的最小版本号，并统一模块的 SDK 版本配置
* 2022-06-30
	* 添加 Gzip 自动解压支持
	* 优化请求头忽略大小写
* 2022-06-16
   * 修复请求头 Host 构建 Bug
   * 优化 Request 数据结构
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
