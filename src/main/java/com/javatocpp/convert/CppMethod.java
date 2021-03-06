package com.javatocpp.convert;

import com.javatocpp.convert.service.Token;

import java.util.ArrayList;

/**
 * Created by ZhuChao on 2017/3/14.
 */
public class CppMethod implements Token{
    private String privilege;
    private String methodName;
    private String methodType;
    private String signature;
    private String token;
    private ArrayList<CppParameter> params = new ArrayList<>();
    private boolean isConflict = false;
    private boolean isStatic =false;
    private boolean isFinal = true;

    public CppMethod(String privilege, String methodType, String methodName) {
        this.privilege = privilege;
        if (privilege.contains("static")) {
            isStatic = true;
        }
        if (privilege.contains("final")) {
            isFinal = true;
        }
        this.methodType = methodType;
        String[] methodNames = methodName.split("\\$");
        this.methodName = methodNames[methodNames.length - 1];
        token = "";
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

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodType() {
        return methodType;
    }

    public void setMethodType(String methodType) {
        this.methodType = methodType;
    }

    public ArrayList<CppParameter> getParams() {
        return params;
    }

    public void addParam(CppParameter param) {
        params.add(param);
    }
    public void constructSignature() {
        String sign = "(";
        for (int i = 0; i < params.size(); i++) {
            CppParameter parameter = params.get(i);
            sign += Util.getSign(parameter.getParamType());
        }
        sign += ")";
        sign += Util.getSign(methodType);
        signature = sign;
    }

    @Override
    public String getToken() {
        if (!token.isEmpty() && !token.equals("")) {
            return token;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(privilege);
        builder.append(methodType);
        builder.append(methodName);
        builder.append(signature);
        for(int i = 0; i < params.size(); i++) {
            CppParameter parameter = params.get(i);
            builder.append(parameter.getParamType() + parameter.getParamName());
        }
        token = "_method_" + methodName + "_" + Util.getToken(builder.toString());
        return token;
    }

    public boolean isConflict() {
        return isConflict;
    }

    public void setConflict(boolean conflict) {
        isConflict = conflict;
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
