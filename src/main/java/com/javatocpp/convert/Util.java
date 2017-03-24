package com.javatocpp.convert;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by ZhuChao on 2017/3/16.
 */
public class Util {
    /**
     * Fetch the type signature for java object,
     * according the type string.
     * 根据类型字段推算出Java的类型签名
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
                return type;
            } else {
                return "L" + type + ";";
            }
        }
    }

    /**
     * Fetch type string for function or field,
     * according the java type signature.
     * @param type
     * @return
     */
    public static String getUnsign(String type) {
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
            return "object";
        }
    }


    /**
     * Transfer java type to C++ type.
     * @param type
     * @return
     */
    public static String getType(String type) {
        if (type.equals("void") || type.equals("int")
                || type.equals("long") || type.equals("float")
                || type.equals("double") || type.equals("short")
                || type.equals("byte") || type.equals("char")) {
            return "j" + type;
        } else if (type.startsWith("[")) {
            return "j" + getUnsign(type.substring(1)) + "Array";
        }
        return "jobject";
    }

    /**
     * 
     * @param type
     * @return
     */
    public static String getFieldType(String type) {
        if (type.equals("boolean")) {
            return "BooleanField";
        } else if (type.equals("void")) {
            return "VoidField";
        } else if (type.equals("int")) {
            return "IntField";
        } else if (type.equals("long")) {
            return "LongField";
        } else if (type.equals("float")) {
            return "FloatField";
        } else if (type.equals("double")) {
            return "DoubleField";
        } else if (type.equals("short")) {
            return "ShortField";
        } else if (type.equals("byte")) {
            return "ByteField";
        } else if (type.equals("char")) {
            return "CharField";
        } else {
            if (type.startsWith("[")) {
                return getUnsign(type.substring(1)) + "ArrayRegion";
            } else {
                return "ObjectField";
            }
        }
    }

    public static String getFieldID(String type) {
        return "Get" + getFieldType(type);
    }

    public static String setFieldID(String type) {
        return "Set" + getFieldType(type);
    }

    public static String build(String origin ,String charsetName){
        if(origin == null )
            return null ;

        StringBuilder sb = new StringBuilder() ;
        MessageDigest digest = null ;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null ;
        }

        //生成一组length=16的byte数组
        byte[] bs = digest.digest(origin.getBytes(Charset.forName(charsetName))) ;

        for (int i = 0; i < bs.length; i++) {
            int c = bs[i] & 0xFF ; //byte转int为了不丢失符号位， 所以&0xFF
            if(c < 16){ //如果c小于16，就说明，可以只用1位16进制来表示， 那么在前面补一个0
                sb.append("0");
            }
            sb.append(Integer.toHexString(c)) ;
        }
        return sb.toString() ;
    }

    public static String getToken(String str) {
        return build(str, "UTF-8");
    }
}
