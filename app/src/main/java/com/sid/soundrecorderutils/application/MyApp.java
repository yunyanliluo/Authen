package com.sid.soundrecorderutils.application;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.sid.soundrecorderutils.receiver.BatteryReceiver;
import com.sid.soundrecorderutils.util.LogUtils;
import com.sid.soundrecorderutils.util.SharedPreferencesUtil;

public class MyApp extends Application {
    private BatteryReceiver receiver;

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferencesUtil.getInstance(getContext());
        LogUtils.init();
        receiver();
    }

    private Context getContext() {
        return getApplicationContext();
    }

    /**
     * 注册电量广播
     */
    private void receiver() {
        receiver = new BatteryReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(receiver, filter);
    }
}