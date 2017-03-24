package com.javatocpp.convert;

import com.javatocpp.convert.service.Token;

import java.util.ArrayList;

/**
 * Created by ZhuChao on 2017/3/14.
 */
public class CppConstructor implements Token{
    private String privilege;
    private String constructorName;
    private String signature;
    private String token;
    private ArrayList<CppParameter> params = new ArrayList<>();
    public CppConstructor() {
        token = "";
    }
    public CppConstructor(String privilege, String constructorName) {
        this.privilege = privilege;
        this.constructorName = constructorName;
        token = "";
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public void addParam(CppParameter param) {
        params.add(param);
    }

    public String getPrivilege() {
        return privilege;
    }

    public void setPrivilege(String privilege) {
        this.privilege = privilege;
    }

    public String getConstructorName() {
        return constructorName;
    }

    public void setConstructorName(String constructorName) {
        this.constructorName = constructorName;
    }

    public ArrayList<CppParameter> getParams() {
        return params;
    }

    public void setParams(ArrayList<CppParameter> params) {
        this.params = params;
    }
    public void constructSignature() {
        String sign = "(";
        for (int i = 0; i < params.size(); i++) {
            CppParameter parameter = params.get(i);
            sign += Util.getSign(parameter.getParamType());
        }
        sign += ")V";
        signature = sign;
    }

    @Override
    public String getToken() {
        if (!token.isEmpty() && !token.equals("")) {
            return token;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(privilege);
        builder.append(constructorName);
        builder.append(signature);
        for(int i = 0; i < params.size(); i++) {
            CppParameter parameter = params.get(i);
            builder.append(parameter.getParamType() + parameter.getParamName());
        }
        token = constructorName + Util.getToken(builder.toString());
        return token;
    }
}
