package com.lemon.core

import com.lemon.core.bean.ReqBody
import com.lemon.core.client.LemonClient
import com.lemon.log.LemonLogInterceptor
import com.lemon.log.LemonLogLevel
import com.lemon.core.request.body.MultipartBody
import com.lemon.core.request.body.RequestBody
import kotlinx.coroutines.*
import org.junit.Test
import java.io.File
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class KotlinUnitTest {

    private val lemon = Lemon.build {
        setApiUrl("http://localhost.charlesproxy.com:8080")
        addConverterFactory(GsonConverterFactory())
        addInterceptor { chain ->
            val request = chain.request()
            val newRequest =
                request.newBuilder()
                    .addHeader("X-CALL-ID", UUID.randomUUID().toString())
                    .build()
            chain.proceed(newRequest)
        }
        addInterceptor(LemonLogInterceptor(LemonLogLevel.ALL))
        setHttpClient(
            LemonClient.Builder().setReadTimeout(30 * 1000).setConnectTimeout(30 * 1000).build()
        )
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
    fun testQueryServerTime() {
        val httpService = lemon.create<KotlinApiService>()
        val result = httpService.getServerTime()
        if (result.isSuccess()) {
            println("serverTime:${result.data.time}")
        } else {
            print("serverError:${result.msg}")
        }
    }

    @Test
    fun testCommitClientTime() {
        val httpService = lemon.create<KotlinApiService>()
        val result = httpService.commitClientTime(System.currentTimeMillis())
        if (result.isSuccess()) {
            println("commitClientTime code:${result.code},msg:${result.msg}")
        } else {
            print("serverError:${result.msg}")
        }
    }

    @Test
    fun testPostQuery() {
        try {
            val httpService = lemon.create<KotlinApiService>()
            val callTime = System.currentTimeMillis()
            val result = httpService.postQuery(
                callTime,
                mapOf(
                    "X-SDK-VERSION-NAME" to BuildConfig.VERSION_NAME,
                    "X-SDK-VERSION-CODE" to BuildConfig.VERSION_CODE,
                ),
                mapOf("type" to "testType", "page" to 1, "pageSize" to 10)
            )
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
                println("ServerMsg Fail! ServerMsg:${result.msg}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Test
    fun testPostBody() {
        val httpService = lemon.create<KotlinApiService>()
        val result = httpService.postBody(ReqBody(1, "call data info"))
        if (result.isSuccess()) {
            println("postBody Sccuess")
        } else {
            println("ServerMsg Fail! ServerMsg:${result.msg}")
        }
    }

    @Test
    fun testPostPartBody() {
        val httpService = lemon.create<KotlinApiService>()
        val file = File("/Users/liya-zhu/Desktop/test.txt")
        val part = MultipartBody.Part.create(
            name = "part",
            requestBody = RequestBody.Companion.create("Part Content!")
        )
        val contentRequestBody = RequestBody.create("RequestBody Content!")

        val mapPart = MultipartBody.Part.create(
            name = "mapPart",
            requestBody = RequestBody.Companion.create("Map Part Content!")
        )

        val partMap = mapOf(
            "mapNum" to 234,
            "mapMsg" to "Map Msg Content!",
            "mapFile" to file,
            "mapPart" to mapPart,
            "mapRequestBody" to RequestBody.Companion.create(" Map Part Content!")
        )

        try {
            val result =
                httpService.postPartBody(123, "Part msg!", file, part, contentRequestBody, partMap)
            println("postPartBody code:${result.code},msg:${result.msg}")
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    @Test
    fun testCoroutine() {
        /**
         * 协程作用域
         */
        val scope = object : CoroutineScope {
            override val coroutineContext: CoroutineContext =
                SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
                    println("CoroutineExceptionHandler: $throwable")
                } + object : ContinuationInterceptor {
                    override val key: CoroutineContext.Key<*>
                        get() = ContinuationInterceptor

                    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {
                        return object : Continuation<T> by continuation {
                            override fun resumeWith(result: Result<T>) {
                                println("startContinuation")
                                continuation.resumeWith(result)
                                println("EndContinuation")
                            }
                        }
                    }
                }
        }

        scope.launch {
            println("1")
            launch(Dispatchers.IO) {
                println("2")
                throw RuntimeException("throw exception 2")
            }

            launch {
                println("3")
                throw RuntimeException("throw exception 3")
            }
            println("4")
        }
    }
}