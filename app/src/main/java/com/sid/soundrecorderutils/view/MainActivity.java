package com.sid.soundrecorderutils.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
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
import com.sid.soundrecorderutils.util.FileUtil;
import com.sid.soundrecorderutils.util.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private String TAG = "MainActivity";
    private Button mBtnPress, /*mBtnPlay,*/
            mBtnTakePhoto, mBtnUpload, mBtnReviewAudio, mBtnReviewPhoto, mBtnSetting;
    private ImageView mIvSetting, mIvLock;
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
        mBtnPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    onMainInitRecord();
            }
        });

        mBtnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTake();
            }
        });
    }

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
     * 拍照
     */
    public void startTake() {
        startActivityForResult(new Intent(MainActivity.this, TakePhotoActivity.class), 222);
    }

    /**
     * 初始化UI
     */
    private void initView() {
        mBtnPress = (Button) findViewById(R.id.btn_press);
        mBtnTakePhoto = (Button) findViewById(R.id.btn_take_photo);
        mBtnUpload = (Button) findViewById(R.id.btn_upload);
        mBtnUpload.setOnClickListener(this);
        mBtnSetting = (Button) findViewById(R.id.btn_setting);
        mBtnReviewAudio = (Button) findViewById(R.id.btn_review_audio);
        mBtnReviewAudio.setOnClickListener(this);
        mBtnReviewPhoto = (Button) findViewById(R.id.btn_review_photo);
        mBtnReviewPhoto.setOnClickListener(this);
        mBtnSetting = (Button) findViewById(R.id.btn_setting);
        mBtnSetting.setOnClickListener(this);
        mTitle = (TextView) findViewById(R.id.title);
        mTitle.setText("真刻");
//        mIvSetting = (ImageView) findViewById(R.id.iv_setting);
//        mIvLock = (ImageView) findViewById(R.id.iv_lock);


        Drawable drawable_btn_press=getResources().getDrawable(R.drawable.record_tran);
        drawable_btn_press.setBounds(0,0,400,300);//第一0是距左边距离，第二0是距上边距离，第三、第四分别是长宽
        mBtnPress.setCompoundDrawables(null,drawable_btn_press,null,null);//只放上边
        Drawable drawable_btn_photo=getResources().getDrawable(R.drawable.camera2_tran);
        drawable_btn_photo.setBounds(0,0,400,300);//第一0是距左边距离，第二0是距上边距离，30、35分别是长宽
        mBtnTakePhoto.setCompoundDrawables(null,drawable_btn_photo,null,null);//只放左边
        Drawable drawable_btn_review_audio=getResources().getDrawable(R.drawable.recorder3_tran);
        drawable_btn_review_audio.setBounds(0,0,400,300);//第一0是距左边距离，第二0是距上边距离，30、35分别是长宽
        mBtnReviewAudio.setCompoundDrawables(null,drawable_btn_review_audio,null,null);//只放左边
        Drawable drawable_btn_review_photo=getResources().getDrawable(R.drawable.gallery2_tran);
        drawable_btn_review_photo.setBounds(0,0,400,300);//第一0是距左边距离，第二0是距上边距离，30、35分别是长宽
        mBtnReviewPhoto.setCompoundDrawables(null,drawable_btn_review_photo,null,null);//只放左边
        Drawable drawable_btn_upload=getResources().getDrawable(R.drawable.file_tran);
        drawable_btn_upload.setBounds(0,0,400,300);//第一0是距左边距离，第二0是距上边距离，30、35分别是长宽
        mBtnUpload.setCompoundDrawables(null,drawable_btn_upload,null,null);//只放左边
        Drawable drawable_btn_sett=getResources().getDrawable(R.drawable.setting7_tran);
        drawable_btn_sett.setBounds(0,0,400,300);//第一0是距左边距离，第二0是距上边距离，30、35分别是长宽
        mBtnSetting.setCompoundDrawables(null,drawable_btn_sett,null,null);//只放左边

    }

    @Override
    protected void onResume() {
        super.onResume();
        imgName = "";
        imgPath = "";
        mp3Name = "";
        mp3Path = "";
    }

    protected void onMainInitRecord() {
        Intent intent = new Intent(this, RecordActivity.class);
        startActivity(intent);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_upload:

                //弹出单选对话框选择
                final String[] arr = {"图片", "音频"};//选择的选项
                final int[] size = {-1};
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("选择上传文件类型")
                        .setSingleChoiceItems(arr, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int witch) {
                                size[0] = witch;
                                LogUtils.e("TAG", "当前选择的类型:" + size[0]);
                            }
                        }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final ArrayList<Integer> integer = new ArrayList<Integer>();
                        Map<String, String> map = new HashMap<>();
                        String[] arrs = null;
                        if (size[0] == 0) {
                            if(TakePhotoActivity.PATH_IMAGES==null) {
                                showToast("没有图片");
                            }
                            else {
                                map = FileUtil.getFileName(TakePhotoActivity.PATH_IMAGES);
                                if(map.size()==0) {
                                    showToast("没有图片");
                                }
                                else {
                                    arrs = new String[map.values().size()];
                                    arrs = map.values().toArray(arrs);
                                    LogUtils.e("TAG", "image arrs:" + Arrays.toString(arrs));
                                }
                            }

                        } else if (size[0] == 1) {
                            if(AudioRecoderUtils.MP3_PATH==null) {
                                showToast("没有音频");
                            }
                            else {
                                map = FileUtil.getFileName(AudioRecoderUtils.MP3_PATH);
                                if(map.size()==0) {
                                    showToast("没有音频");
                                }
                                else {
                                    arrs = new String[map.values().size()];
                                    arrs = map.values().toArray(arrs);
                                    LogUtils.e("TAG", "mp3 arrs:" + Arrays.toString(arrs));
                                }
                            }
                        }
                        if(map.size() != 0) {
                            final Map<String, String> finalMap = map;
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("选择上传文件")
                                    .setMultiChoiceItems(arrs,
                                            new boolean[arrs.length],
                                            new DialogInterface.OnMultiChoiceClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                                    if (b) {
                                                        integer.add(i);
                                                    } else {
                                                        integer.remove(i);
                                                    }
                                                }
                                            })
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            for (int teger : integer) {
                                                if (finalMap.get(String.valueOf(teger)).endsWith(".mp3")) {
                                                    upload(AudioRecoderUtils.MP3_PATH + finalMap.get(String.valueOf(teger)), finalMap.get(String.valueOf(teger)));
                                                }
                                                if (finalMap.get(String.valueOf(teger)).endsWith(".jpg")) {
                                                    upload(TakePhotoActivity.PATH_IMAGES + finalMap.get(String.valueOf(teger)), finalMap.get(String.valueOf(teger)));
                                                }
                                            }
                                        }
                                    }).setNegativeButton("返回", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    }).show();
                        }
                    }
                }).setNegativeButton("返回", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
                break;

            case R.id.btn_review_audio:
                review(false);
                break;

            case R.id.btn_review_photo:
                review(true);
                break;

//            case R.id.iv_setting:
//                userSetting();
//                break;
//
//            case R.id.iv_lock:
//                userLock();
//                break;
        }
    }

    private void upload(final String filePath, final String fileName) {
        // 网络操作，但开一个线程进行处理
        new Thread(new Runnable() {
            @Override
            public void run() {
                FtpClient ftpClient = new FtpClient();
                Log.e("TAG", "filePath:" + filePath);
                Log.e("TAG", "fileName:" + fileName);
                final String str = ftpClient.ftpUpload(filePath, fileName);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (str.equals("1")) {
                            Toast.makeText(MainActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    private void review(boolean isImageMode) {
        Intent intent = new Intent(this, ReviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("isImageMode", isImageMode);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void userSetting() {
        showToast("设置功能还未开启");
//        Intent intent = new Intent(this, HistoryActivity.class);
//        startActivity(intent);
    }

    private void userLock() {
        showToast("密码功能还未开启");
//        Intent intent = new Intent(this, HistoryActivity.class);
//        startActivity(intent);
    }
}