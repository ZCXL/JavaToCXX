package com.javatocpp.main;

import com.javatocpp.convert.Convert;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by ZhuChao on 2017/3/14.
 */
public class Main {
    public static ArrayList<String> classpath = new ArrayList<>();
    public static ArrayList<File> files = new ArrayList<>();
    public static void printHelp() {
        System.out.println("Usage: javatocpp [file]...");
    }
    public static boolean checkFile(String name) {
        if (name.endsWith(".cpp") || name.endsWith(".h")) {
            return true;
        }
        return false;
    }
    public static void getFile(File file) {
        if (file.isFile()) {
            if (checkFile(file.getName())) {
                files.add(file);
            }
        } else if (file.isDirectory()) {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isFile()) {
                    if (checkFile(fileList[i].getName())) {
                        files.add(fileList[i]);
                    }
                } else if (fileList[i].isDirectory()) {
                    getFile(fileList[i]);
                }
            }
        }
    }
    public static void main(String []args) {
        if (args.length < 1) {
            printHelp();
        }
        for (int i = 0; i < args.length; i++) {
            classpath.add(args[i]);
        }

        for (int i = 0; i < classpath.size(); i++) {
            getFile(new File(classpath.get(i)));
        }

        Convert.javaToCPP(files);
    }
}
