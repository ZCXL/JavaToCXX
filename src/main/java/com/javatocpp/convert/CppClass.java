package com.javatocpp.convert;

import com.javatocpp.log.Log;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ZhuChao on 2017/3/14.
 */
public class CppClass {
    private Class cls;
    private int line;
    private boolean hasDefaultConstructor = true;
    private String className;
    private String fileName;
    private ArrayList<CppField> fieldList = new ArrayList<>();
    private ArrayList<CppField> staticFieldList = new ArrayList<>();
    private ArrayList<CppConstructor> constructorList = new ArrayList<>();
    private HashMap<String, ArrayList<CppMethod>> methodSet = new HashMap<>();
    private HashMap<String, ArrayList<CppMethod>> staticMethodSet = new HashMap<>();
    private ArrayList<String> fieldIDList = new ArrayList<>();
    private ArrayList<String> staticFieldIDList = new ArrayList<>();
    private ArrayList<String> methodIDList = new ArrayList<>();
    private ArrayList<String> staticMethodIDList = new ArrayList<>();
    private ArrayList<String> initMethodIDList = new ArrayList<>();
    private ArrayList<String> predeclareClass = new ArrayList<>();
    private ArrayList<CppClass> innerClasses = new ArrayList<>();

    public CppClass(String fileName, String className) {
        this.fileName = fileName;
        if(className.contains(".h")) {
            className = className.substring(0, className.lastIndexOf("."));
        }
        this.className = className;
        hasDefaultConstructor = false;
    }
    public CppClass(String fileName, int line, String className) {
        this.fileName = fileName;
        this.line = line;
        if(className.contains(".h")) {
            className = className.substring(0, className.lastIndexOf("."));
        }
        this.className = className;
        init();
    }

    /**
     * Got methods, constructors and field.
     */
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

    /**
     * Parse all constructors in java class.
     */
    private void getConstructors() {
        Constructor<?> cons[] = cls.getConstructors();
        for (int i = 0; i< cons.length; i++) {
            Parameter parameters[] =  cons[i].getParameters();
            int modifier = cons[i].getModifiers();
            String sModifier = Modifier.toString(modifier);
            CppConstructor constructor = new CppConstructor(sModifier, cons[i].getName());
            for (int j = 0; j < parameters.length; j++) {
                CppParameter param = new CppParameter(parameters[j].getType().getName(), parameters[j].getName());
                String innerType = Util.getNotInnerType(param.getParamType());
                if (!innerType.equals("") && !predeclareClass.contains(innerType)) {
                    predeclareClass.add(innerType);
                }
                constructor.addParam(param);
            }
            constructor.constructSignature();
            constructorList.add(constructor);
            initMethodIDList.add(constructor.getToken());
        }
    }

    /**
     * Parse all methods in java class.
     */
    private void getMethods() {
        Method[] methods = cls.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Class<?> returnType = methods[i].getReturnType();
            Parameter parameters[] = methods[i].getParameters();
            int modifier = methods[i].getModifiers();
            String sModifier = Modifier.toString(modifier);
            CppMethod method = new CppMethod(sModifier, returnType.getName(), methods[i].getName());
            String innerType = Util.getNotInnerType(method.getMethodType());
            if (!innerType.equals("") && !predeclareClass.contains(innerType)) {
                predeclareClass.add(innerType);
            }
            String key = method.getMethodName();
            for(int j = 0; j< parameters.length; j++) {
                key += parameters[j].getType().getName();
                CppParameter param = new CppParameter(parameters[j].getType().getName(), parameters[j].getName());
                innerType = Util.getNotInnerType(param.getParamType());
                if (!innerType.equals("") && !predeclareClass.contains(innerType)) {
                    predeclareClass.add(innerType);
                }
                method.addParam(param);
            }
            method.setConflict(Util.filterKeyword(method.getMethodName()));
            method.constructSignature();
            if (method.isStatic()) {
                staticMethodIDList.add(method.getToken());
            } else {
                methodIDList.add(method.getToken());
            }

            ArrayList<CppMethod> methodList = methodSet.get(key);
            if (methodList == null) {
                methodList = new ArrayList<>();
                methodList.add(method);
                methodSet.put(key, methodList);
            } else {
                methodList.add(method);
            }
        }
    }

    /**
     * Get all fields in java class.
     */
    private void getFields() {
        Field[] fields = cls.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            int modifier = fields[i].getModifiers();
            String sModifier = Modifier.toString(modifier);
            Class<?>type = fields[i].getType();
            CppField field = new CppField(sModifier, type.getName(), fields[i].getName());
            String innerType = Util.getNotInnerType(type.getName());
            if (!innerType.equals("") && !predeclareClass.contains(innerType)) {
                predeclareClass.add(innerType);
            }
            field.constructSignature();
            fieldList.add(field);
            if (field.isStatic()) {
                staticFieldIDList.add(field.getToken());
            } else {
                fieldIDList.add(field.getToken());
            }
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

        for (String key: methodSet.keySet()) {
            ArrayList<CppMethod> methodList = methodSet.get(key);
            for (int i = 0; i < methodList.size(); i++) {
                CppMethod method = methodList.get(i);
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

    public ArrayList<String> getFieldIDList() {
        return fieldIDList;
    }

    public void setFieldIDList(ArrayList<String> fieldIDList) {
        this.fieldIDList = fieldIDList;
    }

    public ArrayList<String> getMethodIDList() {
        return methodIDList;
    }

    public void setMethodIDList(ArrayList<String> methodIDList) {
        this.methodIDList = methodIDList;
    }

    public ArrayList<String> getInitMethodIDList() {
        return initMethodIDList;
    }

    public void setInitMethodIDList(ArrayList<String> initMethodIDList) {
        this.initMethodIDList = initMethodIDList;
    }

    public boolean isHasDefaultConstructor() {
        return hasDefaultConstructor;
    }

    public void setHasDefaultConstructor(boolean hasDefaultConstructor) {
        this.hasDefaultConstructor = hasDefaultConstructor;
    }

    public ArrayList<String> getPredeclareClass() {
        return predeclareClass;
    }

    public void setPredeclareClass(ArrayList<String> predeclareClass) {
        this.predeclareClass = predeclareClass;
    }

    public HashMap<String, ArrayList<CppMethod>> getMethodSet() {
        return methodSet;
    }

    public ArrayList<CppField> getStaticFieldList() {
        return staticFieldList;
    }

    public void addStaticField(CppField cppField) {
        staticFieldList.add(cppField);
    }

    public HashMap<String, ArrayList<CppMethod>> getStaticMethodSet() {
        return staticMethodSet;
    }

    public void addStaticMethod(String key, ArrayList<CppMethod> methods) {
        staticMethodSet.put(key, methods);
    }

    public ArrayList<String> getStaticFieldIDList() {
        return staticFieldIDList;
    }

    public ArrayList<String> getStaticMethodIDList() {
        return staticMethodIDList;
    }
}
