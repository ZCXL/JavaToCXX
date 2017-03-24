package com.javatocpp.convert;

import com.javatocpp.log.Log;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by ZhuChao on 2017/3/15.
 */
public class DefineHFile {
    public static boolean defineHFile(CppClass cppClass, File headerFile) {
        FileOutputStream outer = null;
        try {
            outer = new FileOutputStream(headerFile);
            outer.write(getFileInfo(headerFile.getName()).getBytes("utf-8"));
            outer.write(getStartGrand(cppClass.getClassName()).getBytes("utf-8"));
            outer.write(getStartNameSpace().getBytes("utf-8"));
            outer.write("\n".getBytes("utf-8"));
            outer.write(getInclude().getBytes("utf-8"));
            outer.write("\n".getBytes("utf-8"));
            outer.write(getClassDefine(headerFile.getName()).getBytes("utf-8"));
            ArrayList<CppField> fileds = cppClass.getFieldList();
            for (int i = 0; i < fileds.size(); i++) {
                outer.write(getField(fileds.get(i)).getBytes("utf-8"));
            }
            //outer.write(getFiled(cppClass).getBytes("utf-8"));
            outer.write(getConstructor(cppClass).getBytes("utf-8"));
            outer.write(getMethod(cppClass).getBytes("utf-8"));
            outer.write(getEndDefine(headerFile.getName()).getBytes("utf-8"));
            outer.write(getEndNameSpace().getBytes("utf-8"));
            outer.write("\n".getBytes("utf-8"));
            outer.write(getEndGrand().getBytes("utf-8"));
        } catch (Exception e) {
            Log.error("");
            return false;
        } finally {
            if (outer != null) {
                try {
                    outer.close();
                } catch (IOException e) {
                    Log.error("");
                    return false;
                }
            }
        }
        return true;
    }
    public static String getFileInfo(String fileName) {
        StringBuilder builder = new StringBuilder();
        builder.append("/***************************************\n");
        builder.append(" * File: " + fileName + "\n");
        builder.append(" * Date: " + "2017/03/15" + "\n");
        builder.append(" ***************************************/\n");
        return builder.toString();
    }
    public static String getStartGrand(String className) {
        StringBuilder builder = new StringBuilder();
        String []dirs = className.split("\\.");
        String grandName = "";
        for (int i = 0; i < dirs.length; i++) {
            grandName += dirs[i].toUpperCase() + "_";
        }
        grandName += "H_";
        builder.append("#ifndef " + grandName + "\n");
        builder.append("#define " + grandName + "\n");
        return builder.toString();
    }
    public static String getInclude() {
        StringBuilder builder = new StringBuilder();
        builder.append("#include <bridge.h>\n");
        return builder.toString();
    }
    public static String getStartNameSpace() {
        return "DEFINE_START_NAMESPACE()";
    }
    public static String getEndNameSpace() {
        return "DEFINE_END_NAMESPACE()";
    }
    public static String getClassDefine(String className) {
        StringBuilder builder = new StringBuilder();
        builder.append("DEFINE_CLASS(" + className.replace(".h", "") + ");\n");
        return builder.toString();
    }
    public static String getFiled(CppClass cppClass) {
        StringBuilder builder = new StringBuilder();
        ArrayList<CppField> fields = cppClass.getFieldList();
        for (int i = 0; i < fields.size(); i++) {
            CppField field = fields.get(i);
            String []types = field.getFieldType().split("\\.");
            builder.append("DEFINE_FILED(" + getPrivilege(field.getPrivilege()) + ", " + types[types.length - 1] +
                            ", " + field.getFieldName() + ", " + field.getSignature() + ");\n");
        }
        return builder.toString();
    }
    public static String getField(CppField cppFiled) {
        StringBuilder builder = new StringBuilder();
        builder.append(cppFiled.getPrivilege() + ":\n");
        builder.append("\t" + Util.getType(cppFiled.getFieldType()) + " get_" + cppFiled.getFieldName() + "() {\n");
        builder.append("\t\tif (" + cppFiled.getToken() + " == NULL) {\n");
        builder.append("\t\t\t" + cppFiled.getToken() + " = " + "env->GetFieldID(_clazz, \"" +
                        cppFiled.getFieldName() + "\", \"" + cppFiled.getSignature() + "\");\n");
        builder.append("\t\t}\n");
        builder.append("\t\t" + Util.getType(cppFiled.getFieldType()) + " ret = env->" + Util.getFieldID(cppFiled.getFieldType()) +
                        "(_object, " + cppFiled.getToken() + ");\n");
        builder.append("\t\treturn ret;\n");
        builder.append("\t}\n");
        builder.append("\tvoid " + "set_" + Util.getType(cppFiled.getFieldName()) + "(" + Util.getType(cppFiled.getFieldType()) +
                        " object) {\n");
        builder.append("\t\tif (" + cppFiled.getToken() + " == NULL) {\n");
        builder.append("\t\t\t" + cppFiled.getToken() + " = " + "env->GetFieldID(_clazz, \"" +
                cppFiled.getFieldName() + "\", \"" + cppFiled.getSignature() + "\");\n");
        builder.append("\t\t}\n");
        builder.append("\t\t" + "env->" + Util.setFieldID(cppFiled.getFieldType()) +"(_object, " + cppFiled.getToken() +
                        ", " + "object);\n");
        builder.append("\t}\n");
        return builder.toString();
    }
    public static String getConstructor(CppClass cppClass) {
        StringBuilder builder = new StringBuilder();
        ArrayList<CppConstructor> cppConstructors = cppClass.getConstructorList();
        for (int i = 0; i < cppConstructors.size(); i++) {
            CppConstructor constructor = cppConstructors.get(i);
            String []conName = constructor.getConstructorName().split("\\.");
            builder.append("DEFINE_CONSTRUCTOR(" + getPrivilege(constructor.getPrivilege()) +  ", " + conName[conName.length - 1] +
                            ", " + constructor.getSignature());
            ArrayList<CppParameter> parameters = constructor.getParams();
            if (parameters.size() > 0) {
                builder.append(", ");
                builder.append(getParameters(parameters));
            }
            builder.append(");\n");
        }
        return builder.toString();
    }
    public static String getMethod(CppClass cppClass) {
        StringBuilder builder = new StringBuilder();
        ArrayList<CppMethod> methods = cppClass.getMethodList();
        for (int i = 0; i < methods.size(); i++) {
            CppMethod method = methods.get(i);
            String[] types = method.getMethodType().split("\\.");
            builder.append("DEFINE_METHOD(" + getPrivilege(method.getPrivilege()) + ", " + types[types.length - 1] +
                            ", " + method.getMethodName() + ", " + method.getSignature());
            ArrayList<CppParameter> parameters = method.getParams();
            if (parameters.size() > 0) {
                builder.append(", ");
                builder.append(getParameters(parameters));
            }
            builder.append(");\n");
        }
        return builder.toString();
    }
    public static String getEndDefine(String className) {
        return "DEFINE_END(" + className.replace(".h", "") + ");\n";
    }
    public static String getEndGrand() {
        return "#endif\n";
    }
    public static String getParameters(ArrayList<CppParameter> parameters) {
        StringBuilder builder = new StringBuilder();
        for (int j = 0; j < parameters.size(); j++) {
            CppParameter parameter = parameters.get(j);
            String[] types = parameter.getParamType().split("\\.");
            builder.append(types[types.length - 1] + ", " + parameter.getParamName());
            if (j != parameters.size() - 1) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }
    public static String getPrivilege(String privilege) {
        if(privilege.contains("private")) {
            return "private";
        } else if (privilege.contains("public")) {
            return "public";
        } else if (privilege.contains("protected")) {
            return "protected";
        }
        return "";
    }
}
