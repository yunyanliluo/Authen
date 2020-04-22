package com.sid.soundrecorderutils.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sid.soundrecorderutils.R;
import com.sid.soundrecorderutils.util.EditTextUtil;

public class LoginActivity extends Activity {
    TextView mTvWelcome1, mTvWelcome2, mTvUsername, mTvPassword;
    EditTextUtil mEtUsername, mEtPassword;
    ImageView mIvLogin;

    Handler pause = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    SharedPreferences pref = LoginActivity.this.getSharedPreferences("user", 0);
                    String username = pref.getString("username", "");
                    String password = pref.getString("password", "");
                    Intent intent;
                    if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
                        intent = new Intent();
                        intent.setClass(LoginActivity.this, MainActivity.class);
                        LoginActivity.this.startActivity(intent);
                    } else {
//                        int[] location = new int[2];
//                        mTvWelcome1.getLocationOnScreen(location);//获取视图位置
//                        int x = location[0];
//                        int y = location[1];
//                        TranslateAnimation tAnim = new TranslateAnimation(x, x, y, y-50);//设置视图上下移动的位置
//                        tAnim .setDuration(1000);
//                        tAnim .setRepeatCount(Animation.INFINITE);
//                        tAnim .setRepeatMode(Animation.REVERSE);
//                        mTvWelcome1.startAnimation(tAnim);
                        mTvWelcome1.setVisibility(View.INVISIBLE);
                        mTvWelcome2.setVisibility(View.INVISIBLE);
                        mTvUsername.setVisibility(View.VISIBLE);
                        mTvPassword.setVisibility(View.VISIBLE);
                        mEtUsername.setVisibility(View.VISIBLE);
                        mEtPassword.setVisibility(View.VISIBLE);

                        mIvLogin.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(final View v) {
                                userLogin();
                            }
                        });
                    }

//                    LoginActivity.this.finish();
                default:
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getWindow().addFlags(1024);
        this.requestWindowFeature(1);
        this.setContentView(R.layout.activity_login);

        mTvWelcome1 = (TextView) findViewById(R.id.tv_welcome1);
        mTvWelcome2 = (TextView) findViewById(R.id.tv_welcome2);
        mTvUsername = (TextView) findViewById(R.id.tv_username);
        mTvPassword = (TextView) findViewById(R.id.tv_password);
        mEtUsername = (EditTextUtil) findViewById(R.id.et_username);
        mEtPassword = (EditTextUtil) findViewById(R.id.et_password);
        mIvLogin = (ImageView) findViewById(R.id.iv_zhiwen);

        mTvUsername.setVisibility(View.INVISIBLE);
        mTvPassword.setVisibility(View.INVISIBLE);
        mEtUsername.setVisibility(View.INVISIBLE);
        mEtPassword.setVisibility(View.INVISIBLE);

        this.pause.sendEmptyMessageDelayed(1, 2000);
    }

    /**
     * 用户登录
     */
    protected void userLogin() {
        String input_username = mEtUsername.getText().toString();
        String input_password = mEtPassword.getText().toString();
        if(input_username == "" || !checkUsernameAvailable(input_username)) {
            Toast.makeText(this, "请您输入合法的昵称", Toast.LENGTH_SHORT).show();
            return;
        }
        if(input_password == "" || !checkPasswordAvailable(input_password)) {
            Toast.makeText(this, "请您输入合法的密码", Toast.LENGTH_SHORT).show();
            return;
        }
        login(input_username, input_password);
    }

    /**
     * 检查username是否合法
     * @param input_username
     */
    private boolean checkUsernameAvailable(String input_username) {
        return true;
    }

    /**
     * 检查password是否合法
     * @param input_password
     */
    private boolean checkPasswordAvailable(String input_password) {
        return true;
    }

    /**
     * 登录/注册
     * @param input_username
     * @param input_password
     */
    private void login(String input_username, String input_password) {
        startActivity(MainActivity.newIntent(LoginActivity.this));
    }
}
