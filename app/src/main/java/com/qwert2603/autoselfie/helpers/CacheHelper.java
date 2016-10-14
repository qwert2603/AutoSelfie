package com.qwert2603.autoselfie.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.qwert2603.autoselfie.utils.LogUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CacheHelper {

    private static final String DIR_NAME = "photos";

    private final File mDir;

    public CacheHelper(Context context) {
        mDir = new File(context.getFilesDir(), DIR_NAME);
        boolean mkdirs = mDir.mkdirs();
        LogUtils.d("CacheHelper()#mkdirs == " + mkdirs);
    }

    public void save(Bitmap bitmap, long photoTime) {
        LogUtils.d("CacheHelper#save " + photoTime);
        List<Long> savedTimes = getSavedTimes();
        if (savedTimes.size() >= 14) {
            remove(savedTimes.get(0));
        }
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(new File(mDir, String.valueOf(photoTime)));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        } catch (IOException e) {
            LogUtils.e(e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public void remove(long photoTime) {
        boolean delete = new File(mDir, String.valueOf(photoTime)).delete();
        LogUtils.d("CacheHelper#remove " + photoTime + "; delete == " + delete);
    }

    public List<Long> getSavedTimes() {
        File[] list = mDir.listFiles();
        List<Long> saved = new ArrayList<>();
        for (File s : list) {
            saved.add(Long.parseLong(s.getName()));
        }
        Collections.sort(saved);
        LogUtils.d("CacheHelper#getSavedTimes " + saved);
        return saved;
    }

    public Bitmap load(long photoTime) {
        LogUtils.d("CacheHelper#load " + photoTime);
        return BitmapFactory.decodeFile(new File(mDir, String.valueOf(photoTime)).getPath());
    }

}
