package com.javatocpp.convert;

import com.javatocpp.convert.service.Token;

/**
 * Created by ZhuChao on 2017/3/14.
 */
public class CppField implements Token{
    private String privilege;
    private String fieldName;
    private String fieldType;
    private String signature;
    public CppField(String privilege, String fieldType, String fieldName) {
        this.privilege = privilege;
        this.fieldType = fieldType;
        this.fieldName = fieldName;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getPrivilege() {
        return privilege;
    }

    public void setPrivilege(String privilege) {
        this.privilege = privilege;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public void constructSignature() {
        signature = Util.getSign(fieldType);
    }

    @Override
    public String getToken() {
        return "_field_" + fieldName;
    }
}
