package com.tube.http

/**
 * Tube 注解声明文件
 * 以下注释代码说明基于 Koltin ，使用 Java 请参照对应写法。
 * 以下注释说明初始化 baseUrl 值都以 "https://api.test.com" 作参照。
 *
 */

/**
 * 接口 Api @BaseUrl 注解，用于替换初始化的 baseUrl 或追加相对 Path 路径。
 *
 * @param value url 绝对或相对路径。
 *
 * 场景1：value 值为 "https://newapi.test.com"，前缀为 http 或 https 会认为是完整 BaseUrl ，会替换掉初始化的 baseUrl。
 * 示例1：条件(接口类 @BaseUrl 注解 value 值为 "https://newapi.test.com"， 方法 @GET 或 @POST 注解 value 值为 "yyy")
 * 未使用 @BaseUrl 场景最终请求 Url 为 "https://api.test.com/yyy"
 * 使用 @BaseUrl 场景最终请求 Url 为 "https://newapi.test.com/yyy"
 *
 * 场景2：value 值为 "xxx/"，前缀不为 http 或 https 会被认为是相对 Path，会与追加到初始化的 baseUrl 之后，再与方法上的 @GET 或 @POST 注解 value 进行拼接。
 * 示例2：条件(接口类 @BaseUrl 注解 value 值为 "xxx/"， 方法 @GET 或 @POST 注解 value 值为 "yyy")
 * 未使用 @BaseUrl 场景最终请求 Url 为 "https://api.test.com/yyy"
 * 使用 @BaseUrl 场景最终请求 Url 为 "https://api.test.com/xxx/yyy"
 *
 * 注意：@BaseUrl 注解使用相对路径时 value 值请尽量不要以 "/" 开头，因为可能会出现 "https://api.test.com//xxx/yyy" 情况。
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class BaseUrl(
    val value: String = ""
)

/**
 * HttpMethod 方式注解，表示请求以 GET 方式发起。
 *
 * @param value url 相对路径。
 *
 * 例如："xxx/yyy"
 *
 * 注意：@GET 注解使用时 value 值请尽量不要以 / 开头，因为可能会出现 "https://api.test.com//xxx/yyy" 情况。
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class GET(
    val value: String = ""
)

/**
 * HttpMethod 方式注解，标识请求以 POST 方式发起。
 *
 * @param value url 相对路径。
 *
 * 例如："xxx/yyy"
 *
 * 注意：@POST 注解使用时 value 值请尽量不要以 / 开头，因为可能会出现 "https://api.test.com//xxx/yyy" 情况。
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class POST(
    val value: String = ""
)

/**
 * 请求头参数添加注解，该注解用于方法函数上，用于批量追加请求头参数。
 *
 * @param value 请求头参数数组
 *
 * 示例：@Headers(["Content-Type:application/json;charset=utf-8", "X-Token:token123456"])
 *
 * 注意：使用时请确保请求头名称是否重复添加，如果重复添加后，相同的请求头名称对应的值会使用 ';' 进行拼接作合并处理。
 * 扩展：动态请求头请使用 @Header 或 @HeaderMap 注解。
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Headers(val value: Array<String>)

/**
 * 多部件请求体注解，该注解用于方法函数上，用于请求体包括普通参数和文件场景，使用该注解后，消息体会使用 MultipartBody 进行参数组装。
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Multipart

/**
 * 数据对象序列化转换注解，该注解会被 Converter 进行序列化转换。
 * 场景：例如可用于 Json 数据上报场景，Converter 可参考测试用例代码 GsonConverterFactory
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Body

/**
 * 请求单个参数注解，用于 GET 请求参数或 POST Form 表单参数。
 * @param value 参数约定名称
 * @param encoded 实参是否需要被 URL 编码 (true:是，false：否)，默认为 false
 *
 * 注意：GET 请求方式会将参数追加拼接到 url 后；POST 请求方式会以 FormBody 表单形式进行提交。
 * 参数类型建议使用 String 或基础类型，其它类型请自实现 toString 方法。
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Field(
    val value: String,
    val encoded: Boolean = false
)

/**
 * 请求批量参数注解，用于 GET 请求参数或 POST Form 表单参数。
 * @param encoded 实参是否需要被 URL 编码 (true:是，false：否)，默认为 false
 *
 * 注意：GET 请求方式会将参数追加拼接到 url 后；POST 请求方式会以 FormBody 表单形式进行提交。
 * 参数类型仅支持 Map&ltString, T&gt (T：String 或基础类型，其它类型请自实现 toString 方法)类型。
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class FieldMap(
    val encoded: Boolean = false
)

/**
 * 单个动态请求头添加注解，该注解用于方法参数上，用于单个追加请求头参数。
 * @param value 请求头参数名称，
 *
 * 注意：参数类型建议使用 String 或基础类型，其它类型请自实现 toString 方法。
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Header(val value: String)

/**
 * 批量动态请求头添加注解，该注解用于方法参数上，用于批量追加请求头参数。
 *
 * 注意：参数类型仅支持 Map&ltString, T&gt (T：String 或基础类型，其它类型请自实现 toString 方法)类型。
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class HeaderMap

/**
 * 请求 url 替换注解，用于动态替换 url 中占位符。
 *
 * @param value 占位符名称(与@Path 注解的 value 值对应)，格式：{占位符名称}
 * @param encoded 实参是否需要被 URL 编码 (true:是，false：否)，默认为 false
 *
 * 示例：条件(方法 @GET 或 @POST 注解 value 值为 "xxx/{key}/yyy")
 * 使用 fun xxx(@Path("key") path:String), path 为 "abc"，最终的请求 url 为 "https://api.test.com/xxx/abc/yyy"
 *
 * 注意：参数类型建议使用 String 或基础类型，其它类型请自实现 toString 方法。
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Path(
    val value: String,
    val encoded: Boolean = false
)

/**
 * 请求体部件注解，用于标识该参数为请求体部件
 *
 * @param value 部件名称,默认为空串，参数类型为 MultipartBody.Part 时，设置无效
 * @param encoding 部件内容传输编码，默认为二进制，参数类型为 MultipartBody.Part 时，设置无效
 *
 * 示例：fun xxx(@Part part:Part 或 @PartMap("fileKey") file:File )
 *
 * 注意：参数类型支持 File 、ReuqstBody 、MultipartBody.Part 、String 类型，其它类型会转换成 String，请自实现 toString 方法
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Part(
    val value: String = "",
    val encoding: String = "binary"
)

/**
 * 请求体多部件注解，用于标识多个参数请求体部件
 *
 * @param encoding 部件内容传输编码，默认为二进制
 *
 * 示例：fun xxx(@PartMap partMap:Map<String,File> 或 @PartMap partMap:Map<String,Any> )
 *
 * 注意：Map key 必须为 String，值为部件名称, Map value 的参数类型支持 File 、ReuqstBody 、MultipartBody.Part 、String 类型，
 * 其它类型会转换成 String，请自实现 toString 方法
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class PartMap(
    val encoding: String = "binary"
)
