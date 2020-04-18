package com.sid.soundrecorderutils.view;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
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

public class BaseActivity extends Activity {
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
    }

    public void showToast(String value) {
        Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
//        View toastView = getLayoutInflater().inflate(R.layout.layout_toast, null);
//        Toast toast=new Toast(getApplicationContext());
//        toast.setView(toastView);
////        TextView tv=(TextView)toastView.findViewById(R.id.message);
//        tv.setText(value);
//        toast.show();


    }

//    //记录用户首次点击返回键的时间
//    private long firstTime = 0;
//
//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
//            long secondTime = System.currentTimeMillis();
//            if (secondTime - firstTime > 2000) {
//                Toast.makeText(this, "再按一次退出应用", Toast.LENGTH_SHORT).show();
//                firstTime = secondTime;
//                return true;
//            } else {
//                finish();
//            }
//        }
//        return super.onKeyUp(keyCode, event);
//    }

}
