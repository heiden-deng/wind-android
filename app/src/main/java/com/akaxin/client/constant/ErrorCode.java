package com.akaxin.client.constant;

/**
 * Created by yichao on 2017/12/16.
 */

public interface ErrorCode {

    String REQUEST_SUCCESS = "success";
    String REQUEST_ERROR = "error";
    String REQUEST_ERROR_ALTER = "error.alter";
    String REQUEST_ERROR_ALERT = "error.alert";

    //请求验证session失败
    String REQUEST_SESSION_ERROR = "error.session";
    //本机器身份和手机号已绑定，直接将绑定手机号存入本地
    String REQUEST_PHONE_SAME = "error.phone.same";

    String LOGIN_NEED_REGISTER1 = "error.login.need_register";
    String LOGIN_NEED_REGISTER2 = "error.login.noRegister";
    String LOGIN_NEED_REGISTER3 = "error.login.needRegister";

    String PHONE_HAS_USER       = "error.phone.hasUser";
    String PHONE_SAME_USER      = "error.phone.same";
    String USER_UNIQUE_SUI      = "error.unique.sui";
}
