package com.lemon.http

import com.lemon.http.request.HttpMethod

/**
 * Lemon 注解声明文件
 * 以下注释代码说明基于 Koltin ，使用 Java 请参照对应写法。
 * 以下注释说明初始化 apiUrl 值都以 "https://api.test.com" 作参照。
 *
 */

/**
 * 接口 Api @ApiUrl 注解，用于替换初始化的 apiUrl 或追加相对 Path 路径。
 *
 * @param value url 绝对或相对路径。
 *
 * 场景1：value 值为 "https://newapi.test.com"，前缀为 http 或 https 会认为是完整 ApiUrl ，会替换掉初始化的 apiUrl。
 * 示例1：条件(接口类 @ApiUrl 注解 value 值为 "https://newapi.test.com"， @Api 注解 value 值为 "yyy")
 * 未使用 @ApiUrl 场景最终请求 Url 为 "https://api.test.com/yyy"
 * 使用 @ApiUrl 场景最终请求 Url 为 "https://newapi.test.com/yyy"
 *
 * 场景2：value 值为 "xxx/"，前缀不为 http 或 https 会被认为是相对 Path，会与追加到初始化的 apiUrl 之后，再与方法上的 @Api value 进行拼接。
 * 示例2：条件(接口类 @ApiUrl 注解 value 值为 "xxx/"， @Api 注解 value 值为 "yyy")
 * 未使用 @ApiUrl 场景最终请求 Url 为 "https://api.test.com/yyy"
 * 使用 @ApiUrl 场景最终请求 Url 为 "https://api.test.com/xxx/yyy"
 *
 * 注意：@ApiUrl 注解使用相对路径时 value 值请尽量不要以 "/" 开头，因为可能会出现 "https://api.test.com//xxx/yyy" 情况。
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiUrl(
    val value: String = ""
)


/**
 * Api 注解，表示请求以 某种 HttpMethod 方式发起请求，默认为 POST 请求。
 *
 * @param value url 相对路径。
 * @param method Http 请求方式(如：GET、POST等)，忽略大小写，@see ApiMethod。
 * @param headers 请求头参数数组。
 * @param isMultipart 是否使用 multipart/form-data 方式提交数据。
 *
 * 示例：
 * @Api(value = "xxx/yyy",method = "POST",headers = ["X-CALL-ID:call123", "X-Token:token123456"],isMultipart = true)
 *
 * 注意：value 值请尽量不要以 / 开头，因为可能会出现 "https://api.test.com//xxx/yyy" 情况。
 * headers 请确保请求头名称是否重复添加，如果重复添加后，相同的请求头名称对应的值会使用 ';' 进行拼接作合并处理。
 *
 * 扩展：动态请求头请使用 @ApiHeader 注解。
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Api(
    val value: String = "",
    val method: HttpMethod = HttpMethod.POST,
    val headers: Array<String> = [],
    val isMultipart: Boolean = false
)

/**
 * 数据对象序列化转换注解，该注解会被 Converter 进行序列化转换。
 * 场景：例如可用于 Json 数据上报场景，Converter 可参考测试用例代码 GsonConverterFactory
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiBody

/**
 * 请求单个参数注解，用于 GET 请求参数或 POST Form 表单参数。
 * @param value 参数约定名称，Map 参数类型可为空
 * @param encoded 实参是否进行 URL 编码 (true:是，false：否)，默认为 false
 *
 * 注意：GET 请求方式会将参数追加拼接到 url 后；POST 请求方式会以 FormBody 表单形式进行提交。
 * 参数类型建议使用 String 、Map、基础类型，其它类型请自实现 toString 方法。
 * Map 参数类型必须添加泛型 Map&lt String, T&gt (T：String 或基础类型，其它类型请自实现 toString 方法)类型。
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiField(
    val value: String = "",
    val encoded: Boolean = false
)

/**
 * 单个动态请求头添加注解，该注解用于方法参数上，用于单个追加请求头参数。
 * @param value 请求头参数名称，Map 参数类型可为空
 *
 * 注意：参数类型支持使用 String 、Map、基础类型，其它类型请自实现 toString 方法。
 * Map 参数类型必须添加泛型 Map&lt String, T&gt (T：String 或基础类型，其它类型请自实现 toString 方法)类型。
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiHeader(val value: String = "")

/**
 * 请求 url 替换注解，用于动态替换 url 中占位符。
 *
 * @param value 占位符名称(与@ApiPath 注解的 value 值对应)，格式：{占位符名称}
 * @param encoded 实参是否需要被 URL 编码 (true:是，false：否)，默认为 false
 *
 * 示例：条件(@Api value 值为 "xxx/{key}/yyy")
 * 使用 fun xxx(@ApiPath("key") path:String), path 为 "abc"，最终的请求 url 为 "https://api.test.com/xxx/abc/yyy"
 *
 * 注意：参数类型建议使用 String 或基础类型，其它类型请自实现 toString 方法。
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiPath(
    val value: String,
    val encoded: Boolean = false
)

/**
 * 请求体部件注解，用于标识该参数为请求体部件
 *
 * @param value 部件名称,默认为空串，参数类型为 MultipartBody.Part 时，设置无效
 * @param encoding 部件内容传输编码，默认为二进制，参数类型为 MultipartBody.Part 时，设置无效
 *
 * 示例：fun xxx(@ApiPart part:Part 或 @ApiPart("fileKey") file:File )
 *
 * 注意：参数类型支持 File 、ReuqstBody 、MultipartBody.Part 、String 、Map 类型，其它类型会转换成 String，请自实现 toString 方法，
 * Map 必须声明泛型且 key 必须为 String，值为部件名称；Map value 的参数类型支持 File 、ReuqstBody 、MultipartBody.Part 、String 类型，
 * 其它类型会转换成 String，请自实现 toString 方法
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiPart(
    val value: String = "",
    val encoding: String = ""
)
