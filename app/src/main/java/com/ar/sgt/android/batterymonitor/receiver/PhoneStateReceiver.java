package com.ar.sgt.android.batterymonitor.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ar.sgt.android.batterymonitor.service.BatteryMonitor;

/**
 * Created by gabriel on 30/08/2014.
 */
public class PhoneStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Intent.ACTION_SCREEN_ON
        //Intent.ACTION_SCREEN_OFF
        //Intent.ACTION_BOOT_COMPLETED
        //Intent.ACTION_USER_PRESENT
        Intent i = new Intent(context, BatteryMonitor.class);
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) ||
                intent.getAction().equals(Intent.ACTION_SCREEN_ON) ||
                intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            //i.setAction(BatteryMonitor.ACTION_UPDATE_START);
            context.startService(i);
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            i.setAction(BatteryMonitor.ACTION_UPDATE_STOP);
            context.startService(i);
        }
    }
}
