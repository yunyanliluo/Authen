package com.sid.soundrecorderutils.view;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.ImageView;

import com.sid.soundrecorderutils.R;

/**
 * Created by theon on 2018/1/26.
 */






public class SplashActivity extends Activity {
    Handler pause = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case 1:
                    SharedPreferences pref = SplashActivity.this.getSharedPreferences("user", 0);
                    String username = pref.getString("username", "");
                    String password = pref.getString("password", "");
                    Intent intent;
                    if(!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
                        intent = new Intent();
                        intent.setClass(SplashActivity.this, SelectActivity.class);
                        SplashActivity.this.startActivity(intent);
                    } else {
                        intent = new Intent();
                        intent.setClass(SplashActivity.this, SelectActivity.class);
                        SplashActivity.this.startActivity(intent);
                    }

                    SplashActivity.this.finish();
                default:
            }
        }
    };


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().addFlags(1024);
        this.requestWindowFeature(1);
        this.setContentView(R.layout.activity_splash);
        this.pause.sendEmptyMessageDelayed(1, 2000);
    }
}
