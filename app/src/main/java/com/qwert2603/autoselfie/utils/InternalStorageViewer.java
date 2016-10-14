package com.qwert2603.autoselfie.utils;

import android.content.Context;

import java.io.File;

/**
 * Класс для вывода в logcat содержимое внутреннего хранилища приложения.
 */
public class InternalStorageViewer {

    /**
     * Вывести в logcat содержимое внутреннего хранилища приложения.
     */
    public static void print(Context context) {
        File internalStorage = context.getApplicationContext().getFilesDir();
        LogUtils.d("## INTERNAL STORAGE START ##");
        int totalLength = printDir(internalStorage);
        LogUtils.d("## INTERNAL STORAGE END ## total length == " + totalLength);
    }

    /**
     * Вывести в logcat содержмое папки. Рекурсивно.
     * Вернет общий размер папка с учетом размеров вложенных папок.
     */
    private static int printDir(File dir) {
        int length = 0;
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                length += printDir(file);
            } else {
                LogUtils.d(file.toString() + " { length = " + file.length() + " }");
                length += file.length();
            }
        }
        return length;
    }
}
