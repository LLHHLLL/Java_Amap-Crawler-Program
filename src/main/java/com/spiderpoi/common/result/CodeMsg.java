package com.spiderpoi.common.result;

public class CodeMsg {

    private int code;
    private String msg;

    //通用的错误码 5001XX
    public static CodeMsg SUCCESS = new CodeMsg(0, "操作成功");
    public static CodeMsg BIND_ERROR = new CodeMsg(500101, "参数校验异常：%s");
    public static CodeMsg SERVER_ERROR = new CodeMsg(500102, "服务端异常");
    public static CodeMsg FILE_LOCATION_ERROR = new CodeMsg(500103, "未指定文件位置");


    public CodeMsg() {
    }

    public CodeMsg(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * 返回带参数的错误码
     *
     * @param args
     * @return
     */
    public CodeMsg fillArgs(Object... args) {
        int code = this.code;
        String message = String.format(this.msg, args);
        return new CodeMsg(code, message);
    }

    /**
     * 重新赋值返回的 msg
     *
     * @param message
     * @return CodeMsg
     */
    public CodeMsg setMessage(String message) {
        int code = this.code;
        return new CodeMsg(code, message);
    }

    @Override
    public String toString() {
        return "CodeMsg [code=" + code + ", msg=" + msg + "]";
    }


}
