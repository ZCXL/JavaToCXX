package com.javatocpp.log;

/**
 * Created by ZhuChao on 2017/3/14.
 */
public class Log {
    public static void error(String format, Object... args) {
        System.out.printf(format, args);
    }
    public static void warning(String format, Object... args) {

    }
    public static void notice(String format, Object... args) {

    }
}
