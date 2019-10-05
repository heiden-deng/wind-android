package com.windchat.logger;

/**
 * Created by anguoyue on 2019/9/14.
 */

public class WindLogger {

    private static WindLogger logger = new WindLogger();

    public static WindLogger getInstance() {
        return logger;
    }


    public void info(String tag, String message) {

    }

    public void warn(String tag, String message) {

    }

    public void debug(String tag, String message) {

    }

    public void error(String tag, Throwable t, String message) {

    }
}
