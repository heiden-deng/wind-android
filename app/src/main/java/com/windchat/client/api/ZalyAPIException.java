package com.windchat.client.api;

import com.windchat.client.util.log.ZalyLogUtils;
import com.windchat.im.socket.TransportPackageForResponse;

/**
 * Created by yichao on 2017/10/27.
 */

public class ZalyAPIException extends Exception {

    public static final int TYPE_ERRINFO_NULL = 1;//请求的错误信息为空
    public static final int TYPE_ERRINFO_CODE = 2;//请求的code不为success（success代表请求成功）

    private int exceptionType;

    private String errorInfoCode;
    private String errorInfoStr;
    private byte[] zalyResult;

    public ZalyAPIException(int exceptionType) {
        super("TYPE_ERRINFO_NULL");
        this.exceptionType = exceptionType;
    }

    public ZalyAPIException(int exceptionType, String errorInfoCode, String errorInfoStr) {
        super(errorInfoStr);
        try {
            this.exceptionType = exceptionType;
            this.errorInfoCode = errorInfoCode;
            this.errorInfoStr = errorInfoStr;
        } catch (Exception e) {
            ZalyLogUtils.getInstance().exceptionError(e);
        }
    }
    public ZalyAPIException(int exceptionType, String errorInfoCode, String errorInfoStr, TransportPackageForResponse result) {
        super(errorInfoStr);
        try {
            this.exceptionType = exceptionType;
            this.errorInfoCode = errorInfoCode;
            this.errorInfoStr  = errorInfoStr;
        } catch (Exception e) {
            ZalyLogUtils.getInstance().exceptionError(e);
        }
        try{
            this.zalyResult    = result.data.getData().toByteArray();
        }catch (Exception e) {
            ZalyLogUtils.getInstance().exceptionError(e);
        }
    }


    public int getExceptionType() {
        return exceptionType;
    }

    public String getErrorInfoCode() {
        return errorInfoCode;
    }

    public String getErrorInfoStr() {
        return errorInfoStr;
    }

    public byte[] getZalyResult(){
        return zalyResult;
    }
}


