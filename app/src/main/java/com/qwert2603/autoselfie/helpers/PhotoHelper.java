package com.qwert2603.autoselfie.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PhotoHelper {

    private static Map<Integer, Callback> sCallbacks = new HashMap<>();

    public static Callback getCallback(int id) {
        Callback callback = sCallbacks.get(id);
        sCallbacks.remove(id);
        return callback;
    }

    private static int addCallback(Callback callback) {
        int id;
        do {
            id = new Random().nextInt();
        } while (sCallbacks.containsKey(id));
        sCallbacks.put(id, callback);
        return id;
    }

    public interface Callback {
        void onSuccess(Bitmap bitmap);

        void onError(Throwable throwable);
    }


    public void getPhoto(Context context, final Callback callback) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                callback.onError(new IllegalAccessException("Denied access to camera!!!"));
                return;
            }
        }

        Intent intent = new Intent(context, SurfaceViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        int id = addCallback(callback);
        intent.putExtra(SurfaceViewActivity.EXTRA_CALLBACK_ID, id);
        context.startActivity(intent);
    }

}
