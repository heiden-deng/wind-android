package com.akaxin.client.util;

import android.text.method.ReplacementTransformationMethod;

/**
 * Created by Mr.kk on 2018/6/20.
 * This Project was client-android
 */

public class EditTransfor extends ReplacementTransformationMethod {
    @Override
    protected char[] getOriginal() {
        char[] cc = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        return cc;
    }

    @Override
    protected char[] getReplacement() {
        char[] aa = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
        return aa;
    }
}
