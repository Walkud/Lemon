package com.lemon.core;

import com.lemon.core.bean.BaseResult;
import com.lemon.core.request.HttpMethod;

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
