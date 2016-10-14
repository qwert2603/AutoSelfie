package com.qwert2603.autoselfie.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.qwert2603.autoselfie.services.SelfieService;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SelfieService.setNextIntentOrCancel(context.getApplicationContext());
    }
}
