package com.ar.sgt.android.batterymonitor.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ar.sgt.android.batterymonitor.service.BatteryMonitor;

/**
 * Created by gabriel on 30/08/2014.
 */
public class BatteryStateReceiver extends BroadcastReceiver {

    private final static String TAG = "BatteryStateReceiver";

    public static final String BATTERY_CHANGED = "com.ar.sgt.android.batterymonitor.BATTERY_CHANGED";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, intent.getAction());
        Intent i = new Intent(context, BatteryMonitor.class);
        if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
            intent.setAction(BATTERY_CHANGED);
            intent.putExtras(intent);
        }
        context.startService(i);
    }

}
