package com.sid.soundrecorderutils.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.sid.soundrecorderutils.tcp.SocketManager;
import com.sid.soundrecorderutils.tcp.SocketMsg;
import com.sid.soundrecorderutils.util.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SoundRecordService extends Service {
    private String TAG = "SoundRecordService";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSocketMsg(SocketMsg socketMsg) {
        LogUtils.e(TAG, "ServerSocket:" + socketMsg.strData);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
