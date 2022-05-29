package com.hzy.utils;

import org.slf4j.Logger;

/**
 * User: hzy
 * Date: 2022/5/29
 * Time: 17:25
 * Description:
 */
public class LoggerUtil {
    public static void debug(Logger logger, String format, Object... arguments) {
        if (logger.isDebugEnabled()) {
            logger.debug(format, arguments);
        }
    }

    public static void info(Logger logger, String format, Object... arguments) {
        if (logger.isInfoEnabled()) {
            logger.debug(format, arguments);
        }
    }

    public static void error(Logger logger,Throwable throwable, String format, Object... arguments) {
        if (logger.isErrorEnabled()) {
            logger.error(format, arguments, throwable);
        }
    }

}
