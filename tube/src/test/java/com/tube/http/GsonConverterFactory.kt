package com.tube.http

import com.google.gson.Gson
import com.tube.http.converter.Converter
import com.tube.http.request.ContentType
import com.tube.http.request.body.RequestBody
import com.tube.http.request.body.ResponseBody
import java.lang.reflect.Method
import java.lang.reflect.Type

/**
 * Describe:
 * Created by liya.zhu on 2022/3/25
 */
class GsonConverterFactory(val gson: Gson = Gson()) : Converter.Factory {

    override fun requestBodyConverter(
        type: Type,
        originMethod: Method
    ): Converter<*, RequestBody> {
        return object : Converter<Any, RequestBody> {
            override fun convert(value: Any): RequestBody {
                return RequestBody.create(gson.toJson(value, type), ContentType.JSON)
            }
        }
    }

    override fun responseBodyConverter(
        type: Type,
        originMethod: Method
    ): Converter<ResponseBody, *> {
        return object : Converter<ResponseBody, Any> {
            override fun convert(value: ResponseBody): Any {
                val charset = value.contentType()?.let {
                    it.getCharset()
                } ?: Charsets.UTF_8
                return gson.fromJson(String(value.byteArray(), charset), type)
            }

        }
    }
}