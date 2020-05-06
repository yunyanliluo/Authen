package com.sid.soundrecorderutils.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sid.soundrecorderutils.record.AudioRecoderUtils;
import com.sid.soundrecorderutils.R;
import com.sid.soundrecorderutils.ftp.FtpClient;
import com.sid.soundrecorderutils.service.UploadService;
import com.sid.soundrecorderutils.util.FileUtil;
import com.sid.soundrecorderutils.util.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private String TAG = "MainActivity";
    private Button mBtnRecord, mBtnTakePhoto, mBtnTakeVideo, mBtnUpload;
    private ImageView mBtnSetting, mBtnLock, mBtnPosition, mBtnCall;
    private String imgPath = "";            //图片文件路径
    private String imgName = "";            //图片文件名
    private String mp3Path = "";            //录音文件路径
    private String mp3Name = "";            //录音文件名
    private TextView mTitle;

    public static Intent newIntent(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initView();

        //****************注册广播监听网络变化************
        // 创建 IntentFilter 实例
        intentFilter = new IntentFilter();
        // 添加广播值
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        // 创建 NetworkChangeReceiver 实例
        networkChangeReceiver = new BaseActivity.NetworkChangeReceiver();
        // 注册广播
        registerReceiver(networkChangeReceiver,intentFilter);
        //****************注册广播监听网络变化************
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(networkChangeReceiver);
    }


    /**
     * 初始化UI
     */
    private void initView() {
        mBtnRecord = (Button) findViewById(R.id.btn_record);
        mBtnRecord.setOnClickListener(this);

        mBtnTakePhoto = (Button) findViewById(R.id.btn_take_photo);
        mBtnTakePhoto.setOnClickListener(this);

        mBtnTakeVideo = (Button) findViewById(R.id.btn_take_video);
        mBtnTakeVideo.setOnClickListener(this);

        mBtnUpload = (Button) findViewById(R.id.btn_upload);
        mBtnUpload.setOnClickListener(this);

        mBtnSetting = (ImageView) findViewById(R.id.iv_setting);
        mBtnSetting.setOnClickListener(this);

        mBtnLock = (ImageView) findViewById(R.id.iv_lock);
        mBtnLock.setOnClickListener(this);

        mBtnPosition = (ImageView) findViewById(R.id.iv_position);
        mBtnPosition.setOnClickListener(this);

        mBtnCall = (ImageView) findViewById(R.id.iv_call);
        mBtnCall.setOnClickListener(this);

        mTitle = (TextView) findViewById(R.id.title);
        mTitle.setText("真刻");


        Drawable drawable_btn_press=getResources().getDrawable(R.drawable.luyin_4);
        drawable_btn_press.setBounds(0,0,200,200);//第一0是距左边距离，第二0是距上边距离，第三、第四分别是长宽
        mBtnRecord.setCompoundDrawables(drawable_btn_press,null,null,null);//只放上边
        Drawable drawable_btn_photo=getResources().getDrawable(R.drawable.xiangji);
        drawable_btn_photo.setBounds(0,0,200,200);//第一0是距左边距离，第二0是距上边距离，30、35分别是长宽
        mBtnTakePhoto.setCompoundDrawables(drawable_btn_photo,null,null,null);//只放左边
        Drawable drawable_btn_video=getResources().getDrawable(R.drawable.luxiang);
        drawable_btn_video.setBounds(-30,0,230,200);//第一0是距左边距离，第二0是距上边距离，30、35分别是长宽
        mBtnTakeVideo.setCompoundDrawables(drawable_btn_video,null,null,null);//只放左边
        Drawable drawable_btn_upload=getResources().getDrawable(R.drawable.xiazai);
        drawable_btn_upload.setBounds(0,0,200,200);//第一0是距左边距离，第二0是距上边距离，30、35分别是长宽
        mBtnUpload.setCompoundDrawables(drawable_btn_upload,null,null,null);//只放左边
    }

    @Override
    protected void onResume() {
        super.onResume();
        imgName = "";
        imgPath = "";
        mp3Name = "";
        mp3Path = "";
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_record:
                onMainInitRecord();
                break;

            case R.id.btn_take_photo:
                startTake();
                break;

            case R.id.btn_take_video:
                startTakeVideo();
                break;

            case R.id.btn_upload:
                review(false);
                break;

            case R.id.iv_setting:
                userSetting();
                break;

            case R.id.iv_lock:
                userLock();
                break;

            case R.id.iv_position:
                figurePosition();
                break;

            case R.id.iv_call:
                helpCall();
                break;
        }
    }

    /**
     * 进入录音功能
     */
    protected void onMainInitRecord() {
        Intent intent = new Intent(this, RecordActivity.class);
        startActivity(intent);
    }


    /**
     * 进入拍摄功能，并处理返回结果
     */
    public void startTake() {
        startActivityForResult(new Intent(MainActivity.this, TakePhotoActivity.class), 222);
    }

    /**
     * 进入视频功能，并处理返回结果
     */
    public void startTakeVideo(){
        startActivity(new Intent(MainActivity.this, TakeVideoActivity.class));
    }

    /**
     * 拍摄功能返回结果的处理
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case 222:
                imgPath = data.getStringExtra("imgPath");
                imgName = data.getStringExtra("imgName");
                break;
        }
    }


    /**
     * 进入本地同步功能
     * @param isImageMode true for image, false for audio
     */
    private void review(boolean isImageMode) {
        Intent intent = new Intent(this, ReviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("isImageMode", isImageMode);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
     * 设置
     */
    private void userSetting() {
        showToast("设置功能还未开启");
//        Intent intent = new Intent(this, SettingActivity.class);
//        startActivity(intent);
    }

    /**
     * 锁定
     */
    private void userLock() {
        sendBroadcast(new Intent("action.exit"));
    }

    /**
     * 定位
     */
    private void figurePosition() {
        showToast("定位功能还未开启");
//        Intent intent = new Intent(this, HistoryActivity.class);
//        startActivity(intent);
    }

    /**
     * 电话
     */
    private void helpCall() {
        Intent intent = new Intent(this, CallActivity.class);
        startActivity(intent);
    }

//    /**
//     * 上传文件到服务器
//     * @param filePath
//     * @param fileName
//     */
//    private void upload(final String filePath, final String fileName) {
//        // 网络操作，但开一个线程进行处理
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                FtpClient ftpClient = new FtpClient();
//                Log.e("TAG", "filePath:" + filePath);
//                Log.e("TAG", "fileName:" + fileName);
//                final String str = ftpClient.ftpUpload(filePath, fileName);
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (str.equals("1")) {
//                            Toast.makeText(MainActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
//                        } else {
//                            Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//            }
//        }).start();
//    }


    //****************注册广播监听网络变化************
    private IntentFilter intentFilter;
    private BaseActivity.NetworkChangeReceiver networkChangeReceiver;
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