package com.tube.http.request

import com.tube.http.request.body.MultipartBody
import java.nio.charset.Charset
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Describe: 参考RFC 2045 Media Type 、参考OkHttp
 * Created by liya.zhu on 2022/3/4
 *
 * @see <a href="http://tools.ietf.org/html/rfc2045">RFC 2045</a>
 * @see <a href="https://github.com/square/okhttp">OkHttp</a>
 */
class ContentType(
    val value: String,
    val type: String,
    val subType: String,
    val parameterMap: Map<String, String>
) {

    companion object {
        private const val SEMICOLON_DASH = ";"
        private const val REG_TYPE = "([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)"
        private const val REG_PARAMETER = "$SEMICOLON_DASH\\s*(?:$REG_TYPE=($REG_TYPE))?"

        private val TYPE_PATTERN = Pattern.compile("$REG_TYPE/$REG_TYPE")
        private val PARAMETER_PARTTERN = Pattern.compile(REG_PARAMETER)
        val DEFAULT = ContentType("", "", "", mapOf())
        val FROM by lazy {
            parse("application/x-www-form-urlencoded")
        }
        val JSON by lazy {
            parse("application/json; charset=UTF-8")
        }
        val MULTIPART_FORM_DATA by lazy {
            parse("multipart/form-data")
        }

        /**
         * 通过正则表达式解析 Content-Type
         */
        fun parse(contentType: String): ContentType {
            val typeMatcher: Matcher = TYPE_PATTERN.matcher(contentType)

            val argException = IllegalArgumentException("Check type format for $contentType!")

            if (!typeMatcher.lookingAt()) throw argException

            val type = typeMatcher.group(1) ?: throw argException
            val subType = typeMatcher.group(2) ?: throw argException

            val parameterMap = mutableMapOf<String, String>()
            val parameterMatcher = PARAMETER_PARTTERN.matcher(contentType)

            parameterMatcher.region(typeMatcher.end(), contentType.length)

            while (parameterMatcher.find()) {
                val attribute = parameterMatcher.group(1)
                val value = parameterMatcher.group(2)
                if (attribute != null && value != null) {
                    parameterMap[attribute.lowercase()] = value.lowercase()
                } else {
                    break
                }
            }

            return ContentType(contentType, type.lowercase(), subType.lowercase(), parameterMap)
        }
    }

    fun getCharset(defaultCharset: Charset = Charsets.UTF_8): Charset {
        return try {
            val charset = parameterMap["charset"]
            return Charset.forName(charset)
        } catch (e: Exception) {
            defaultCharset
        }
    }

    fun addParameter(attribute: String, parameterValue: String): ContentType {
        var dash = ""
        if (!parameterValue.endsWith(SEMICOLON_DASH) && !attribute.startsWith(SEMICOLON_DASH)) {
            dash = SEMICOLON_DASH
        }

        return parse("${value}$dash$attribute=$parameterValue")
    }
}