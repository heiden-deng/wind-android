package com.orhanobut.logger;

/**
 * But more pretty, simple and powerful
 * <p>
 * 原有基础上添加了含有二级tag的.i .d方法 by yichao
 */
public final class Logger {
    public static final int VERBOSE = 2;
    public static final int DEBUG = 3;
    public static final int INFO = 4;
    public static final int WARN = 5;
    public static final int ERROR = 6;
    public static final int ASSERT = 7;

    private static Printer printer = new LoggerPrinter();

    private Logger() {
        //no instance
    }

    public static void printer(Printer printer) {
        Logger.printer = printer;
    }

    public static void addLogAdapter(LogAdapter adapter) {
        printer.addAdapter(adapter);
    }

    public static void clearLogAdapters() {
        printer.clearLogAdapters();
    }

    /**
     * Given tag will be used as tag only once for this method call regardless of the tag that's been
     * set during initialization. After this invocation, the general tag that's been set will
     * be used for the subsequent log calls
     */
    public static Printer t(String tag) {
        return printer.t(tag);
    }

    /**
     * General log function that accepts all configurations as parameter
     */
    public static void log(int priority, String tag, String message, Throwable throwable) {
        printer.log(priority, tag, message, throwable);
    }

    public static void d(String message, Object... args) {
        printer.d(message, args);
    }

    public static void d(Object object) {
        printer.d(object);
    }

    public static void e(String message, Object... args) {
        printer.e(null, message, args);
    }

    public static void e(Throwable throwable, String message, Object... args) {
        printer.e(throwable, message, args);
    }

    public static void e(String tag, Throwable throwable, Object... args) {
        printer.e(throwable, tag, args);
    }

    public static void e(Throwable throwable) {
        e(throwable, "");
    }

    public static void i(String message, Object... args) {
        printer.i(message, args);
    }

    /**
     * log.info
     *
     * @param tag     二级tag
     * @param message log信息
     */
    public static void i(String tag, String message) {
        if ("".equals(tag)) {
            i(message);
            return;
        }
        t(tag);
        i(message);
    }

    public static void v(String message, Object... args) {
        printer.v(message, args);
    }

    /**
     * log.warn
     *
     * @param tag     二级tag
     * @param message log信息
     */
    public static void w(String tag, String message) {
        if ("".equals(tag)) {
            w(message);
            return;
        }
        t(tag);
        w(message);
    }

    public static void w(String message, Object... args) {
        printer.w(message, args);
    }

    /**
     * Tip: Use this for exceptional situations to log
     * ie: Unexpected errors etc
     */
    public static void wtf(String message, Object... args) {
        printer.wtf(message, args);
    }

    /**
     * Formats the given json content and print it
     */
    public static void json(String json) {
        printer.json(json);
    }

    /**
     * Formats the given xml content and print it
     */
    public static void xml(String xml) {
        printer.xml(xml);
    }

}
