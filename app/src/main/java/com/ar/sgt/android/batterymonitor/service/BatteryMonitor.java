package com.ar.sgt.android.batterymonitor.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.*;
import android.os.Process;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.ar.sgt.android.batterymonitor.BatteryMngr;
import com.ar.sgt.android.batterymonitor.MainApp;
import com.ar.sgt.android.batterymonitor.R;
import com.ar.sgt.android.batterymonitor.receiver.BatteryStateReceiver;
import com.ar.sgt.android.batterymonitor.receiver.PhoneStateReceiver;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by gabriel on 30/08/2014.
 */
public class BatteryMonitor extends Service {

    //public static final String ACTION_UPDATE_START = "com.ar.sgt.android.batterymonitor.MONITOR_START";
    public static final String ACTION_UPDATE_STOP = "com.ar.sgt.android.batterymonitor.MONITOR_STOP";

    private static final String NAME = BatteryMonitor.class.getName();

    private static final String TAG = "BatteryMonitor";

    private static final String BATTERY_RESOURCE_NAME = "battery_digit_";

    private static final int NOTIFICATION_ID = 1;

    private static final long DELAY = 5 * 60 * 1000;

    private Looper mServiceLooper;

    private MonitorHandler mMonitorHandler;

    private AtomicBoolean mMonitorRunning = new AtomicBoolean(false);

    //private BatteryStateReceiver mBatteryStateReceiver = null;

    private PhoneStateReceiver mPhoneStateReceiver = null;

    private NotificationManager mNotificationManager;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread(NAME, Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        this.mServiceLooper = thread.getLooper();
        this.mMonitorHandler = new MonitorHandler(mServiceLooper, this);

        this.mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        this.mPhoneStateReceiver = new PhoneStateReceiver();

        //this.mBatteryStateReceiver = new BatteryStateReceiver();

        registerListeners();

        startForeground(NOTIFICATION_ID, getServiceNotification(BatteryMngr.getBatteryDetails(getApplicationContext())));
    }

    private void registerListeners() {
        Log.d(TAG, "registerListeners");
        //battery changed fires too often, it's a waste of resources
        //getApplicationContext().registerReceiver(this.mBatteryStateReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        getApplicationContext().registerReceiver(this.mPhoneStateReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
        getApplicationContext().registerReceiver(this.mPhoneStateReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        getApplicationContext().registerReceiver(this.mPhoneStateReceiver, new IntentFilter(Intent.ACTION_USER_PRESENT));
    }

    private void unregisterListeners() {
        Log.d(TAG, "unregisterListeners");
        //getApplicationContext().unregisterReceiver(this.mBatteryStateReceiver);
        getApplicationContext().unregisterReceiver(this.mPhoneStateReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        updateBatteryStatus(intent);

        if (intent != null && ACTION_UPDATE_STOP.equals(intent.getAction())) {
            stopMonitor();
        } else if (!mMonitorRunning.get() && isScreenOn()) {
            startMonitor();
        }

        return START_STICKY;
    }

    private boolean isScreenOn() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        return powerManager.isScreenOn();
    }

    private void startMonitor() {
        synchronized (mMonitorRunning) {
            if (!mMonitorRunning.getAndSet(true)) {
                Log.d(TAG, "Monitor running");
                this.mMonitorHandler.sendEmptyMessageDelayed(0, DELAY);
            }
        }
    }

    private void stopMonitor() {
        synchronized (mMonitorRunning) {
            if (mMonitorRunning.getAndSet(false)) {
                Log.d(TAG, "Monitor stopped");
                this.mMonitorHandler.removeMessages(0);
            }
        }
    }

    private void updateBatteryStatus(Intent intent) {
        Log.d(TAG, "Update battery status");
        BatteryMngr.BatteryDetails battery;
        if (intent != null && BatteryStateReceiver.BATTERY_CHANGED.equals(intent.getAction())) {
            battery = BatteryMngr.extractBatteryStatus(intent);
        } else {
            battery = BatteryMngr.getBatteryDetails(getApplicationContext());
        }
        mNotificationManager.notify(NOTIFICATION_ID, getServiceNotification(battery));
    }

    private Notification getServiceNotification(BatteryMngr.BatteryDetails battery) {
        int resourceId = this.getResources().getIdentifier(BATTERY_RESOURCE_NAME + Integer.toString(battery.getLevel()), "drawable", this.getPackageName());

        Intent iApp = new Intent(getApplicationContext(), MainApp.class);
        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, iApp, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setOngoing(true)
                .setContentTitle(getResources().getString(R.string.battery_status, battery.getLevel()))
                .setContentText(getResources().getString(R.string.battery_temperature, battery.getTemperature()))
                .setContentIntent(pIntent)
                .setWhen(0)
                .setSmallIcon(resourceId);

        return builder.build();
    }

    @Override
    public void onDestroy() {
        stopMonitor();
        mServiceLooper.quit();
        unregisterListeners();
    }

    private final class MonitorHandler extends Handler {

        private final WeakReference<BatteryMonitor> mService;

        public MonitorHandler(Looper looper, BatteryMonitor receiver) {
            super(looper);
            mService = new WeakReference<BatteryMonitor>(receiver);
        }

        @Override
        public void handleMessage(final Message msg) {
            BatteryMonitor service = mService.get();
            service.updateBatteryStatus(null);
            synchronized (service.mMonitorRunning) {
                if (service.mMonitorRunning.get()) service.mMonitorHandler.sendEmptyMessageDelayed(0, service.DELAY);
            }
        }
    }

}
