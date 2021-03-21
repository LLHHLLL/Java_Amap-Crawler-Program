package com.spiderpoi.common.exception;


import com.spiderpoi.common.result.CodeMsg;

/**
 * 断言处理类，用于抛出各种API异常
 */
public class Asserts {

    public static void fail(CodeMsg codeMsg) {
        throw new ApiException(codeMsg);
    }

    public static void fail(int code,String msg) {
        throw new ApiException(code,msg);
    }




}
