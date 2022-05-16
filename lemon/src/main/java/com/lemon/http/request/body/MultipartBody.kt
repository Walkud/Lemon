package com.lemon.http.request.body

import com.lemon.http.LemonUtils
import com.lemon.http.request.ContentType
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream

/**
 * Describe:多部件请求 Body
 * multipart/form-data 请求格式
 * 假设 boundary 值为 xxxxxxxxxx，值为任意字符；普通参数约定字段为 aaa，值为 123456；文件约定字段为 textFile ，文件内容为 abcdefg，文件名称：abc.txt
 * 1、请求头
 * Content-Type:multipart/form-data;boundary=xxxxxxxxxxx ，boundary 为必须
 *
 * 2、消息体
 * --xxxxxxxxxx                                                     //分界线，两个破折号(--) 加上 boundary 值（必须）
 * Content-Disposition:form-data; name="aaa"                        //普通参数（必须）
 * Content-Length: 6                                                //普通参数值长度(可选)
 * \r\n                                                             //换行符（必须）
 * 123456                                                           //普通参数值（必须）
 * --xxxxxxxxxx                                                     //分界线，两个破折号(--) 加上 boundary 值（必须）
 * Content-Disposition:form-data; name="aaa"; filename="abc.txt"    //文件（必须）
 * Content-Type:text/plain                                          //文件类型(可选)
 * Content-Transfer-Encoding:binary                                 //传输内容编码(可选)
 * Content-Length: 6                                                //普通参数值长度(可选)
 * \r\n                                                             //换行符（必须）
 * abcdefg                                                          //文件内容（必须）
 * --xxxxxxxxxx--                                                   //结束分界线，两个破折号(--) 加上 boundary 值加上两个破折号(--)（必须）
 * \r\n                                                             //结束换行符（必须）
 *
 * Created by liya.zhu on 2022/4/6
 */
class MultipartBody private constructor(
    val boundary: String,
    val parts: List<Part>
) : RequestBody() {

    private val contentType by lazy {
        ContentType.MULTIPART_FORM_DATA.addParameter("boundary", boundary)
    }

    companion object {
        private val CRLF = "\r\n".toByteArray()
        private val DASH = "--".toByteArray()
    }

    override fun measureContentLength(): Long {
        return ByteArrayOutputStream().let {
            writeTo(it)
            it.size().toLong()
        }
    }

    override fun contentType() = contentType

    override fun writeTo(outputStream: OutputStream) {
        for (part in parts) {
            val body = part.requestBody

            outputStream.write(DASH)
            outputStream.write(boundary.toByteArray())
            outputStream.write(CRLF)

            val fileNameSb = StringBuilder()
            part.fileName?.let { fileName ->
                fileNameSb.append("; filename=\"")
                fileNameSb.append(fileName)
                fileNameSb.append("\"")
            }

            val disposition = "Content-Disposition:form-data; name=\"${part.name}\"$fileNameSb"
            outputStream.write(disposition.toByteArray())
            outputStream.write(CRLF)

            body.contentType()?.let { contentType ->
                outputStream.write("Content-Type:${contentType.value}".toByteArray())
                outputStream.write(CRLF)
            }

            part.encoding?.takeIf { it.isNotEmpty() }?.let { encoding ->
                val transferEncoding = "Content-Transfer-Encoding:$encoding"
                outputStream.write(transferEncoding.toByteArray())
                outputStream.write(CRLF)
            }

            val contentLength = body.contentLength()
            contentLength.takeIf { it != 1L }?.let { length ->
                outputStream.write("Content-Length: $length".toByteArray())
                outputStream.write(CRLF)
            }

            outputStream.write(CRLF)
            body.writeTo(outputStream)
            outputStream.write(CRLF)
        }

        outputStream.write(DASH)
        outputStream.write(boundary.toByteArray())
        outputStream.write(DASH)
        outputStream.write(CRLF)
    }

    fun newBuilder() = Builder(boundary, parts.toMutableList())

    class Builder(
        private val boundary: String = LemonUtils.getRandomUUID32(),
        private val parts: MutableList<Part> = mutableListOf()
    ) {

        fun addPart(part: Part) {
            parts.add(part)
        }

        fun addPart(
            name: String,
            encoding: String? = null,
            contentType: ContentType? = null,
            file: File
        ) {
            addPart(Part.create(name, encoding, contentType, file))
        }

        fun addPart(
            name: String,
            encoding: String? = null,
            requestBody: RequestBody
        ) {
            addPart(Part.create(name, null, encoding, requestBody))
        }

        fun addPart(
            name: String,
            encoding: String? = null,
            content: String
        ) {
            addPart(name, encoding, create(content, null))
        }

        fun build() = MultipartBody(boundary, parts)
    }

    class Part private constructor(
        val name: String,
        val fileName: String?,
        val encoding: String?,
        val requestBody: RequestBody
    ) {
        companion object {

            fun create(
                name: String,
                encoding: String? = null,
                contentType: ContentType? = null,
                file: File
            ): Part {
                return create(
                    name,
                    file.name,
                    encoding,
                    create(contentType, file)
                )
            }

            fun create(
                name: String,
                fileName: String? = null,
                encoding: String? = null,
                requestBody: RequestBody
            ) = Part(name, fileName, encoding, requestBody)

        }
    }
}