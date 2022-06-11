package com.lemon.log.logger

import com.lemon.core.Lemon
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Describe: Lemon 默认的日志输出器
 * Created by liya.zhu on 2022/4/14
 */
class DefaultLemonLogger : LemonLogger {
    private var logger = Logger.getLogger(Lemon::class.java.simpleName)

    override fun log(message: String) {
        logger.log(Level.INFO, message)
    }
}