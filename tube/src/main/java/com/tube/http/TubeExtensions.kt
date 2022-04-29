package com.tube.http

import com.tube.http.request.body.FormBody
import com.tube.http.request.body.MultipartBody
import java.io.*
import java.lang.StringBuilder
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType
import java.net.URL

/**
 * Describe: 扩展文件
 * Created by liya.zhu on 2022/3/2
 */

inline fun <reified T : Any> Tube.create(): T = create(T::class.java)

/**
 * 判断是否为 Http 或 Https 协议
 */
internal fun String.isHttpProtocol() = this.startsWith("http", true)
        || this.startsWith("https", true)

/**
 * 追加 URI Path 部分
 */
internal fun String.appendPath(vararg urlPath: String): String {
    val pathSeparator = "/"
    val baseSeparator = if (this.endsWith(pathSeparator)) "" else pathSeparator

    val pathSb = StringBuilder(this)
    pathSb.append(baseSeparator)
    for (path in urlPath) {
        pathSb.append(path)
    }

    return pathSb.toString()
}

/**
 * 将字符串转换为 URL
 */
internal fun String.toURL() = URL(this)

/**
 * 检测是否为默认方法
 */
internal fun Method.checkDefault() =
    android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N && this.isDefault

/**
 * 获取方法的类名称.方法名字符串
 */
internal fun Method.referenceName() = "${declaringClass.simpleName}.${name}"

/**
 * 判断 Class 是否不为无效类型
 */
internal fun Class<*>.isInvalidParameterType() = !isValidParameterType()

/**
 * 判断 Class 是否为有效类型
 */
internal fun Class<*>.isValidParameterType() =
    this.isPrimitive || Any::class.java.isAssignableFrom(this)

/**
 * 是否为 MultipartBody.Part 类型
 */
internal fun Class<*>.isPartType() = MultipartBody.Part::class.java.isAssignableFrom(this)

/**
 * 检测是否不为无效泛型类型，有效：例如 <String,String> 、<String,Any> 等
 */
internal fun Type.isInvalidGenericParameterType() = !isValidGenericParameterType()

/**
 * 检测泛型类型是否有效
 */
internal fun Type.isValidGenericParameterType(): Boolean {
    if (this is ParameterizedType) {
        val types = actualTypeArguments
        if (types.size == 2) {
            val rawType1 = types[0].asRawType()
            val rawType2 = types[1].asRawType()
            return rawType1 === String::class.java
                    && Any::class.java.isAssignableFrom(rawType2)
        }
    }
    return false
}

/**
 * 判断 Class 是否不为 Map 类型
 */
internal fun Type.isMapParameterizedType() =
    this is ParameterizedType && Map::class.java.isAssignableFrom(this.rawType as Class<*>)

/**
 * 获取 Type 类型的类或接口类型，例如:List<String> 返回 List
 */
internal fun Type.asRawType(): Class<*> {
    return when (this) {
        is Class<*> -> this
        is ParameterizedType -> {
            return this.rawType as Class<*>
        }
        is WildcardType -> {
            return this.upperBounds[0].asRawType()
        }
        else -> {
            throw IllegalArgumentException("The ${this::class.java.name} is not Class<*> or ParameterizedType type!")
        }
    }
}

/**
 * 将FormBody 转换为 Get 请求 Query 参数
 */
internal fun FormBody.convertToQuery(): String {
    val ops = ByteArrayOutputStream()
    writeTo(ops)
    return String(ops.toByteArray())
}

/**
 * 尝试关闭输出流
 */
internal fun OutputStream.tryClose() {
    try {
        close()
    } catch (t: Throwable) {
    }
}

/**
 * 尝试关闭输入流
 */
internal fun InputStream.tryClose() {
    try {
        close()
    } catch (t: Throwable) {
    }
}

/**
 * 读取文件流并写入 OutputStream
 */
internal fun FileInputStream.readTo(outputStream: OutputStream) {
    var len: Int
    val buffer = ByteArray(4096)
    while (read(buffer).also { len = it } != -1) {
        outputStream.write(buffer, 0, len)
        outputStream.flush()
    }
}