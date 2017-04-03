package com.javatocpp.convert;

import com.javatocpp.log.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Created by ZhuChao on 2017/3/15.
 */
public class DefineHFile {
    public static boolean defineHFile(CppClass cppClass, File headerFile) {
        FileOutputStream outer = null;
        try {
            outer = new FileOutputStream(headerFile);
            outer.write(getFileInfo(headerFile.getName(), cppClass.getClassName()).getBytes("utf-8"));
            outer.write(getStartGrand(cppClass.getClassName()).getBytes("utf-8"));
            outer.write("\n".getBytes("utf-8"));
            outer.write(getInclude(cppClass).getBytes("utf-8"));
            outer.write("\n".getBytes("utf-8"));
            outer.write(getStartNameSpace().getBytes("utf-8"));

            outer.write(getClassDefine(headerFile.getName()).getBytes("utf-8"));

            /**
             * Define variable of method id or field id.
             */
            outer.write(getDefineID(cppClass).getBytes("utf-8"));

            outer.write(getDefaultConstructor(headerFile.getName(),
                        cppClass.getClassName(),
                        cppClass.isHasDefaultConstructor()).getBytes("utf-8"));
            outer.write(getDeconstructor(headerFile.getName()).getBytes("utf-8"));
            /**
             * Print the field of a java object
             */
            ArrayList<CppField> fileds = cppClass.getFieldList();
            for (int i = 0; i < fileds.size(); i++) {
                outer.write(getField(fileds.get(i)).getBytes("utf-8"));
            }

            /**
             * Print constructor method of a java object
             */
            ArrayList<CppConstructor> constructors = cppClass.getConstructorList();
            for (int i = 0; i < constructors.size(); i++) {
                outer.write(getConstructor(constructors.get(i), cppClass.getClassName()).getBytes("utf-8"));
            }

            /**
             * protected constructor function
             */
            outer.write(getConstructor(cppClass).getBytes("utf-8"));

            /**
             * Print method of a java method
             */
            ArrayList<CppMethod> methods = cppClass.getMethodList();
            for (int i = 0; i < methods.size(); i++) {
                outer.write(getMethod(methods.get(i)).getBytes("utf-8"));
            }
            outer.write(getEndDefine(headerFile.getName()).getBytes("utf-8"));

            outer.write(getEndNameSpace().getBytes("utf-8"));
            outer.write("\n".getBytes("utf-8"));
            outer.write(getEndGrand().getBytes("utf-8"));
        } catch (Exception e) {
            Log.error("Msg: generate c++ header file error.[%s]\n", e.toString());
            return false;
        } finally {
            if (outer != null) {
                try {
                    outer.close();
                } catch (IOException e) {
                    Log.error("Msg: close c++ header file error.[%s]\n", e.toString());
                    return false;
                }
            }
        }
        return true;
    }
    public static String getFileInfo(String fileName, String className) {
        StringBuilder builder = new StringBuilder();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        builder.append("/*******************************************************************\n");
        builder.append(" * File: " + fileName + "\n");
        builder.append(" * Date: " + df.format(new Date()) + "\n");
        builder.append(" * Mapping: " + fileName + " ==> " + className + "\n");
        builder.append(" * Description: \n");
        builder.append(Util.getLisence());
        builder.append(" *******************************************************************/\n");
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
    public static String getInclude(CppClass cppClass) {
        StringBuilder builder = new StringBuilder();
        builder.append("#include <bridge/bridge.h>\n");
        builder.append("#include <bridge_object/common.h>\n");
        builder.append("#include <bridge_object/java.h>\n");

        ArrayList<String> predeclareClasses = cppClass.getPredeclareClass();
        String[] classDirs = cppClass.getClassName().split("\\.");
        for (int i = 0; i < predeclareClasses.size(); i++) {
            if (cppClass.getClassName().equals(predeclareClasses.get(i))) {
                continue;
            }
            builder.append("#include \"");
            String[] importDirs = predeclareClasses.get(i).split("\\.");
            int j = 0;
            for (int k = 0; k < classDirs.length && k < importDirs.length; k++) {
                if (classDirs[k].equals(importDirs[k])) {
                    j++;
                }
                break;
            }

            int level1 = classDirs.length - j;
            int level2 = importDirs.length - j;
            for (int k = 0; k < level1 - 1; k++) {
                builder.append("../");
            }

            for (int k = j; k < importDirs.length - 1; k++) {
                builder.append(importDirs[k] + "/");
            }
            builder.append(importDirs[importDirs.length - 1] + ".h\"\n");
        }
        return builder.toString();
    }
    public static String getStartNameSpace() {
        return "DEFINE_START_NAMESPACE()\n";
    }
    public static String getEndNameSpace() {
        return "DEFINE_END_NAMESPACE()\n";
    }
    public static String getClassDefine(String className) {
        StringBuilder builder = new StringBuilder();
        builder.append("DEFINE_CLASS(" + className.replace(".h", "") + ")\n");
        return builder.toString();
    }
    public static String getField(CppField cppFiled) {
        StringBuilder builder = new StringBuilder();
        builder.append(getPrivilege(cppFiled.getPrivilege()) + ":\n");
        builder.append("\t" + Util.getType(cppFiled.getFieldType(), Util.RETURN) + " get_" + cppFiled.getFieldName() + "() {\n");
        builder.append("\t\tif (" + cppFiled.getToken() + " == NULL) {\n");
        builder.append("\t\t\t" + cppFiled.getToken() + " = " + "_env->GetFieldID(_clazz, \"" +
                        cppFiled.getFieldName() + "\", \"" + cppFiled.getSignature() + "\");\n");
        builder.append("\t\t}\n");
        builder.append("\t\t" + Util.getType(cppFiled.getFieldType(), Util.RETURN) + " ret = "+
                        Util.getType(cppFiled.getFieldType(), Util.RETURN) + "(" +
                        "_env->" + Util.getFieldID(cppFiled.getFieldType()) +
                        "(_object, " + cppFiled.getToken() + "));\n");
        builder.append("\t\treturn ret;\n");
        builder.append("\t}\n");
        builder.append("\tvoid " + "set_" + cppFiled.getFieldName() + "(" + Util.getType(cppFiled.getFieldType(), Util.PARAM) +
                        " object) {\n");
        builder.append("\t\tif (" + cppFiled.getToken() + " == NULL) {\n");
        builder.append("\t\t\t" + cppFiled.getToken() + " = " + "_env->GetFieldID(_clazz, \"" +
                cppFiled.getFieldName() + "\", \"" + cppFiled.getSignature() + "\");\n");
        builder.append("\t\t}\n");
        builder.append("\t\t" + "_env->" + Util.setFieldID(cppFiled.getFieldType()) +"(_object, " + cppFiled.getToken() +
                        ", " + "object);\n");
        builder.append("\t}\n");
        return builder.toString();
    }

    public static String getDefineID(CppClass cppClass) {
        StringBuilder builder = new StringBuilder();
        builder.append("private:\n");
        ArrayList<String> fieldIDList = cppClass.getFieldIDList();
        for (int i = 0; i < fieldIDList.size(); i++) {
            builder.append("\tjfieldID " + fieldIDList.get(i) + ";\n");
        }
        ArrayList<String> methodIDList = cppClass.getMethodIDList();
        for (int i = 0; i < methodIDList.size(); i++) {
            builder.append("\tjmethodID " + methodIDList.get(i) + ";\n");
        }
        ArrayList<String> initMethodIDList = cppClass.getInitMethodIDList();
        for (int i = 0; i < initMethodIDList.size(); i++) {
            builder.append("\tjmethodID " + initMethodIDList.get(i) + ";\n");
        }

        return builder.toString();
    }

    /**
     * This function is used to create a constructor function default.
     * @param fileName
     * @param className
     * @return
     */
    public static String getDefaultConstructor(String fileName, String className, boolean hasDefault) {
        StringBuilder builder = new StringBuilder();
        builder.append("public:\n");
        fileName = fileName.replace(".h", "");
        if (!hasDefault) {
            builder.append("\t" + fileName + "(): " + fileName
                    + "(std::string(\"" + Util.getSign(className).replace(".", "/") + "\")) {\n");
            builder.append("\t}\n");
        }
        builder.append("\t" + fileName + "(jobject object): " + fileName
                + "(std::string(\"" + Util.getSign(className).replace(".", "/") + "\")) {\n");
        builder.append("\t\tif (object != NULL) {\n");
        builder.append("\t\t\t_object = object;\n");
        builder.append("\t\t}\n");
        builder.append("\t}\n");
        builder.append("\t" + fileName + "(" + fileName + "& obj): " + fileName
                + "(std::string(\"" + Util.getSign(className).replace(".", "/") + "\")) {\n");
        builder.append("\t\t_object = obj._object;\n");
        builder.append("\t}\n");
        builder.append("\toperator jobject(){\n");
        builder.append("\t\treturn _object;\n");
        builder.append("\t}\n");

        return builder.toString();
    }

    public static String getDeconstructor(String fileName) {
        StringBuilder builder = new StringBuilder();
        builder.append("public:\n");
        fileName = fileName.replace(".h", "");
        builder.append("\t~" + fileName + "() {\n");
        builder.append("\t}\n");

        return builder.toString();
    }

    public static String getConstructor(CppClass cppClass) {
        StringBuilder builder = new StringBuilder();
        String className = cppClass.getClassName();
        String name = className.substring(className.lastIndexOf(".") + 1, className.length());
        builder.append("protected:\n");
        builder.append("\t" + name + "(std::string className): JavaObject(className) {\n");
        ArrayList<String> fieldIDList = cppClass.getFieldIDList();
        for (int i = 0; i < fieldIDList.size(); i++) {
            builder.append("\t\t" + fieldIDList.get(i) + " = NULL;\n");
        }
        ArrayList<String> methodIDList = cppClass.getMethodIDList();
        for (int i = 0; i < methodIDList.size(); i++) {
            builder.append("\t\t" + methodIDList.get(i) + " = NULL;\n");
        }
        ArrayList<String> initMethodIDList = cppClass.getInitMethodIDList();
        for (int i = 0; i < initMethodIDList.size(); i++) {
            builder.append("\t\t" + initMethodIDList.get(i) + " = NULL;\n");
        }
        builder.append("\t}\n");
        return builder.toString();
    }
    public static String getConstructor(CppConstructor cppConstructor, String className) {
        StringBuilder builder = new StringBuilder();
        builder.append(getPrivilege(cppConstructor.getPrivilege()) + ":\n");
        builder.append("\t" + cppConstructor.getConstructorName() + "(");
        ArrayList<CppParameter> params = cppConstructor.getParams();
        for (int i = 0; i < params.size(); i++) {
            CppParameter param = params.get(i);
            builder.append(Util.getType(param.getParamType(), Util.PARAM) + " " + param.getParamName());
            if (i != params.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append("): " + cppConstructor.getConstructorName() + "(std::string(\"" +
                        Util.getSign(className).replace(".", "/") + "\")) {\n");
        builder.append("\t\tif (" + cppConstructor.getToken() + " == NULL) {\n");
        builder.append("\t\t\t" + cppConstructor.getToken() + " = _env->GetMethodID(_clazz, \"<init>\", \"" +
                        cppConstructor.getSignature() + "\");\n");
        builder.append("\t\t}\n");
        builder.append("\t\t_object = _env->NewObject(_clazz, " + cppConstructor.getToken());
        for (int i = 0; i < params.size(); i++) {
            builder.append(", ");
            boolean isObject = false;
            if (Util.isObjectOrNot(params.get(i).getParamType())) {
                isObject = true;
                builder.append("jobject(");
            }
            builder.append(params.get(i).getParamName());
            if (isObject) {
                builder.append(")");
            }
        }
        builder.append(");\n");
        builder.append("\t}\n");
        return builder.toString();
    }
    public static String getMethod(CppMethod cppMethod) {
        StringBuilder builder = new StringBuilder();
        builder.append(getPrivilege(cppMethod.getPrivilege()) + ":\n");
        builder.append("\t" + Util.getType(cppMethod.getMethodType(), Util.RETURN) + " " + cppMethod.getMethodName() + "(");
        ArrayList<CppParameter> params = cppMethod.getParams();
        for (int i = 0; i < params.size(); i++) {
            CppParameter param = params.get(i);
            builder.append(Util.getType(param.getParamType(), Util.PARAM) + " " + param.getParamName());
            if (i != params.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append(") {\n");
        builder.append("\t\tif (" + cppMethod.getToken() + " == NULL) {\n");
        builder.append("\t\t\t" + cppMethod.getToken() + " = _env->GetMethodID(_clazz, \"" +
                cppMethod.getMethodName() + "\", \"" +
                cppMethod.getSignature() + "\");\n");
        builder.append("\t\t}\n");
        builder.append("\t\t");
        boolean isReturn =false;
        if (!Util.getType(cppMethod.getMethodType(), Util.RETURN).equals("void")) {
            isReturn = true;
            builder.append(Util.getType(cppMethod.getMethodType(), Util.RETURN) + " ret = " +
                    Util.getType(cppMethod.getMethodType(), Util.RETURN) + "(");
        }
        builder.append("_env->" + Util.getMethodID(cppMethod.getMethodType()) + "(_object, " +
                        cppMethod.getToken());
        for (int i = 0; i < params.size(); i++) {
            builder.append(", ");
            boolean isObject = false;
            if (Util.isObjectOrNot(params.get(i).getParamType())) {
                isObject = true;
                builder.append("jobject(");
            }
            builder.append(params.get(i).getParamName());
            if (isObject) {
                builder.append(")");
            }
        }
        if (isReturn) {
            builder.append(")");
        }
        builder.append(");\n");
        if (isReturn) {
            builder.append("\t\treturn ret;\n");
        }
        builder.append("\t}\n");
        return builder.toString();
    }
    public static String getEndDefine(String className) {
        return "DEFINE_END()\n";
    }
    public static String getEndGrand() {
        return "#endif\n";
    }
    public static String getPrivilege(String privilege) {
        if(privilege.contains("private")) {
            return "private";
        } else if (privilege.contains("public")) {
            return "public";
        } else if (privilege.contains("protected")) {
            return "protected";
        }
        return "protected";
    }
}
