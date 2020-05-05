package com.sid.soundrecorderutils.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.sid.soundrecorderutils.R;
import com.sid.soundrecorderutils.record.AudioRecoderUtils;
import com.sid.soundrecorderutils.util.FileUtil;
import com.sid.soundrecorderutils.util.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SelectActivity extends BaseActivity implements View.OnClickListener {
    private Button mBtn1;
    private Button mBtn2;
    private Button mBtnUser;
    private TextView mTitle;
    private EditText mEdNumber;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);
        initView();
    }

    private void initView() {

        mBtn1 = (Button) findViewById(R.id.btn_1);
        mBtn2 = (Button) findViewById(R.id.btn_2);
        mBtnUser = (Button) findViewById(R.id.btn_user);
        mBtn1.setTextSize(16);
        mBtn2.setTextSize(16);
        mBtnUser.setTextSize(16);

        Drawable drawable_btnuser=getResources().getDrawable(R.drawable.me_tran);
        drawable_btnuser.setBounds(10,0,200,200);//第一0是距左边距离，第二0是距上边距离，30、35分别是长宽
        mBtnUser.setCompoundDrawables(drawable_btnuser,null,null,null);//只放左边
        Drawable drawable_btn1=getResources().getDrawable(R.drawable.visitor_tran);
        drawable_btn1.setBounds(10,0,200,200);//第一0是距左边距离，第二0是距上边距离，30、35分别是长宽
        mBtn1.setCompoundDrawables(drawable_btn1,null,null,null);//只放左边
        Drawable drawable_btn2=getResources().getDrawable(R.drawable.calendar_tran);
        drawable_btn2.setBounds(10,0,200,200);//第一0是距左边距离，第二0是距上边距离，30、35分别是长宽
        mBtn2.setCompoundDrawables(drawable_btn2,null,null,null);//只放左边



        mBtn1.setOnClickListener(this);
        mBtn2.setOnClickListener(this);
        mBtnUser.setOnClickListener(this);
        mTitle = (TextView) findViewById(R.id.title);
        mTitle.setText("真刻");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_1:
                startAct();
                break;
            case R.id.btn_2:
                help();
                break;
            case R.id.btn_user:
                showToast("登录/注册还未开启");
                break;
        }
    }

    private void startAct() {
        startActivity(MainActivity.newIntent(SelectActivity.this));
//        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void help() {
        new AlertDialog.Builder(SelectActivity.this)
                .setTitle("真刻 AuthenBase")
                .setMessage("Version 1.0.0\n\n记录一份证据，拯救一个生命\n\nRecord a piece of evidence and save a life")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {}
                }).show();

    }
}