package com.tube.http.log.logger

import com.tube.http.Tube
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Describe:
 * Created by liya.zhu on 2022/4/14
 */
class DefaultTubeLogger : TubeLogger {
    private var logger = Logger.getLogger(Tube::class.java.simpleName)

    override fun log(message: String) {
        logger.log(Level.INFO, message)
    }
}