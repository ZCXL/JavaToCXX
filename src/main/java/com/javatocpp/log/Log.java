package com.javatocpp.log;

/**
 * Created by ZhuChao on 2017/3/14.
 */
public class Log {
    public static void error(String format, Object... args) {
        String errorFormat = "ERROR:\n\t";
        System.out.printf(errorFormat + format, args);
    }
    public static void warning(String format, Object... args) {
        String warningFormat = "WARNING:\n\t";
        System.out.printf(warningFormat + format, args);
    }
    public static void notice(String format, Object... args) {
        String noticeFormat = "NOTICE:\n\t";
        System.out.printf(noticeFormat + format, args);
    }
}
