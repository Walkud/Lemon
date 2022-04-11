# 简介
Tube 是一款针对于 Android 使用注解形式使接口适用于 Http 请求的网络请求库。本库主要参考 Retorfit 和 Okhttp，但并未按照 Restful 规范实现，主要用途是开发 SDK，所以仅实现部分基础功能，当然也可以用于主程序中。

### 实现的原因
个人认为接口注解形式是客户端对服务端接口最好的一种表达方式，所以参考了 Retorfit 和 Okhttp。之前一直使用 Retrofit + Okhttp +RxJava，觉得整个使 SDK 太重且容易与应用依赖的版本冲突，又觉得简单对 HttpURLConnection 进行封装太简陋，找了一圈，未找到一款轻量、接口注解形式且使用 HttpURLConnection 的网络库，索性自己实现一套，这就是 Tube 产生的原因。

# 说明文档
待完善

# TODO

* Http 请求日志拦截器
* 支持 Multipart 数据 (已支持)
* 验证 sdk 依赖是否会传递
* 研究协程方式调用的封装
* 待补充