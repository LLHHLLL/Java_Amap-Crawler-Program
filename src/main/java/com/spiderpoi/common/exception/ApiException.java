package com.spiderpoi.common.exception;

import com.spiderpoi.common.result.CodeMsg;

/**
 * 自定义API异常
 */
public class ApiException extends RuntimeException {
    private CodeMsg codeMsg;

    public ApiException(CodeMsg codeMsg) {
        super(codeMsg.getMsg());
        this.codeMsg = codeMsg;
    }

    public ApiException(int code, String msg) {
        CodeMsg codeMsg = new CodeMsg(code,msg);
        this.codeMsg = codeMsg;
    }

    public ApiException(String message) {
        super(message);
    }

    public ApiException(Throwable cause) {
        super(cause);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public CodeMsg getCodeMsg() {
        return codeMsg;
    }
}
