package com.sid.soundrecorderutils.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sid.soundrecorderutils.R;
import com.sid.soundrecorderutils.record.AudioRecoderUtils;
import com.sid.soundrecorderutils.service.UploadService;

public class RecordActivity extends BaseActivity {
    private String TAG = "RecordActivity";
    private ImageView mImgBtnStop, mImgBtnPause, mImgBtnCancel, mImgBtnRecBack;
    private Drawable drawable_play, drawable_pause, drawable_stop;
    private TextView mTv, mTvRecordTitle;
    private AudioRecoderUtils mRecoderUtils = null;
    private boolean isReCord = false;       //记录当前是否在录音
    private boolean isPause = false;        //记录当前是否在暂停录音
    private boolean isPlayer = false;       //记录当前是否在播放
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
        setContentView(R.layout.activity_record);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initView();
        mRecoderUtils = new AudioRecoderUtils(RecordActivity.this);
        mRecoderUtils.setOnAudioStatusUpdateListener(new AudioRecoderUtils.OnAudioStatusUpdateListener() {
            //录音中....db为声音分贝，time为录音时长
            @Override
            public void onUpdate(double db, long time) {
                int m = 00;
                int s = 00;
                if (time >= 60000) {
                    m = (int) (time / 60000);
                    s = (int) ((time / 1000) & 60);
                } else if (time < 60000) {
                    m = 00;
                    s = (int) (time / 1000);
                }
                Log.e("TAG", "当前时间:" + m + ":" + s);
                final int finalM = m;
                final int finalS = s;
                final String prefixM = "0";
                final String prefixS = "0";
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(finalM<10 && finalS<10) {
                            mTv.setText(prefixM + finalM + ":" + prefixS + finalS + "");
                        }
                        else if(finalM<10) {
                            mTv.setText(prefixM + finalM + ":" + finalS + "");
                        }
                        else if(finalS<10) {
                            mTv.setText(finalM + ":" + prefixS + finalS + "");
                        }
                        else {
                            mTv.setText(finalM + ":" + finalS + "");
                        }
                    }
                });
            }

            //录音结束，filePath为保存路径
            @Override
            public void onStop(String filepath) {
                showToast("录音保存至" + filepath);
                Log.e("TAG", "录音保存至" + filepath);
                mp3Path = filepath;
                mp3Name = filepath.substring(filepath.length() - 23, filepath.length());
            }
        });

        onInitRecord();

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

    /**
     * 初始化UI
     */
    private void initView() {
        mTvRecordTitle = (TextView) findViewById(R.id.tv_rectitle);
        mImgBtnRecBack = (ImageView) findViewById(R.id.iv_recback);
        mTv = (TextView) findViewById(R.id.tv_clock);
        mTv.setText("00:00");
        mImgBtnStop = (ImageView) findViewById(R.id.imgbtn_stop);
        mImgBtnPause = (ImageView) findViewById(R.id.imgbtn_pause);
        mImgBtnCancel = (ImageView) findViewById(R.id.imgbtn_cancel);

        drawable_play=getResources().getDrawable(R.drawable.ic_play_arrow_black_48dp);
        drawable_play.setBounds(0,0,50,50);
        drawable_pause=getResources().getDrawable(R.drawable.ic_pause_black_48dp);
        drawable_pause.setBounds(0,0,50,50);
        drawable_stop=getResources().getDrawable(R.drawable.ic_stop_black_48dp);
        drawable_stop.setBounds(0,0,50,50);
    }



    /**
     * 初始化录音
     */
    protected void onInitRecord() {
        if(!isReCord) {
            mTv.setVisibility(View.VISIBLE);
            mImgBtnStop.setVisibility(View.VISIBLE);
            mImgBtnStop.setImageDrawable(drawable_play);
            mImgBtnStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isReCord) {
                        onStopRecord();
                    }
                    else {
                        onStartRecord();
                    }
                }
            });
            mImgBtnPause.setVisibility(View.VISIBLE);
            mImgBtnPause.setImageDrawable(drawable_pause);
            mImgBtnPause.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(View v) {
                    if(isReCord) {
                        if(isPause) {
                            onResumeRecord();
                        }
                        else {
                            onPauseRecord();
                        }
                    }

                }
            });
            mImgBtnCancel.setVisibility(View.VISIBLE);
            mImgBtnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isReCord) {
                        onCancelRecord();
                    }

                }
            });
            mImgBtnRecBack.setVisibility(View.VISIBLE);
            mImgBtnRecBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onExitRecord();
                }
            });
            isReCord = false;
            isPause = false;
        }
    }

    /**
     * 开始录音
     */
    protected void onStartRecord() {
        if(!isReCord) {
            mImgBtnStop.setImageDrawable(drawable_stop);
            mRecoderUtils.startRecord();
            isReCord = true;
        }
    }

    /**
     * 暂停录音
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void onPauseRecord() {
        if(isReCord) {
            mRecoderUtils.pauseRecord();
            isPause = true;
            mImgBtnPause.setImageDrawable(drawable_play);
        }
    }

    /**
     * 继续录音
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void onResumeRecord() {
        if(isReCord) {
            mRecoderUtils.resumeRecord();
            isPause = false;
            mImgBtnPause.setImageDrawable(drawable_pause);
        }
    }

    /**
     * 停止录音
     */
    protected void onStopRecord() {
        if (isReCord) {
            mRecoderUtils.stopRecord();
            mTv.setText("00:00");
            isReCord = false;
            isPause = false;
            mImgBtnStop.setImageDrawable(drawable_play);
            mImgBtnPause.setImageDrawable(drawable_pause);
        }
    }

    /**
     * 取消录音
     */
    protected void onCancelRecord() {
        if(isReCord) {
            mRecoderUtils.cancelRecord();
            mTv.setText("00:00");
            mImgBtnStop.setImageDrawable(drawable_play);
            mImgBtnPause.setImageDrawable(drawable_pause);
            isReCord = false;
            isPause = false;
        }
    }

    /**
     * 退出录音
     */
    protected void onExitRecord() {
        if (isReCord) {
            onCancelRecord();
        }
        finish();
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
    protected void onStop() {
        super.onStop();
        if (isReCord) {
            mRecoderUtils.stopRecord();
            isReCord = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRecoderUtils != null) {
            mRecoderUtils.cancelRecord();
        }
        unregisterReceiver(networkChangeReceiver);
    }
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
