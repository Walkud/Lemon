package com.tube.http

import com.tube.http.bean.BaseResult
import com.tube.http.bean.ReqBody
import com.tube.http.bean.ServerTime
import com.tube.http.disposer.Disposer
import com.tube.http.request.body.MultipartBody
import com.tube.http.request.body.RequestBody
import java.io.File

/**
 * Describe:
 * Created by liya.zhu on 2022/3/3
 */
@ApiUrl("tube/")
interface KotlinApiService {

    @GET("getServerTime")
    fun getServerTime(): Disposer<BaseResult<ServerTime>>

    @GET("commitClientTime?createTime=10002345")
    fun commitClientTime(@Field("time") time: Long): Disposer<BaseResult<Void>>

    @POST("post/{queryKey}")
    fun postQuery(@Path("queryKey") path: String): Disposer<BaseResult<List<Item>>>

    @POST("post/query")
    @Headers(["X-Token:token123456"])
    fun postQuery(
        @Field("type") type: String,
        @Field("page") page: Int,
        @Field("pageSize") pageSize: Int
    ): Disposer<BaseResult<List<Item>>>

    @POST("post/query")
    @Headers(["X-Token:token123456"])
    fun postQuery(
        @Header("X-CALL-TIME") callTime: Long,
        @HeaderMap headers: Map<String, Any>,
        @FieldMap params: Map<String, Any>
    ): Disposer<BaseResult<List<Item>>>

    @POST("post/body")
    fun postBody(@Body reqBody: ReqBody): Disposer<BaseResult<Void>>

    @POST("post/part")
    @Multipart
    fun postPartBody(
        @Part("num") num: Int,
        @Part("msg") msg: String?,
        @Part("partFile") singleFile: File,
        @Part part: MultipartBody.Part,
        @Part("contentRequestBody") contentRequestBody: RequestBody,
        @PartMap partMap: Map<String, Any>
    ): Disposer<BaseResult<Void>>

    @POST("post/part")
    @Multipart
    fun postPartBody(
        @Part("msg") msg: String,
    ): Disposer<BaseResult<Void>>

}