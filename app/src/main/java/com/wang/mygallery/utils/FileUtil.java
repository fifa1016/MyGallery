package com.wang.mygallery.utils;

import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by wang on 16-8-30.
 */

public class FileUtil {
    /**
     * save bitmap to sd card
     *
     * @param bitmap
     * @param fileName xxx.jpg
     */
    public static String savePic(Bitmap bitmap, String fileName) {
        String path = Environment.getExternalStorageDirectory() + "/Pictures";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(path, fileName);
        int i = 0;
        String newFileName = fileName;
        while (file.exists()) {
            i += 1;
            newFileName = fileName.substring(0, fileName.lastIndexOf("."))
                    + "(" + i + ")" + fileName.substring(fileName.lastIndexOf("."));
            file = new File(path, newFileName);
        }
        try {
            OutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
            outputStream.flush();
            outputStream.close();

            return path + "/" + newFileName;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }
}
