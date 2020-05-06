package com.sid.soundrecorderutils.view;

import android.content.Intent;
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

    private void register(final String username, final String password) {
        System.out.println("0000000000000000000000000000");
        final API api = new API(getApplicationContext());
        System.out.println("0000000000000000000000000001");
        final String[][] res = new String[1][];
        System.out.println("0000000000000000000000000002");
        final boolean[] notContinue = {true};
        System.out.println("0000000000000000000000000003");
        new Thread(new Runnable() {
            @Override
            public void run() {
                res[0] = api.register(username, password);
                System.out.println("res[0]");
                System.out.println(notContinue[0]);
                notContinue[0] = false;
                System.out.println( notContinue[0]);
            }
        }).start();
        System.out.println("0000000000000000000000000004");
        while (notContinue[0]) {
            System.out.println("ovo");
        }
        System.out.println("0000000000000000000000000005");
        if (res != null && res[0] != null && res[0][0].equals("0")) {
            Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "注册失败，请重新注册", Toast.LENGTH_SHORT).show();
        }

    }
}
