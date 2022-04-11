package com.tube.http;

import com.tube.http.bean.BaseResult;
import com.tube.http.disposer.Disposer;

import java.lang.reflect.WildcardType;
import java.util.List;

/**
 * Describe:
 * Created by liya.zhu on 2022/3/7
 */
@ApiUrl("tube/")
interface JavaApiService {

    @POST("post/query")
    @Headers({"X-Token:token123456"})
    Disposer<BaseResult<List<Item>>> postQuery(
            @Field("type") String type, @Field("page") Integer page, @Field("pageSize") Integer pageSize);
}
