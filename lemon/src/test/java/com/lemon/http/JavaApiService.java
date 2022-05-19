package com.lemon.http;

import com.lemon.http.bean.BaseResult;
import com.lemon.http.request.HttpMethod;

import java.util.List;

/**
 * Describe:
 * Created by liya.zhu on 2022/3/7
 */
@ApiUrl("lemon/")
interface JavaApiService {

    @Api(value = "post/query", method = HttpMethod.POST, headers = {"X-Token:token123456"})
    BaseResult<List<Item>> postQuery(
            @ApiField("type") String type, @ApiField("page") Integer page, @ApiField("pageSize") Integer pageSize);
}
