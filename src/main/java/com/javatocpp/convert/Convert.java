package com.javatocpp.convert;

import com.javatocpp.log.Log;

import java.io.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
/**
 * Created by ZhuChao on 2017/3/14.
 */
public class Convert {
    public final static String PREFIX = "jni_bridge/";
    /**
     * An array list to save the class object that has imported.
     */
    public final static HashSet<String> classSet = new HashSet<>();
    /**
     * An array list to save the class object used in class file imported.
     */
    public final static ArrayList<String> predeclareList = new ArrayList<>();

    /**
     * This function is used to create a import file as a file to
     * contain all classes and predeclare classes.
     * @return
     */
    public static boolean createImportFile() {
        String dirPath = PREFIX.replace("/", "") + File.separator;
        File rDir = new File(dirPath);
        if (!rDir.isDirectory()) {
            rDir.deleteOnExit();
        }
        if (!rDir.exists()) {
            rDir.mkdirs();
        }
        File importFile = new File(dirPath + "import.h");
        if (importFile.exists()) {
            importFile.delete();
        }
        FileOutputStream outer = null;
        try {
            outer = new FileOutputStream(importFile);
            outer.write(DefineHFile.getFileInfo("import.h", "import").getBytes("utf-8"));
            outer.write(DefineHFile.getStartGrand("import").getBytes("utf-8"));
            outer.write("\n".getBytes("utf-8"));
            for (String h : classSet) {
                String header = h.replace(".h", "");
                header = header.replaceAll("\\.", "/") + ".h";
                header = "#include \"" + header + "\"\n";
                outer.write(header.getBytes("utf-8"));
            }
            for (int i = 0; i < predeclareList.size(); i++) {
                String header = predeclareList.get(i).replace(".h", "");
                header = header.replaceAll("\\.", "/") + ".h";
                header = "#include \"" + header + "\"\n";
                outer.write(header.getBytes("utf-8"));
            }
            outer.write("\n".getBytes("utf-8"));
            outer.write(DefineHFile.getEndGrand().getBytes("utf-8"));
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
    /**
     * Read all header file or source file of C++,
     * and fetch the import command
     * @param files
     */
    public static void javaToCPP(ArrayList<File> files) {
        for (int i = 0; i < files.size(); i++) {
            javaToCPP(files.get(i));
        }
        if (!createImportFile()) {
            Log.error("Msg: create import file error.\n");
        }
    }

    /**
     * Start parse file
     * @param file
     */
    public static void javaToCPP(File file) {
        BufferedReader reader = null;
        Map<Integer, String> importConstruction = new HashMap<>();
        ArrayList<CppClass> classList = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            int line = 1;
            while((tempString = reader.readLine()) != null) {
                tempString = tempString.trim();
                if (tempString.startsWith("#include")) {
                    tempString = tempString.trim();
                    String[] construction = tempString.split("[\t|\\s+]");
                    if (construction.length != 2) {
                        Log.error("File: %s\tLine: %d\tMsg: %s.\n", file.getName(), line, "错误的导入指令");
                    } else {
                        String headerName = construction[1];
                        headerName = headerName.replace("\"", "");
                        headerName = headerName.replace("[<|>]", "");
                        if (headerName.startsWith(PREFIX)) {
                            headerName = headerName.substring(PREFIX.length(), headerName.length());
                            headerName = headerName.replace("/",".");
                            /**
                             * Filter the class of having parsed
                             */
                            if (classSet.contains(headerName)){
                                continue;
                            }
                            classSet.add(headerName);

                            /**
                             * Parse the class imported
                             */
                            Log.notice("Include class[%s]\n", headerName.replace(".h", ""));
                            importConstruction.put(line, headerName);
                            String fileNames[] = headerName.split("\\.");
                            CppClass cppClass = new CppClass(fileNames[fileNames.length - 2], line, headerName);
                            classList.add(cppClass);
                        }
                    }
                }
                line++;
            }
            for (int i = 0; i < classList.size(); i++) {
                DefineNewClass(classList.get(i));
            }

            for (int i = 0; i < predeclareList.size(); i++) {
                Log.notice("Predeclare class[%s]\n", predeclareList.get(i));
                String fileName = predeclareList.get(i);
                String fileNames[] = fileName.split("\\.");
                CppClass cppClass = new CppClass(fileNames[fileNames.length - 2], predeclareList.get(i));
                DefineNewClass(cppClass);
            }
        } catch (IOException e) {
            Log.error("Msg: %s\n", e.toString());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.error("Msg: %s\n", e.toString());
                }
            }
        }
    }

    /**
     * Create a header file and produce some code automaticlly.
     * @param cppClass
     */
    public static void DefineNewClass(CppClass cppClass) {
        File classFile = createDir(cppClass.getClassName());
        if (classFile != null) {
            try {
                classFile.createNewFile();
            } catch (IOException e) {
                Log.error("Msg: create file[%s] error. %s.\n", classFile.getName(), e.toString());
                return;
            }
        }
        if (!DefineHFile.defineHFile(cppClass, classFile)) {
            Log.error("Msg: parse class file[%s] error.\n", cppClass.getClassName());
        }
    }

    /**
     * This function is used to create directories and
     * header file which will be writed into.
     * @param path
     * @return
     */
    public static File createDir(String path) {
        /**
         * Create root directory
         */
        String dirPath = PREFIX.replace("/", "") + File.separator;
        File rDir = new File(dirPath);
        if (!rDir.isDirectory()) {
            rDir.deleteOnExit();
        }
        rDir.mkdir();
        if (!rDir.exists()) {
            if (!rDir.mkdir()) {
                Log.error("Msg: create directory[%s] failed.\n", dirPath);
            }
        }
        /**
         * Create path of class
         */
        String []dirs = path.split("\\.");
        for (int i = 0; i < dirs.length - 1; i++) {
            dirPath += dirs[i] + File.separator;
            File dDir = new File(dirPath);
            if (!dDir.isDirectory()) {
                dDir.deleteOnExit();
            }
            if (!dDir.exists()) {
                if(!dDir.mkdir()) {
                    Log.error("Error: create directory[%s] failed.\n", dirPath);
                }
            }
        }
        /**
         * Create class file
         */
        String filePath = dirPath + dirs[dirs.length - 1] + ".h";
        File classFile = new File(filePath);
        if (classFile.exists()) {
            classFile.delete();
        }
        return classFile;
    }

    /**
     * This function is used to search the class wheather is
     * imported or not.
     * @param name
     * @return
     */
    public static boolean findClass(String name) {
        if (classSet.contains(name + ".h")) {
            return true;
        }
        if (predeclareList.contains(name + ".h")) {
            return true;
        }
        return false;
    }

    /**
     * This function is used to add the class is not imported,
     * but used.
     * @param name
     */
    public static void addPredeclareClass(String name) {
        predeclareList.add(name + ".h");
    }
}
