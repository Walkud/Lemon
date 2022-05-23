package com.lemon.http

import com.lemon.http.bean.BaseResult
import com.lemon.http.bean.ReqBody
import com.lemon.http.bean.ServerTime
import com.lemon.http.request.HttpMethod
import com.lemon.http.request.body.MultipartBody
import com.lemon.http.request.body.RequestBody
import java.io.File

/**
 * Describe:
 * Created by liya.zhu on 2022/3/3
 */
@ApiUrl("lemon/")
interface KotlinApiService {

    @Api(
        value = "getServerTime",
        method = HttpMethod.GET,
        headers = ["X-CALL-ID:call123", "X-Token:token123456"]
    )
    fun getServerTime(): BaseResult<ServerTime>

    @Api("commitClientTime?createTime=10002345", method = HttpMethod.GET)
    fun commitClientTime(@ApiField("time") time: Long): BaseResult<Void>

    @Api("post/{queryKey}")
    fun postQuery(@ApiPath("queryKey") path: String): BaseResult<List<Item>>

    @Api("post/query", headers = ["X-Token:token123456"])
    fun postQuery(
        @ApiField("type") type: String,
        @ApiField("page") page: Int,
        @ApiField("pageSize") pageSize: Int
    ): BaseResult<List<Item>>

    @Api("post/query", headers = ["X-Token:token123456"])
    fun postQuery(
        @ApiHeader("X-CALL-TIME") callTime: Long,
        @ApiHeader headers: Map<String, Any>,
        @ApiField params: Map<String, Any>
    ): BaseResult<List<Item>>

    @Api("post/body")
    fun postBody(@ApiBody reqBody: ReqBody): BaseResult<Void>

    @Api("post/part", isMultipart = true)
    fun postPartBody(
        @ApiPart("num") num: Int,
        @ApiPart("msg") msg: String?,
        @ApiPart("partFile") singleFile: File,
        @ApiPart part: MultipartBody.Part,
        @ApiPart("contentRequestBody") contentRequestBody: RequestBody,
        @ApiPart partMap: Map<String, Any>
    ): BaseResult<Void>
}