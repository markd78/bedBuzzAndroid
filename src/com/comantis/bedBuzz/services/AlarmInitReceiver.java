package com.comantis.bedBuzz.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.comantis.bedBuzz.models.AlarmsModel;

public class AlarmInitReceiver extends BroadcastReceiver {

    /**
     * Sets alarm on ACTION_BOOT_COMPLETED.  Resets alarm on
     * TIME_SET, TIMEZONE_CHANGED
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.w(getClass().getSimpleName(), "***** AlarmInitReceiver - Received "+action);

        // set the alarms
        AlarmsModel.getAlarmsModel().getAlarms(context);
        AlarmsModel.getAlarmsModel().saveAlarms(context);
    }
}
