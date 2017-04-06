package com.javatocpp.convert;

import com.javatocpp.log.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Created by ZhuChao on 2017/3/15.
 */
public class DefineHFile {
    public static boolean defineHFile(CppClass cppClass, FileOutputStream outer) {
        try {
            outer.write(getClassDefine(cppClass.getFileName()).getBytes("utf-8"));

            /**
             * Define variable of method id or field id.
             */
            outer.write(getDefineID(cppClass).getBytes("utf-8"));

            outer.write(getDefaultConstructor(cppClass.getFileName(),
                    cppClass.getClassName(),
                    cppClass.isHasDefaultConstructor()).getBytes("utf-8"));
            outer.write(getDeconstructor(cppClass.getFileName()).getBytes("utf-8"));
            /**
             * Print the field of a java object
             */
            ArrayList<CppField> fileds = cppClass.getFieldList();
            for (int i = 0; i < fileds.size(); i++) {
                outer.write(getField(fileds.get(i), cppClass).getBytes("utf-8"));
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
            HashMap<String, ArrayList<CppMethod>> methodSet = cppClass.getMethodSet();
            for (String key : methodSet.keySet()) {
                ArrayList<CppMethod> methods = methodSet.get(key);
                outer.write(getMethod(methods, key, cppClass).getBytes("utf-8"));
            }
            ArrayList<CppField> staticFields = cppClass.getStaticFieldList();
            HashMap<String, ArrayList<CppMethod>> staticMethodSet = cppClass.getStaticMethodSet();
            if (staticFields.size() != 0 || staticMethodSet.size() != 0) {
                outer.write(getDeclareStaticClass().getBytes("utf-8"));
                outer.write(getStaticDefineID(cppClass).getBytes("utf-8"));
            }

            ArrayList<CppClass> innerClasses = cppClass.getInnerClasses();
            for (int i = 0; i < innerClasses.size(); i++) {
                CppClass innerClass = innerClasses.get(i);
                String tmp = getPrivilege(innerClass.getPrivilege()) + ":\n";
                outer.write(tmp.getBytes("utf-8"));
                defineHFile(innerClass, outer);
            }

            outer.write(getEndDefine(cppClass.getFileName()).getBytes("utf-8"));
        }catch (Exception e) {
            Log.error("Msg: produce class file[%s] error. error[%s]\n", cppClass.getClassName(), e.toString());
            return false;
        }
        return true;
    }
    public static boolean produceStaticCode(CppClass cppClass, FileOutputStream outer) {
        try {
            ArrayList<CppField> staticFields = cppClass.getStaticFieldList();
            HashMap<String, ArrayList<CppMethod>> staticMethodSet = cppClass.getStaticMethodSet();
            if (staticFields.size() != 0 || staticMethodSet.size() != 0) {
                outer.write(getMakeStaticClass(cppClass.getFileName(),
                        cppClass.getClassName(), cppClass).getBytes("utf-8"));

                for (int i = 0; i < staticFields.size(); i++) {
                    outer.write(getStaticField(staticFields.get(i), cppClass.getFileName(), cppClass)
                            .getBytes("utf-8"));
                }

                for (String key : staticMethodSet.keySet()) {
                    ArrayList<CppMethod> methods = staticMethodSet.get(key);
                    outer.write(getStaticMethod(methods, cppClass.getFileName(), cppClass)
                            .getBytes("utf-8"));
                }
            }
            ArrayList<CppClass> innerClasses = cppClass.getInnerClasses();
            for (int i = 0; i < innerClasses.size(); i++) {
                produceStaticCode(innerClasses.get(i), outer);
            }
        }catch (Exception e) {
            Log.error("Msg: produce static code error. file[%s] error[%s]\n", cppClass.getClassName(), e.toString());
            return false;
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

        HashSet<String> importClassSet = new HashSet<>();
        getInclude(cppClass, importClassSet);

        for(String instruction: importClassSet) {
            builder.append(instruction);
        }
        return builder.toString();
    }

    /**
     * Get include instruction recursively.
     * @param cppClass
     * @param importClassSet
     */
    public static void getInclude(CppClass cppClass, HashSet<String> importClassSet) {
        ArrayList<String> predeclareClasses = cppClass.getPredeclareClass();
        String className = cppClass.getClassName();
        /**
         * filter outer class.
         */
        if (className.contains("$")) {
            className = className.substring(0, className.indexOf("$"));
        }
        String[] classDirs = className.split("\\.");
        for (int i = 0; i < predeclareClasses.size(); i++) {
            if (className.equals(predeclareClasses.get(i))) {
                continue;
            }

            String importInstruction = "";
            importInstruction += "#include \"";
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
                importInstruction += "../";
            }

            for (int k = j; k < importDirs.length - 1; k++) {
                importInstruction += importDirs[k] + "/";
            }
            importInstruction += importDirs[importDirs.length - 1] + ".h\"\n";
            importClassSet.add(importInstruction);
        }
        ArrayList<CppClass> innerClasses = cppClass.getInnerClasses();
        for (int i = 0; i < innerClasses.size(); i++) {
            getInclude(innerClasses.get(i), importClassSet);
        }
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
    public static String getField(CppField cppFiled, CppClass cppClass) {
        StringBuilder builder = new StringBuilder();
        if (!cppFiled.isStatic()) {
            builder.append(getPrivilege(cppFiled.getPrivilege()) + ":\n");
            builder.append("\t" + Util.getType(cppFiled.getFieldType(), Util.RETURN) + " get_" + cppFiled.getFieldName() + "() {\n");
            builder.append("\t\tif (" + cppFiled.getToken() + " == NULL) {\n");
            builder.append("\t\t\t" + cppFiled.getToken() + " = " + "_env->GetFieldID(_clazz, \"" +
                    cppFiled.getFieldName() + "\", \"" + cppFiled.getSignature() + "\");\n");
            builder.append("\t\t}\n");
            builder.append("\t\t" + Util.getType(cppFiled.getFieldType(), Util.RETURN) + " ret = " +
                    Util.getType(cppFiled.getFieldType(), Util.RETURN) + "(" +
                    "_env->" + Util.getFieldID(cppFiled.getFieldType()) +
                    "(_object, " + cppFiled.getToken() + "));\n");
            builder.append("\t\treturn ret;\n");
            builder.append("\t}\n");
            if (!cppFiled.isFinal()) {
                builder.append("\tvoid " + "set_" + cppFiled.getFieldName() + "("
                        + Util.getType(cppFiled.getFieldType(), Util.PARAM)
                        + " object) {\n");
                builder.append("\t\tif (" + cppFiled.getToken() + " == NULL) {\n");
                builder.append("\t\t\t" + cppFiled.getToken() + " = " + "_env->GetFieldID(_clazz, \"" +
                        cppFiled.getFieldName() + "\", \"" + cppFiled.getSignature() + "\");\n");
                builder.append("\t\t}\n");
                builder.append("\t\t" + "_env->" + Util.setFieldID(cppFiled.getFieldType())
                        + "(_object, " + cppFiled.getToken() + ", " + "object);\n");
                builder.append("\t}\n");
            }
        } else {
            builder.append(getPrivilege(cppFiled.getPrivilege()) + ":\n");
            builder.append("\tstatic " + Util.getType(cppFiled.getFieldType(), Util.RETURN)
                    + " get_" + cppFiled.getFieldName() + "();\n");
            if (!cppFiled.isFinal()) {
                builder.append("\tstatic void " + "set_" + cppFiled.getFieldName() + "("
                        + Util.getType(cppFiled.getFieldType(), Util.PARAM)
                        + " object);\n");
            }
            cppClass.addStaticField(cppFiled);
        }
        return builder.toString();
    }

    public static String getStaticField(CppField cppFiled, String fileName, CppClass cppClass) {
        StringBuilder builder = new StringBuilder();

        builder.append(Util.getType(cppFiled.getFieldType(), Util.RETURN) + " "
                + cppClass.getStaticPrefix()
                + fileName + "::get_" + cppFiled.getFieldName() + "() {\n");
        builder.append("\tjclass object_class = get_java_static_class();\n");
        builder.append("\tJNIEnv* env = get_java_virtual_machine_env();\n");
        builder.append("\tif (" + cppFiled.getToken() + " == NULL) {\n");
        builder.append("\t\t" + cppFiled.getToken() + " = " + "env->GetStaticFieldID(object_class, \"" +
                cppFiled.getFieldName() + "\", \"" + cppFiled.getSignature() + "\");\n");
        builder.append("\t}\n");
        builder.append("\t" + Util.getType(cppFiled.getFieldType(), Util.RETURN) + " ret = " +
                Util.getType(cppFiled.getFieldType(), Util.RETURN) + "(" +
                "env->" + Util.getStaticFieldID(cppFiled.getFieldType()) +
                "(object_class, " + cppFiled.getToken() + "));\n");
        builder.append("\treturn ret;\n");
        builder.append("}\n");
        if (!cppFiled.isFinal()) {
            builder.append("void " + cppClass.getStaticPrefix()
                    + fileName + "::set_" + cppFiled.getFieldName() + "("
                    + Util.getType(cppFiled.getFieldType(), Util.PARAM)
                    + " object) {\n");
            builder.append("\tjclass object_class = get_java_static_class();\n");
            builder.append("\tJNIEnv* env = get_java_virtual_machine_env();\n");
            builder.append("\tif (" + cppFiled.getToken() + " == NULL) {\n");
            builder.append("\t\t" + cppFiled.getToken() + " = " + "env->GetStaticFieldID(object_class, \"" +
                    cppFiled.getFieldName() + "\", \"" + cppFiled.getSignature() + "\");\n");
            builder.append("\t}\n");
            builder.append("\t" + "env->" + Util.setStaticFieldID(cppFiled.getFieldType())
                    + "(object_class, " + cppFiled.getToken() + ", " + "object);\n");
            builder.append("}\n");
        }

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

    public static String getStaticDefineID(CppClass cppClass) {
        ArrayList<String> staticFieldIDList = cppClass.getStaticFieldIDList();
        ArrayList<String> staticMethodIDList = cppClass.getStaticMethodIDList();
        if (staticFieldIDList.size() == 0 && staticMethodIDList.size() == 0) {
            return "\n";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("private:\n");
        for (int i = 0; i < staticFieldIDList.size(); i++) {
            builder.append("\tstatic jfieldID " + staticFieldIDList.get(i) + ";\n");
        }
        for (int i = 0; i < staticMethodIDList.size(); i++) {
            builder.append("\tstatic jmethodID " + staticMethodIDList.get(i) + ";\n");
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
        if (!hasDefault) {
            builder.append("\t" + fileName + "(): " + fileName
                    + "(std::string(\"" + Util.getSign(className) + "\")) {\n");
            builder.append("\t}\n");
        }
        builder.append("\t" + fileName + "(jobject object): " + fileName
                + "(std::string(\"" + Util.getSign(className) + "\")) {\n");
        builder.append("\t\tif (object != NULL) {\n");
        builder.append("\t\t\t_object = object;\n");
        builder.append("\t\t}\n");
        builder.append("\t}\n");
        builder.append("\t" + fileName + "(" + fileName + "& obj): " + fileName
                + "(std::string(\"" + Util.getSign(className) + "\")) {\n");
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
        builder.append("\t~" + fileName + "() {\n");
        builder.append("\t}\n");

        return builder.toString();
    }

    public static String getConstructor(CppClass cppClass) {
        StringBuilder builder = new StringBuilder();
        String className = cppClass.getClassName();
        String name = cppClass.getFileName();
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
                        Util.getSign(className) + "\")) {\n");
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
    public static String getMethod(ArrayList<CppMethod> methods, String key, CppClass cppClass) {
        StringBuilder builder = new StringBuilder();
        CppMethod cppMethod = methods.get(0);
        builder.append(getPrivilege(cppMethod.getPrivilege()) + ":\n");
        String methodName = "";
        if (cppMethod.isConflict()) {
            methodName += "j";
        }
        methodName += cppMethod.getMethodName();
        if (cppMethod.isStatic()) {
            builder.append("\tstatic " + Util.getType(cppMethod.getMethodType(), Util.RETURN) + " " + methodName + "(");
        } else {
            builder.append("\t" + Util.getType(cppMethod.getMethodType(), Util.RETURN) + " " + methodName + "(");
        }
        ArrayList<CppParameter> params = cppMethod.getParams();
        for (int i = 0; i < params.size(); i++) {
            CppParameter param = params.get(i);
            builder.append(Util.getType(param.getParamType(), Util.PARAM) + " " + param.getParamName());
            if (i != params.size() - 1) {
                builder.append(", ");
            }
        }
        /**
         * Add default parameter
         */
        if (params.size() != 0) {
            builder.append(", ");
        }
        if (cppMethod.isStatic()) {
            builder.append("std::string sign");
            builder.append(");\n");
            cppClass.addStaticMethod(key, methods);
            return builder.toString();
        }
        builder.append("std::string sign = \"" + Util.getType(cppMethod.getMethodType(), Util.PARAM) + "\"");
        builder.append(") {\n");
        builder.append("\t\tjmethodID method_id = NULL;\n");
        for (int i = 0; i < methods.size(); i++) {
            CppMethod method = methods.get(i);
            String sign = Util.getType(method.getMethodType(), Util.PARAM);
            builder.append("\t\tif (sign == \"" + sign + "\") {\n");
            builder.append("\t\t\tif (" + method.getToken() + " == NULL) {\n");
            builder.append("\t\t\t\t" + method.getToken() + " = _env->GetMethodID(_clazz, \"" +
                    method.getMethodName() + "\", \"" +
                    method.getSignature() + "\");\n");
            builder.append("\t\t\t\tmethod_id = " + method.getToken() + ";\n");
            builder.append("\t\t\t} else {\n");
            builder.append("\t\t\t\tmethod_id = " + method.getToken() + ";\n");
            builder.append("\t\t\t}\n");
            builder.append("\t\t}\n");
        }

        builder.append("\t\t");
        boolean isReturn =false;
        if (!Util.getType(cppMethod.getMethodType(), Util.RETURN).equals("void")) {
            isReturn = true;
            builder.append(Util.getType(cppMethod.getMethodType(), Util.RETURN) + " ret = " +
                    Util.getType(cppMethod.getMethodType(), Util.RETURN) + "(");
        }
        builder.append("_env->" + Util.getMethodID(cppMethod.getMethodType()) + "(_object, method_id");
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

    public static String getStaticMethod(ArrayList<CppMethod> methods, String fileName, CppClass cppClass) {
        StringBuilder builder = new StringBuilder();
        CppMethod cppMethod = methods.get(0);
        String methodName = "";
        if (cppMethod.isConflict()) {
            methodName += "j";
        }
        methodName += cppMethod.getMethodName();
        builder.append(Util.getType(cppMethod.getMethodType(), Util.RETURN) + " "
                + cppClass.getStaticPrefix()
                + fileName + "::" + methodName + "(");
        ArrayList<CppParameter> params = cppMethod.getParams();
        for (int i = 0; i < params.size(); i++) {
            CppParameter param = params.get(i);
            builder.append(Util.getType(param.getParamType(), Util.PARAM) + " " + param.getParamName());
            if (i != params.size() - 1) {
                builder.append(", ");
            }
        }
        /**
         * Add default parameter
         */
        if (params.size() != 0) {
            builder.append(", ");
        }
        builder.append("std::string sign = \"" + Util.getType(cppMethod.getMethodType(), Util.PARAM) + "\"");
        builder.append(") {\n");
        builder.append("\tjmethodID method_id = NULL;\n");
        builder.append("\tjclass object_class = get_java_static_class();\n");
        builder.append("\tJNIEnv* env = get_java_virtual_machine_env();\n");
        for (int i = 0; i < methods.size(); i++) {
            CppMethod method = methods.get(i);
            String sign = Util.getType(method.getMethodType(), Util.PARAM);
            builder.append("\tif (sign == \"" + sign + "\") {\n");
            builder.append("\t\tif (" + method.getToken() + " == NULL) {\n");
            builder.append("\t\t\t" + method.getToken() + " = env->GetStaticMethodID(object_class, \"" +
                    method.getMethodName() + "\", \"" +
                    method.getSignature() + "\");\n");
            builder.append("\t\t\tmethod_id = " + method.getToken() + ";\n");
            builder.append("\t\t} else {\n");
            builder.append("\t\t\tmethod_id = " + method.getToken() + ";\n");
            builder.append("\t\t}\n");
            builder.append("\t}\n");
        }

        builder.append("\t");
        boolean isReturn =false;
        if (!Util.getType(cppMethod.getMethodType(), Util.RETURN).equals("void")) {
            isReturn = true;
            builder.append(Util.getType(cppMethod.getMethodType(), Util.RETURN) + " ret = " +
                    Util.getType(cppMethod.getMethodType(), Util.RETURN) + "(");
        }
        builder.append("env->" + Util.getStaticMethodID(cppMethod.getMethodType()) + "(object_class, method_id");
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
            builder.append("\treturn ret;\n");
        }
        builder.append("}\n");
        return builder.toString();
    }
    public static String getDeclareStaticClass() {
        StringBuilder builder = new StringBuilder();

        builder.append("private:\n");
        builder.append("\tstatic bridge::BridgeObject static_bridge_object;\n");
        builder.append("\tstatic jclass get_java_static_class();\n");
        builder.append("\tstatic JNIEnv* get_java_virtual_machine_env();\n");

        return builder.toString();
    }
    public static String getMakeStaticClass(String fileName, String className, CppClass cppClass) {
        StringBuilder builder = new StringBuilder();
        className = Util.getSign(className);

        ArrayList<String> staticFieldIDList = cppClass.getStaticFieldIDList();
        ArrayList<String> staticMethodIDList = cppClass.getStaticMethodIDList();
        for (int i = 0; i < staticFieldIDList.size(); i++) {
            builder.append("jfieldID " + cppClass.getStaticPrefix()
                    + fileName + "::" + staticFieldIDList.get(i) + " = NULL;\n");
        }
        for (int i = 0; i < staticMethodIDList.size(); i++) {
            builder.append("jmethodID " + cppClass.getStaticPrefix()
                    + fileName + "::" + staticMethodIDList.get(i) + " = NULL;\n");
        }
        builder.append("bridge::BridgeObject " + cppClass.getStaticPrefix()
                + fileName + "::static_bridge_object(\"" + className + "\");\n");
        builder.append("jclass " + cppClass.getStaticPrefix()
                + fileName + "::get_java_static_class() {\n");
        builder.append("\treturn static_bridge_object.getClass();\n");
        builder.append("}\n");
        builder.append("JNIEnv* " + cppClass.getStaticPrefix()
                + fileName + "::get_java_virtual_machine_env() {\n");
        builder.append("\treturn static_bridge_object.getEnv();\n");
        builder.append("}\n");

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
