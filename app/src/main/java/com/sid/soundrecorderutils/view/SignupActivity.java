package com.sid.soundrecorderutils.view;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.sid.soundrecorderutils.R;
import com.sid.soundrecorderutils.api.API;
import com.sid.soundrecorderutils.util.EditTextUtil;

public class SignupActivity extends BaseActivity {
    EditTextUtil mEtUsername, mEtPassword1, mEtPassword2;
    ImageView mIvSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mEtUsername = findViewById(R.id.et_username);
        mEtPassword1 = findViewById(R.id.et_password);
        mEtPassword2 = findViewById(R.id.et_password2);
        mIvSignup = findViewById(R.id.iv_zhiwen);
        mIvSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserRegister();
            }
        });

    }

    /**
     * 用户注册
     */
    protected void UserRegister() {
        String username = mEtUsername.getText().toString();
        String password1 = mEtPassword1.getText().toString();
        String password2 = mEtPassword2.getText().toString();
        if (username.equals("") || !checkUsernameAvailable(username)) {
            Toast.makeText(this, "请您输入合法的昵称", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password1.equals("") || !checkPasswordAvailable(password1)) {
            Toast.makeText(this, "请您输入合法的密码", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password2.equals("") || !checkPasswordAvailable(password2)) {
            Toast.makeText(this, "请您输入合法的密码", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password1.equals(password2)) {
            Toast.makeText(this, "输入的两次密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }
        register(username, password1);
    }

    /**
     * 检查username是否合法
     *
     * @param input_username
     */
    private boolean checkUsernameAvailable(String input_username) {
        return true;
    }

    /**
     * 检查password是否合法
     *
     * @param input_password
     */
    private boolean checkPasswordAvailable(String input_password) {
        return true;
    }

    //    private void register(final String username, final String password) {
//
//        final API api = new API(getApplicationContext());
//
//        final String[][] res = new String[1][];
//
//        final boolean[] notContinue = {true};
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                res[0] = api.register(username, password);
//
//
//                notContinue[0] = false;
//
//            }
//        }).start();
//
//        while (notContinue[0]) {
//            System.out.println("ovo");
//        }
//
//        if (res != null && res[0] != null && res[0][0].equals("0")) {
//            Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
//            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
//            startActivity(intent);
//        } else {
//            Toast.makeText(this, "注册失败，请重新注册", Toast.LENGTH_SHORT).show();
//        }
//
//    }
    private void register(final String username, final String password) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                API api = new API(getApplicationContext());
                String[] res = api.register(username, password);
                if (res != null && res[0].equals("0")) {
                    Message message = new Message();
                    message.what = 0;
                    registerHandler.sendMessage(message);
                } else {
                    Message message = new Message();
                    message.what = -1;
                    registerHandler.sendMessage(message);
                }
            }
        }).start();
    }

    Handler registerHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Toast.makeText(getApplicationContext(), "注册成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                    startActivity(intent);
                    break;
                case -1:
                    Toast.makeText(getApplicationContext(), "注册失败，请重新注册", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

    };


}
