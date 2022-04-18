# 简介
Tube 是一款针对于 Android 使用注解形式使接口适用于 Http 请求的网络请求库。本库主要参考 Retorfit 和 Okhttp。

### 实现的原因
个人认为接口注解形式是客户端对服务端接口最好的一种表达方式，所以参考了 Retorfit 和 Okhttp。之前一直使用 Retrofit + Okhttp +RxJava，觉得整个使 SDK 太重且容易与应用依赖的版本冲突，又觉得简单对 HttpURLConnection 进行封装太简陋，找了一圈，未找到一款轻量、接口注解形式且使用 HttpURLConnection 的网络库，索性自己实现一套，这就是 Tube 产生的原因。

# 说明文档
待完善

# TODO

* Http 请求日志拦截器 (已支持 2022年4月14日)
* 支持 Multipart 数据 (已支持 2022年4月8日)
* 验证 sdk 依赖是否会传递(已验证，会传递依赖，已通过修改Api和注解名称避免冲突，2022年4月11日)
* 研究协程方式调用的封装
* 支持类似 Chuck 功能，便于手机本地查看请求日志
* 支持 Gzip 压缩
* 分离 Disposer