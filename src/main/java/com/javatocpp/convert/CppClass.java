package com.javatocpp.convert;

import com.javatocpp.log.Log;

import java.lang.reflect.*;
import java.util.ArrayList;

/**
 * Created by ZhuChao on 2017/3/14.
 */
public class CppClass {
    private Class cls;
    private int line;
    private String className;
    private String fileName;
    private ArrayList<CppField> fieldList = new ArrayList<>();
    private ArrayList<CppConstructor> constructorList = new ArrayList<>();
    private ArrayList<CppMethod> methodList = new ArrayList<>();
    private ArrayList<String> fieldIDList = new ArrayList<>();
    private ArrayList<String> methodIDList = new ArrayList<>();
    private ArrayList<String> initMethodIDList = new ArrayList<>();
    public CppClass(String fileName, int line, String className) {
        this.fileName = fileName;
        this.line = line;
        this.className = className;
        init();
    }

    private void init() {
        try {
            cls = Class.forName(className);
            getFields();
            getConstructors();
            getMethods();
        } catch (ClassNotFoundException e) {
            Log.error("Line: %d\tError: %s\t%s", line, "File imported is not java class!", e.toString());
        }
    }

    private void getConstructors() {
        Constructor<?> cons[] = cls.getConstructors();
        for (int i = 0; i< cons.length; i++) {
            Parameter parameters[] =  cons[i].getParameters();
            int modifier = cons[i].getModifiers();
            String sModifier = Modifier.toString(modifier);
            CppConstructor constructor = new CppConstructor(sModifier, cons[i].getName());
            for (int j = 0; j < parameters.length; j++) {
                CppParameter param = new CppParameter(parameters[j].getType().getName(), parameters[j].getName());
                constructor.addParam(param);
            }
            constructor.constructSignature();
            constructorList.add(constructor);
            initMethodIDList.add(constructor.getToken());
        }
    }
    private void getMethods() {
        Method[] methods = cls.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Class<?> returnType = methods[i].getReturnType();
            Parameter parameters[] = methods[i].getParameters();
            int modifier = methods[i].getModifiers();
            String sModifier = Modifier.toString(modifier);
            CppMethod method = new CppMethod(sModifier, returnType.getName(), methods[i].getName());
            for(int j = 0; j< parameters.length; j++) {
                CppParameter param = new CppParameter(parameters[j].getType().getName(), parameters[j].getName());
                method.addParam(param);
            }
            method.constructSignature();
            methodList.add(method);
            methodIDList.add(method.getToken());
        }
    }

    private void getFields() {
        Field[] fields = cls.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            int modifier = fields[i].getModifiers();
            String sModifier = Modifier.toString(modifier);
            Class<?>type = fields[i].getType();
            CppField field = new CppField(sModifier, type.getName(), fields[i].getName());
            field.constructSignature();
            fieldList.add(field);
            fieldIDList.add(field.getToken());
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("class " + className.substring(className.lastIndexOf(".") + 1, className.length()));
        builder.append(" {" + "\n");
        for (int i = 0; i < fieldList.size(); i++) {
            CppField filed= fieldList.get(i);
            builder.append("\t" + filed.getPrivilege() + " " + filed.getFieldType() + " " + filed.getFieldName() + ";\n");
        }
        for (int i = 0; i < constructorList.size(); i++) {
            CppConstructor constructor = constructorList.get(i);
            builder.append("\t" + constructor.getPrivilege() + " " + constructor.getConstructorName() + "(");
            ArrayList<CppParameter> params = constructor.getParams();
            for (int j = 0; j < params.size(); j++) {
                CppParameter param = params.get(j);
                if (j != 0) {
                    builder.append(" ");
                }
                builder.append(param.getParamType() + " " + param.getParamName());
                if (j != params.size() - 1) {
                    builder.append(",");
                }
            }
            builder.append(");\n");
        }
        for (int i = 0; i < methodList.size(); i++) {
            CppMethod method= methodList.get(i);
            builder.append("\t" + method.getPrivilege() + " " + method.getMethodType() + " " + method.getMethodName() + "(");
            ArrayList<CppParameter> params = method.getParams();
            for (int j = 0; j < params.size(); j++) {
                CppParameter param = params.get(j);
                if (j != 0) {
                    builder.append(" ");
                }
                builder.append(param.getParamType() + " " + param.getParamName());
                if (j != params.size() - 1) {
                    builder.append(",");
                }
            }
            builder.append(");\n");
        }
        builder.append("};\n");
        return builder.toString();
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ArrayList<CppField> getFieldList() {
        return fieldList;
    }

    public ArrayList<CppConstructor> getConstructorList() {
        return constructorList;
    }

    public ArrayList<CppMethod> getMethodList() {
        return methodList;
    }
}
