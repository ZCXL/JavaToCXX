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
    private boolean isStatic = false;
    private boolean isFinal = false;
    public CppField(String privilege, String fieldType, String fieldName) {
        this.privilege = privilege;
        if (privilege.contains("static")) {
            isStatic = true;
        }
        if (privilege.contains("final")) {
            isFinal = true;
        }
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

    public boolean isStatic() {
        return isStatic;
    }

    public void setStatic(boolean aStatic) {
        isStatic = aStatic;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean aFinal) {
        isFinal = aFinal;
    }
}
