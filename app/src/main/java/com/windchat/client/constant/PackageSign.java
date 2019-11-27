package com.windchat.client.constant;

import com.windchat.client.BuildConfig;

/**
 * Created by anguoyue on 27/01/2018.
 */

public abstract class PackageSign {

    public static final String AKAXIN_PACKAGE = "com.akaxin.client";
    public static final String AKAXIN_PACKAGE_DEBUG = "com.akaxin.client.debug";

    public static String getPackage() {
        if (BuildConfig.DEBUG) return AKAXIN_PACKAGE_DEBUG;
        else return AKAXIN_PACKAGE;
    }

}
