package com.valor.mercury.script.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class IncrementValueWriter {

    public static boolean saveInc(String incValue, String filePath) {
        File file = new File(filePath);
        if(!file.exists())
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        try {
            FileOutputStream fos = new FileOutputStream(file,true);
            fos.write("\n".getBytes());
            fos.write(incValue.getBytes());
            fos.close();
        }catch(IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
