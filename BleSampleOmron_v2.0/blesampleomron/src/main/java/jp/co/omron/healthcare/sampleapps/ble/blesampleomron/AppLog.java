/*
 * File: AppLog.java
 *
 * Copyright (c) 2015 OMRON HEALTHCARE Co., Ltd. All rights reserved.
 */

package jp.co.omron.healthcare.sampleapps.ble.blesampleomron;

import android.util.Log;

public class AppLog {

    private static final boolean OUTPUT_LOG_MODE = true;
    private static final LogLevel OUTPUT_LOG_LEVEL = LogLevel.Debug;
    private final static String BLE_LOG_TAG = "BLE_LOG";
    private static final String LOG_PREFIX_DEBUG = "[DEBUG] ";
    private static final String LOG_PREFIX_INFO = "[INFO]  ";
    private static final String LOG_PREFIX_WARN = "[WARN]  ";
    private static final String LOG_PREFIX_ERROR = "[ERROR] ";
    private static final String LOG_PREFIX_METHOD_IN = "[IN]    ";
    private static final String LOG_PREFIX_METHOD_OUT = "[OUT]   ";
    private static final String LOG_PREFIX_BLE_INFO = "[LOG_OUT]";
    private static String TAG = "BleSampleOmron";

    public static void d(String msg) {
        outputLog(LogLevel.Debug, TAG, LOG_PREFIX_DEBUG + methodNameString(true) + ' ' + msg);
    }

    public static void i(String msg) {
        outputLog(LogLevel.Info, TAG, LOG_PREFIX_INFO + methodNameString(true) + ' ' + msg);
    }

    public static void w(String msg) {
        outputLog(LogLevel.Warn, TAG, LOG_PREFIX_WARN + methodNameString(true) + ' ' + msg);
    }

    public static void e(String msg) {
        outputLog(LogLevel.Error, TAG, LOG_PREFIX_ERROR + methodNameString(true) + ' ' + msg);
    }

    public static void dMethodIn() {
        outputLog(LogLevel.Debug, TAG, LOG_PREFIX_METHOD_IN + methodNameString(false));
    }

    public static void dMethodIn(String msg) {
        outputLog(LogLevel.Debug, TAG, LOG_PREFIX_METHOD_IN + methodNameString(false) + " " + msg);
    }

    public static void dMethodOut() {
        outputLog(LogLevel.Debug, TAG, LOG_PREFIX_METHOD_OUT + methodNameString(false));
    }

    public static void dMethodOut(String msg) {
        outputLog(LogLevel.Debug, TAG, LOG_PREFIX_METHOD_OUT + methodNameString(false) + " " + msg);
    }

    public static void bleInfo(String msg) {
        outputLog(LogLevel.Info, BLE_LOG_TAG, LOG_PREFIX_BLE_INFO + msg);
    }

    public static void outputLog(LogLevel level, String tag, String msg) {
        if (!OUTPUT_LOG_MODE) {
            return;
        }
        if (OUTPUT_LOG_LEVEL.ordinal() > level.ordinal()) {
            return;
        }
        switch (level) {
            case Debug:
                Log.d(tag, msg);
                break;
            case Info:
                Log.i(tag, msg);
                break;
            case Warn:
                Log.w(tag, msg);
                break;
            case Error:
                Log.e(tag, msg);
                break;
            default:
                Log.v(tag, msg);
                break;
        }
    }

    private static String methodNameString(boolean addLineNumber) {
        final StackTraceElement element = Thread.currentThread().getStackTrace()[4];
        final String fullClassName = element.getClassName();
        final String simpleClassName = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
        final String methodName = element.getMethodName();
        final int lineNumber = element.getLineNumber();
        final StringBuilder sb = new StringBuilder();
        sb.append(simpleClassName + "#" + methodName);
        if (addLineNumber) {
            sb.append(":" + lineNumber);
        }
        return sb.toString();
    }

    public enum LogLevel {
        Verbose, Debug, Info, Warn, Error
    }
}
