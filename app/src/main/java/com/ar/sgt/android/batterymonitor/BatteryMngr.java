package com.ar.sgt.android.batterymonitor;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.ar.sgt.android.batterymonitor.service.BatteryMonitor;

/**
 * Created by gabriel on 30/08/2014.
 */
public class BatteryMngr {

    public static class BatteryDetails {
        public float temperature;
        public int level;
        public int scale;

        public float getTemperature() {
            return temperature;
        }

        public void setTemperature(float temperature) {
            this.temperature = temperature;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public int getScale() {
            return scale;
        }

        public void setScale(int scale) {
            this.scale = scale;
        }
    }

    public static BatteryDetails getBatteryDetails(Context context) {
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);
        return extractBatteryStatus(batteryStatus);
    }

    public static BatteryDetails extractBatteryStatus(Intent batteryStatus) {
        BatteryDetails b = new BatteryDetails();
        b.setLevel(batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, 0));
        b.setScale(batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, 100));

        float temp = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10;
        b.setTemperature(temp);
        //float batteryPct = (level / (float) scale) * 100;

        return b;
    }
}
