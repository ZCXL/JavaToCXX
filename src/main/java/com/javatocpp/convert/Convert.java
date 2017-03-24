package com.javatocpp.convert;

import com.javatocpp.log.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ZhuChao on 2017/3/14.
 */
public class Convert {
    public final static String PREFIX = "jni_bridge/";
    public static void javaToCPP(ArrayList<File> files) {
        for (int i = 0; i < files.size(); i++) {
            javaToCPP(files.get(i));
        }
    }
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
                        Log.error("Line: %d\tError: %s.\n", line, "错误的导入指令");
                    } else {
                        String headerName = construction[1];
                        headerName = headerName.replace("\"", "");
                        if (headerName.startsWith(PREFIX)) {
                            headerName = headerName.substring(PREFIX.length(), headerName.length());
                            headerName = headerName.replace("/",".");
                            importConstruction.put(line, headerName);
                            classList.add(new CppClass(file.getName(), line, headerName));
                        }
                    }
                }
                line++;
            }
            for (int i = 0; i < classList.size(); i++) {
                DefineNewClass(classList.get(i));
            }
        } catch (IOException e) {
            Log.error("Error: %s", e.toString());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.error("Error: %s", e.toString());
                }
            }
        }
    }
    public static void DefineNewClass(CppClass cppClass) {
        File classFile = createDir(cppClass.getClassName());
        if (classFile != null) {
            try {
                classFile.createNewFile();
            } catch (IOException e) {
                Log.error("Error: create file[%s] error. \n\tInfo: %s.\n", classFile.getName(), e.toString());
                return;
            }
        }
        if (!DefineHFile.defineHFile(cppClass, classFile)) {
            Log.error("");
        }
    }
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
                Log.error("Error: create directory[%s] failed.\n", dirPath);
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
}
