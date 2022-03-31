package com.tube.http

import com.tube.http.bean.BaseResult
import com.tube.http.bean.ServerTime
import com.tube.http.client.TubeClient
import com.tube.http.disposer.Accepter
import com.tube.http.disposer.Disposer
import com.tube.http.disposer.transformer.ConvertTransformer
import com.tube.http.disposer.transformer.WarpTransformer
import com.tube.http.interceptor.Interceptor
import com.tube.http.request.Response
import org.junit.Test
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class KotlinUnitTest {

    private val tubeHttp = TubeHttp.build {
        setBaseUrl("http://localhost:8080")
        addConverterFactory(GsonConverterFactory())
        addInterceptor(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val request = chain.request()
                val newRequest =
                    request.newBuilder()
                        .addHeader("X-CALL-ID", UUID.randomUUID().toString())
                        .build()
                return chain.proceed(newRequest)
            }
        })
        setTubeHttpClient(
            TubeClient.Builder().setReadTimeout(30 * 1000).setConnectTimeout(30 * 1000).build()
        )
    }

    @Test
    fun testQueryServerTime() {
        val httpService = tubeHttp.create<KotlinApiService>()
        httpService.getServerTime()
            .subscribe(object : SampleAccepter<BaseResult<ServerTime>>() {
                override fun call(result: BaseResult<ServerTime>) {
                    if (result.isSuccess()) {
                        println("serverTime:${result.data.time}")
                    } else {
                        print("serverError:${result.msg}")
                    }
                }
            })
    }

    @Test
    fun testCommitClientTime() {
        val httpService = tubeHttp.create<KotlinApiService>()
        httpService.commitClientTime(System.currentTimeMillis())
            .subscribe(object : SampleAccepter<BaseResult<Void>>() {
                override fun call(result: BaseResult<Void>) {
                    if (result.isSuccess()) {
                        println("commitClientTime code:${result.code},msg:${result.msg}")
                    } else {
                        print("serverError:${result.msg}")
                    }
                }
            })
    }


    @Test
    fun testContentTypeParse() {
        val contentType = "application/json;charset=utf-8;"
        val typeReg = "([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)"

        val contentTypePattern = Pattern.compile("$typeReg/$typeReg")
        val typeMatcher: Matcher = contentTypePattern.matcher(contentType)
        assert(typeMatcher.lookingAt())

        val type = typeMatcher.group(1)
        val subType = typeMatcher.group(2)

        val parameterReg = ";\\s*(?:$typeReg=($typeReg))?"
        val parameterPattern = Pattern.compile(parameterReg)
        val parameterMatcher = parameterPattern.matcher(contentType)
        val parameterMap = mutableMapOf<String, String>()

        parameterMatcher.region(typeMatcher.end(), contentType.length)

        while (parameterMatcher.find()) {
            val attribute = parameterMatcher.group(1)
            val value = parameterMatcher.group(2)
            if (attribute != null && value != null) {
                parameterMap[attribute] = value
            } else {
                break
            }
        }

        println("type:$type,subType:$subType")
        println("parameters:$parameterMap")
    }

    @Test
    fun testDisposerCreate() {
        Disposer.create("10")
            .convert(object : ConvertTransformer<String, Int> {
                override fun convert(result: String): Disposer<Int> {
                    return Disposer.create(result.toInt() / 0)//除以0抛出异常
                        .doStart { println("convert doStart call") }//convert 中添加无效
                        .doEnd { println("convert doEnd call") }//convert 中添加无效
                        .doError { println("convert doError call :${it.message}") }//convert 中添加无效
                }
            })
            .warp(object : WarpTransformer<Int, Int> {
                override fun transform(disposer: Disposer<Int>): Disposer<Int> {
                    return disposer
                        .doStart { println("warp doStart call") }
                        .doEnd { println("warp doEnd call") }
                        .doError { println("warp doError call :${it.message}") }
                }
            })
            .doStart { println("doStart call") }
            .doEnd { println("doEnd call") }
            .doError { println("doError call :${it.message}") }
            .subscribe(object : Accepter<Int> {
                override fun call(result: Int) {
                    println(result)
                }

                override fun onStart() {
                    println("Accepter onStart")
                }

                override fun onEnd() {
                    println("Accepter onEnd")
                }

                override fun onError(throwable: Throwable) {
                    println("Accepter onError :${throwable.message}")
                }
            })
    }

    @Test
    fun testPostQuery() {
        val httpService = tubeHttp.create<KotlinApiService>()
//        httpService.postQuery("testType",1,10)
        httpService.postQuery(mapOf("type" to "testType", "page" to 1, "pageSize" to 10))
            .subscribe(object : SampleAccepter<BaseResult<List<Item>>>() {
                override fun call(result: BaseResult<List<Item>>) {
                    if (result.isSuccess()) {
                        val sb = StringBuilder()
                        for (datum in result.data) {
                            if (sb.isNotEmpty()) {
                                sb.append(",")
                            }
                            sb.append(datum.id)
                        }
                        println("Item ids:$sb")
                    } else {
                        println("ServerMsg:${result.msg}")
                    }
                }

                override fun onError(throwable: Throwable) {
                    super.onError(throwable)
                    throwable.printStackTrace()
                }

            })
    }
}