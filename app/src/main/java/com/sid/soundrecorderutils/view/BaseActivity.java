package com.sid.soundrecorderutils.view;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sid.soundrecorderutils.R;
import com.sid.soundrecorderutils.service.UploadService;

public class BaseActivity extends Activity {
    //通过在BaseActivity中注册一个广播，当退出时发送一个广播，finish退出
    private static final String EXITACTION = "action.exit";
    private ExitReceiver exitReceiver = new ExitReceiver();

    private String[] STRINGS = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.LOCATION_HARDWARE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WAKE_LOCK};





    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * 动态申请读写权限
         * */
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(getBaseContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        STRINGS, 1);
            }
        }

        //通过在BaseActivity中注册一个广播，当退出时发送一个广播，finish退出
        IntentFilter filter = new IntentFilter();
        filter.addAction(EXITACTION);
        registerReceiver(exitReceiver, filter);



        //****************注册广播监听网络变化************
        // 创建 IntentFilter 实例
        intentFilter = new IntentFilter();
        // 添加广播值
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        // 创建 NetworkChangeReceiver 实例
        networkChangeReceiver = new NetworkChangeReceiver();
        // 注册广播
        registerReceiver(networkChangeReceiver,intentFilter);
        //****************注册广播监听网络变化************



    }

    public void showToast(String value) {
        Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(exitReceiver);
        unregisterReceiver(networkChangeReceiver);
    }

    class ExitReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            BaseActivity.this.finish();
        }

    }

    //****************注册广播监听网络变化************
    private IntentFilter intentFilter;
    private NetworkChangeReceiver networkChangeReceiver;
    //****************注册广播监听网络变化************
    class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 获取管理网络连接的系统服务类的实例
            ConnectivityManager connectivityManager = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            // 判断网络是否可用
            if (networkInfo != null && networkInfo.isAvailable()){
                //网络可以用
                Intent intentService = new Intent(context, UploadService.class);
                startService(intentService);

            }else {
                //网络不可用
                Intent intentService = new Intent(context,UploadService.class);
                stopService(intentService);
            }

        }
    }




}
