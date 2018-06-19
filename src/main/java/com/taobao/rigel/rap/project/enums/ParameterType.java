package com.taobao.rigel.rap.project.enums;

/**
 * add 2015.11.21
 * 参数表parameter 中参数类型
 *
 */
public enum ParameterType {

    REQUEST(1,"请求参数"),
    RESPONSE(2,"返回参数"),
    RETURNCODE(3,"返回错误码参数"),
    CHILDPARAMETER(4,"子类结构参数");


    private int code;
    private String desc;
    private ParameterType(int code, String desc){
        this.code = code;
        this.desc = desc;
    }


    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
