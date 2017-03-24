package com.javatocpp.convert;

/**
 * Created by ZhuChao on 2017/3/14.
 */
public class CppParameter {
    private String paramName;
    private String paramType;

    public CppParameter() {

    }
    public CppParameter(String paramType, String paramName) {
        this.paramType = paramType;
        this.paramName = paramName;
    }
    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

}
