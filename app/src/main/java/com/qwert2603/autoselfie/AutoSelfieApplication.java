package com.qwert2603.autoselfie;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

import com.qwert2603.autoselfie.utils.InternalStorageViewer;
import com.vk.sdk.VKSdk;

public class AutoSelfieApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        VKSdk.initialize(AutoSelfieApplication.this);

        InternalStorageViewer.print(AutoSelfieApplication.this);

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

//        String[] certificateFingerprint = VKUtil.getCertificateFingerprint(AutoSelfieApplication.this, getPackageName());
//        if (certificateFingerprint != null) {
//            for (String s : certificateFingerprint) {
//                LogUtils.d(s);
//            }
//        }
    }
}
