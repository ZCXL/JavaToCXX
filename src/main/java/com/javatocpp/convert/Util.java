package com.javatocpp.convert;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by ZhuChao on 2017/3/16.
 */
public class Util {
    public final static int RETURN = 1;
    public final static int PARAM  = 2;
    public static String[] keywords = {
            "asm", "auto", "bool", "break", "case", "catch", "char", "class", "const", "const_cast", "continue",
            "default", "delete", "do", "double", "dynamic_cast", "else", "enum", "explicit", "export", "extern",
            "false", "float", "for", "for", "friend", "goto", "if", "inline", "int", "long", "mutable", "namespace",
            "new", "operator", "private", "protected", "public", "register", "reinterpret_cast", "return", "short",
            "signed", "sizeof", "static", "static_cast", "struct", "switch", "template", "this", "throw", "try", "true",
            "typedef", "typeid", "typename", "union", "unsigned", "using", "virtual", "void", "volatile", "wchar_t",
            "while"
    };
    /**
     * Fetch the type signature for java object,
     * according the type string.
     * 根据类型字段推算出Java的类型签名
     *
     * @param type
     * @return
     */
    public static String getSign(String type) {
        if (type.equals("boolean")) {
            return "Z";
        } else if (type.equals("void")) {
            return "V";
        } else if (type.equals("int")) {
            return "I";
        } else if (type.equals("long")) {
            return "J";
        } else if (type.equals("float")) {
            return "F";
        } else if (type.equals("double")) {
            return "D";
        } else if (type.equals("short")) {
            return "S";
        } else if (type.equals("byte")) {
            return "B";
        } else if (type.equals("char")) {
            return "C";
        } else {
            if (type.startsWith("[")) {
                return type.replaceAll("\\.", "/");
            } else {
                return "L" + type.replaceAll("\\.", "/") + ";";
            }
        }
    }

    /**
     * Fetch type string for function or field,
     * according the java type signature.
     *
     * @param type
     * @return
     */
    public static String getUnsign(String type, boolean isImport) {
        if (type.equals("Z")) {
            return "boolean";
        } else if (type.equals("V")) {
            return "void";
        } else if (type.equals("I")) {
            return "int";
        } else if (type.equals("J")) {
            return "long";
        } else if (type.equals("F")) {
            return "float";
        } else if (type.equals("D")) {
            return "double";
        } else if (type.equals("S")) {
            return "short";
        } else if (type.equals("B")) {
            return "byte";
        } else if (type.equals("C")) {
            return "char";
        } else {
            if (type.startsWith("L")) {
                type = type.substring(1);
            }
            String className = type.replaceAll(";", "");
            String[] dirs = className.split("\\.");

            /**
             * Process some classes that is not imported,
             * but used.
             */
            if (!Convert.findClass(className) && isImport) {
                Convert.addPredeclareClass(className);
            }
            return dirs[dirs.length - 1];
        }
    }


    /**
     * Transfer java type to C++ type.
     * @param type
     * @return
     */
    public static String getType(String type, int result) {
        if (type.equals("void")) {
            return "void";
        } else if (type.equals("int") || type.equals("boolean")
                || type.equals("long") || type.equals("float")
                || type.equals("double") || type.equals("short")
                || type.equals("byte") || type.equals("char")) {
            return "bridge_" + type;
        } else if (type.startsWith("[")) {
            String subType = type.substring(1);
            String unsign = getUnsign(subType, true);
            if (unsign.equals("int") || unsign.equals("boolean")
                    || unsign.equals("long") || unsign.equals("float")
                    || unsign.equals("double") || unsign.equals("short")
                    || unsign.equals("byte") || unsign.equals("char")) {
                return "bridge_" + unsign + "_array";
            } else {
                if (result == RETURN) {
                    return "jobject";
                } else {
                    return "JavaObjectArray<" + unsign + ">";
                }
            }
        } else {
            if (type.equals("java.lang.String")) {
                return "bridge_string";
            }
            String[] dirs = type.split("\\.");

            /**
             * Process some classes that is not imported,
             * but used.
             */
            if (!Convert.findClass(type)) {
                Convert.addPredeclareClass(type);
            }
            if (result == RETURN) {
                return "jobject";
            } else {
                return dirs[dirs.length - 1];
            }
        }
    }

    public static boolean isObjectOrNot(String type) {
        if (type.equals("int") || type.equals("boolean")
                || type.equals("long") || type.equals("float")
                || type.equals("double") || type.equals("short")
                || type.equals("byte") || type.equals("char")) {
            return false;
        }
        return true;
    }

    /**
     * Check some type wheather is basic data type or not.
     * @param type
     * @return
     */
    public static String getNotInnerType(String type) {
        if (type.equals("void")) {
            return "";
        } else if (type.equals("int") || type.equals("boolean")
                || type.equals("long") || type.equals("float")
                || type.equals("double") || type.equals("short")
                || type.equals("byte") || type.equals("char")) {
            return "";
        } else if (type.startsWith("[")) {
            String subType = type.substring(1);
            String unsign = getUnsign(subType, false);
            if (unsign.equals("int") || unsign.equals("boolean")
                    || unsign.equals("long") || unsign.equals("float")
                    || unsign.equals("double") || unsign.equals("short")
                    || unsign.equals("byte") || unsign.equals("char")) {
                return "";
            } else {
                if (subType.startsWith("L")) {
                    subType = subType.substring(1);
                    subType = subType.replaceAll(";", "");
                }

                return subType;
            }
        } else {
            if (type.equals("java.lang.String")) {
                return "";
            }

            if (type.startsWith("L")) {
                type = type.substring(1);
                type = type.replaceAll(";", "");
            }

            return type;
        }
    }
    /**
     * @param type
     * @return
     */
    public static String getFieldType(String type) {
        if (type.equals("boolean")) {
            return "Boolean";
        } else if (type.equals("void")) {
            return "Void";
        } else if (type.equals("int")) {
            return "Int";
        } else if (type.equals("long")) {
            return "Long";
        } else if (type.equals("float")) {
            return "Float";
        } else if (type.equals("double")) {
            return "Double";
        } else if (type.equals("short")) {
            return "Short";
        } else if (type.equals("byte")) {
            return "Byte";
        } else if (type.equals("char")) {
            return "Char";
        } else {
            return "Object";
        }
    }

    public static String getFieldID(String type) {
        return "Get" + getFieldType(type) + "Field";
    }

    public static String getMethodID(String type) {
        return "Call" + getFieldType(type) + "Method";
    }

    public static String setFieldID(String type) {
        return "Set" + getFieldType(type) + "Field";
    }

    public static String getStaticFieldID(String type) {
        return "GetStatic" + getFieldType(type) + "Field";
    }

    public static String getStaticMethodID(String type) {
        return "CallStatic" + getFieldType(type) + "Method";
    }

    public static String setStaticFieldID(String type) {
        return "SetStatic" + getFieldType(type) + "Field";
    }


    /**
     * Get a md5 value of a string, which distinguish java overload method.
     * @param origin
     * @param charsetName
     * @return
     */
    public static String build(String origin, String charsetName) {
        if (origin == null)
            return null;

        StringBuilder sb = new StringBuilder();
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

        //生成一组length=16的byte数组
        byte[] bs = digest.digest(origin.getBytes(Charset.forName(charsetName)));

        for (int i = 0; i < bs.length; i++) {
            int c = bs[i] & 0xFF; //byte转int为了不丢失符号位， 所以&0xFF
            if (c < 16) { //如果c小于16，就说明，可以只用1位16进制来表示， 那么在前面补一个0
                sb.append("0");
            }
            sb.append(Integer.toHexString(c));
        }
        return sb.toString();
    }

    /**
     * Produce a token for field, method, or constructor.
     *
     * @param str
     * @return
     */
    public static String getToken(String str) {
        return build(str, "UTF-8");
    }

    /**
     * This function is used to print bridge framework lisence.
     * @return
     */
    public static String getLisence() {
        StringBuilder builder = new StringBuilder();
        builder.append(" * \tThis file generated by Java-to-C++ mixed programming framework, \n");
        builder.append(" * \twhich is developed by author Brother-Chao, is used to create a \n");
        builder.append(" * \tbridge linking java virtual machine with c++ runtime. It help \n");
        builder.append(" * \tprogrammer invoking java class in C++ extra-easily, just inclu- \n");
        builder.append(" * \tdeing this header file in their C++ code file. You can not only \n");
        builder.append(" * \tuse all variables or invoke all function whose privilege is pu-\n");
        builder.append(" * \tblic declared in java class file, but also inherit java class s-\n");
        builder.append(" * \ttarting with \"J\". Generally speaking, it's very useful.\n");
        return builder.toString();
    }

    /**
     * This function is used to filter C++ reserved keyword,
     * which is used in java code.
     * @param word
     * @return
     */
    public static boolean filterKeyword(String word) {
        for (int i = 0; i < keywords.length; i++) {
            if (word.equals(keywords[i])) {
                return true;
            }
        }
        return false;
    }
}
