package com.sid.soundrecorderutils.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.sid.soundrecorderutils.R;

import static android.content.ContentValues.TAG;

public class CallActivity extends BaseActivity implements View.OnClickListener {
    private Button mBtn110, mBtn120, mBtn119, mBtnCont1, mBtnCont2;
    private ImageView mBtnAdd, mImgBtnCallBack;
    Drawable drawable_btn_110, drawable_btn_119, drawable_btn_120, drawable_btn_cont1;
    private String phone1, phone2, phone3, phone4;
    private static String name_cont1;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        context = this;
        if(name_cont1 == null) name_cont1 = "常用联系人";
        initView();
    }

    /**
     * 初始化UI
     */
    private void initView() {
        mBtn110 = (Button) findViewById(R.id.btn_110);
        mBtn110.setOnClickListener(this);

        mBtn120 = (Button) findViewById(R.id.btn_120);
        mBtn120.setOnClickListener(this);

        mBtn119 = (Button) findViewById(R.id.btn_119);
        mBtn119.setOnClickListener(this);

        //常用联系人
        mBtnCont1 = (Button) findViewById(R.id.btn_cont1);
        mBtnCont1.setText(name_cont1);
        mBtnCont1.setOnClickListener(this);
        //长按更新常用联系人电话号码及名称
        mBtnCont1.setOnLongClickListener(new View.OnLongClickListener() {
        @Override
            public boolean onLongClick(View v) {
                final EditText ETView = new EditText(context);
                ETView.setInputType(InputType.TYPE_CLASS_PHONE);
                ETView.setHint("暂未设置求助号码");
                if(phone4 != null)
                    ETView.setText(phone4);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setView(ETView);
                alertDialogBuilder.setTitle("更新求助号码");
                alertDialogBuilder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String phone_update = ETView.getText().toString();
                        phone4 = phone_update;

                        final EditText ETView = new EditText(context);
                        ETView.setHint("常用联系人");
                        Log.d(TAG, "onClick: to show" + mBtnCont1.getText().toString() + ".");
                        ETView.setText(mBtnCont1.getText());
                        if(ETView.getText().toString().compareTo("常用联系人") == 0) ETView.setText("");
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                        alertDialogBuilder.setView(ETView);
                        alertDialogBuilder.setTitle("更新名称");
                        alertDialogBuilder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String name_update = ETView.getText().toString();
                                Log.d(TAG, "onClick: to update"+ name_update + ".");
                                mBtnCont1.setText(name_update);
                                if(mBtnCont1.getText().toString().compareTo("") == 0) {
                                    mBtnCont1.setText("常用联系人");
                                }

                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
                return false;
            }
         });


        mImgBtnCallBack = (ImageView) findViewById(R.id.iv_callback);
        mImgBtnCallBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        drawable_btn_110=getResources().getDrawable(R.drawable.paichusuo);
        drawable_btn_110.setBounds(0,0,200,200);//第一0是距左边距离，第二0是距上边距离，第三、第四分别是长宽
        mBtn110.setCompoundDrawables(drawable_btn_110,null,null,null);//只放上边
        drawable_btn_120=getResources().getDrawable(R.drawable.yiliao);
        drawable_btn_120.setBounds(0,0,200,200);//第一0是距左边距离，第二0是距上边距离，30、35分别是长宽
        mBtn120.setCompoundDrawables(drawable_btn_120,null,null,null);//只放左边
        drawable_btn_119=getResources().getDrawable(R.drawable.xiaofang);
        drawable_btn_119.setBounds(0,0,200,200);//第一0是距左边距离，第二0是距上边距离，30、35分别是长宽
        mBtn119.setCompoundDrawables(drawable_btn_119,null,null,null);//只放左边
        drawable_btn_cont1=getResources().getDrawable(R.drawable.lianxiren);
        drawable_btn_cont1.setBounds(0,0,200,200);//第一0是距左边距离，第二0是距上边距离，30、35分别是长宽
        mBtnCont1.setCompoundDrawables(drawable_btn_cont1,null,null,null);//只放左边
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_110:
                AlertDialog.Builder alertDialogBuilder110 = new AlertDialog.Builder(context);
                alertDialogBuilder110.setTitle("请注意");
                alertDialogBuilder110.setMessage("恶意拨打110报警电话可追究刑事责任，继续拨打请按确认").setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: 110");
                        callPhone("110");
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
                break;

            case R.id.btn_120:
                AlertDialog.Builder alertDialogBuilder120 = new AlertDialog.Builder(context);
                alertDialogBuilder120.setTitle("请注意");
                alertDialogBuilder120.setMessage("恶意拨打120求助电话可追究刑事责任，继续拨打请按确认").setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: 120");
                        callPhone("120");
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
                break;

            case R.id.btn_119:
                AlertDialog.Builder alertDialogBuilder119 = new AlertDialog.Builder(context);
                alertDialogBuilder119.setTitle("请注意");
                alertDialogBuilder119.setMessage("恶意拨打119火警电话可追究刑事责任，继续拨打请按确认").setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: 119");
                        callPhone("119");
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
                break;

            case R.id.btn_cont1:
                if(phone4 == null) {
                    callPhone("");
                }
                else {
                    callPhone(phone4);
                }
                break;
        }
    }

    /**
     * 拨打电话（跳转到拨号界面，用户手动点击拨打）
     *
     * @param phoneNum 电话号码
     */
    public void callPhone(String phoneNum) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        Uri data = Uri.parse("tel:" + phoneNum);
        intent.setData(data);
        startActivity(intent);
    }
}
