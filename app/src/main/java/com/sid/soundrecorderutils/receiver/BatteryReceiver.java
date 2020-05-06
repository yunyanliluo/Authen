package com.sid.soundrecorderutils.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Administrator on 2018\8\8.
 * 获取当前电量
 */

public class BatteryReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        int current = intent.getExtras().getInt("level");//获得当前电量
        int total = intent.getExtras().getInt("scale");//获得总电量
        int percent = current * 100 / total;
        Log.e("TAG", "当前电量:" + percent + "/" + total);
    }
}