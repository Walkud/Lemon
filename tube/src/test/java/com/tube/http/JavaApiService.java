package com.tube.http;

import com.tube.http.bean.BaseResult;
import com.tube.http.disposer.Disposer;
import com.tube.http.request.HttpMethod;

import java.util.List;

/**
 * Describe:
 * Created by liya.zhu on 2022/3/7
 */
@ApiUrl("tube/")
interface JavaApiService {

    @Api(value = "post/query", method = HttpMethod.POST, headers = {"X-Token:token123456"})
    Disposer<BaseResult<List<Item>>> postQuery(
            @ApiField("type") String type, @ApiField("page") Integer page, @ApiField("pageSize") Integer pageSize);
}
