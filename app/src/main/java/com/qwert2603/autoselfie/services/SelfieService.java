package com.qwert2603.autoselfie.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.qwert2603.autoselfie.R;
import com.qwert2603.autoselfie.helpers.CacheHelper;
import com.qwert2603.autoselfie.helpers.PhotoHelper;
import com.qwert2603.autoselfie.helpers.VkHelper;
import com.qwert2603.autoselfie.login.StartActivity;
import com.qwert2603.autoselfie.utils.LogUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class SelfieService extends IntentService {

    private static final String EXTRA_NOW_ONLY = "com.qwert2603.autoselfie.EXTRA_NOW_ONLY";

    private static final long MILLIS_PER_MINUTE = 60 * 1000;

    private VkHelper mVkHelper;
    private CacheHelper mCacheHelper;
    private PhotoHelper mPhotoHelper;

    public static void setNextIntentOrCancel(Context context) {
        Intent intent = new Intent(context, SelfieService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        LogUtils.d("alarmManager.cancel(pendingIntent);");
        alarmManager.cancel(pendingIntent);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        String enabledKey = context.getString(R.string.preference_autoselfie_enabled);
        boolean enabled = sharedPreferences.getBoolean(enabledKey, false);
        if (enabled) {
            String periodKey = context.getString(R.string.preference_autoselfie_period);
            String periodString = sharedPreferences.getString(periodKey, "");
            long periodInMillis = Long.valueOf(periodString) * MILLIS_PER_MINUTE;
            long triggerAtMillis = SystemClock.elapsedRealtime() + periodInMillis;
            LogUtils.d("triggerAtMillis == " + triggerAtMillis);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                LogUtils.d("alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pendingIntent);");
                alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pendingIntent);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                LogUtils.d("alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pendingIntent);");
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pendingIntent);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                LogUtils.d("alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pendingIntent);");
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pendingIntent);
            }
        }
    }

    public static void NOW(Context context) {
        Intent intent = new Intent(context, SelfieService.class);
        intent.putExtra(EXTRA_NOW_ONLY, true);
        context.startService(intent);
    }

    public SelfieService() {
        super(SelfieService.class.getName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mVkHelper = new VkHelper();
        mCacheHelper = new CacheHelper(SelfieService.this);
        mPhotoHelper = new PhotoHelper();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mNewPhotoTime = System.currentTimeMillis();
        boolean nowOnly = intent.getBooleanExtra(EXTRA_NOW_ONLY, false);
        LogUtils.d("onHandleIntent; mNewPhotoTime == " + new Date(mNewPhotoTime).toString() + "; nowOnly == " + nowOnly);
        mPhotoHelper.getPhoto(SelfieService.this, new PhotoHelper.Callback() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                LogUtils.d("onHandleIntent# getPhoto# onSuccess");
                mNewPhoto = bitmap;
                if (!isInternetConnected()) {
                    LogUtils.d("mCacheHelper.save(mNewPhoto, mNewPhotoTime);");
                    mCacheHelper.save(mNewPhoto, mNewPhotoTime);
                    mNewPhoto.recycle();
                    return;
                }
                mCurrentCachePhotoIndex = 0;
                if (!nowOnly) {
                    mCachesPhotosTimes = mCacheHelper.getSavedTimes();
                } else {
                    mCachesPhotosTimes = Collections.emptyList();
                }
                sendNextPhoto();
            }

            @Override
            public void onError(Throwable throwable) {
                LogUtils.e(throwable);
            }
        });
        if (!nowOnly) {
            setNextIntentOrCancel(SelfieService.this);
        }
    }

    private volatile long mNewPhotoTime;
    private volatile Bitmap mNewPhoto;
    private volatile List<Long> mCachesPhotosTimes;
    private volatile int mCurrentCachePhotoIndex;

    private void sendNextPhoto() {
        if (mCurrentCachePhotoIndex >= mCachesPhotosTimes.size()) {
            mVkHelper.sendPhoto(mNewPhoto, mNewPhotoTime, new VkHelper.Callback() {
                @Override
                public void onSuccess() {
                    mNewPhoto.recycle();
                    showNotification(mNewPhotoTime);
                }

                @Override
                public void onError(Throwable throwable) {
                    LogUtils.e(throwable);
                    mCacheHelper.save(mNewPhoto, mNewPhotoTime);
                    mNewPhoto.recycle();
                }
            });
        } else {
            Long photoTime = mCachesPhotosTimes.get(mCurrentCachePhotoIndex);
            Bitmap bitmap = mCacheHelper.load(photoTime);
            LogUtils.d("mCurrentCachePhotoIndex == " + mCurrentCachePhotoIndex + "; photoTime == " + photoTime);
            mVkHelper.sendPhoto(bitmap, photoTime, new VkHelper.Callback() {
                @Override
                public void onSuccess() {
                    mCacheHelper.remove(photoTime);
                    showNotification(photoTime);
                    ++mCurrentCachePhotoIndex;
                    sendNextPhoto();
                }

                @Override
                public void onError(Throwable throwable) {
                    LogUtils.e(throwable);
                }
            });
        }
    }

    private void showNotification(long photoTime) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(SelfieService.this);

        Intent intent = new Intent(SelfieService.this, StartActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(SelfieService.this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Notification notification = new NotificationCompat.Builder(SelfieService.this)
                .setTicker(getString(R.string.selfie_done))
                .setSmallIcon(Build.VERSION.SDK_INT >= 21 ? R.drawable._icon_white : R.drawable.icon_white)
                .setShowWhen(true)
                .setContentTitle(getString(R.string.selfie_done))
                .setContentIntent(pendingIntent)
                .setContentText(getString(R.string.selfie_was_sent_to_vk))
                .setAutoCancel(true)
                .setGroupSummary(true)
                .setGroup(getString(R.string.app_name))
                .build();

        notificationManager.notify((int) photoTime, notification);
    }

    private boolean isInternetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}
