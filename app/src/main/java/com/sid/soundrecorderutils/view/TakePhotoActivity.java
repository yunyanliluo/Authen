package com.sid.soundrecorderutils.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.senierr.shootbutton.ShootButton;
import com.sid.soundrecorderutils.R;
import com.sid.soundrecorderutils.db.Diary;
import com.sid.soundrecorderutils.db.DiaryDataBaseManager;
import com.sid.soundrecorderutils.util.DateUtil;
import com.sid.soundrecorderutils.util.FileUtil;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;


public class TakePhotoActivity extends BaseActivity {
    RelativeLayout relativeLayout = null;
    public static String PATH_IMAGES = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator + "SoundRecord/image/";
    private Camera camera = null;
    private TakePhotoActivity.CameraView cv = null;
    List<String> dList = new ArrayList<>();
    private ShootButton mBtnShoot;
    public static final String AES_KEY = "PKUAuthenKey1024";



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_takephoto);
        if (dList.size() > 0) {
            dList.clear();
        }
        relativeLayout = (RelativeLayout) findViewById(R.id.cameraView);
        relativeLayout.removeAllViews();
        cv = new CameraView(TakePhotoActivity.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        relativeLayout.addView(cv, params);

        //拍摄按钮
        mBtnShoot = (ShootButton) findViewById(R.id.btn_shoot);
        mBtnShoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    takePicture();
            }
        });
    }

    /**
     * 主要的surfaceView，负责展示预览图片，camera的开关
     */
    class CameraView extends SurfaceView {
        private SurfaceHolder holder = null;

        public CameraView(Context context) {
            super(context);
            holder = this.getHolder();
            holder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceChanged(SurfaceHolder holder, int format,
                                           int width, int height) {
                    Log.e("TAG", "surfaceChanged...");
                    int PreviewWidth = 0;
                    int PreviewHeight = 0;
                    Camera.Parameters parameters = camera.getParameters();
                    //获得相机支持的照片尺寸,选择合适的尺寸
                    WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);//获取窗口的管理器
                    Display display = wm.getDefaultDisplay();//获得窗口里面的屏幕
                    List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
                    int maxSize = Math.max(display.getWidth(), display.getHeight());
                    int length = sizes.size();
                    if (maxSize > 0) {
                        for (int i = 0; i < length; i++) {
                            if (maxSize <= Math.max(sizes.get(i).width, sizes.get(i).height)) {
                                parameters.setPictureSize(sizes.get(i).width, sizes.get(i).height);
                                break;
                            }
                        }
                    }
                    List<Camera.Size> ShowSizes = parameters.getSupportedPreviewSizes();
                    int showLength = ShowSizes.size();
                    if (maxSize > 0) {
                        for (int i = 0; i < showLength; i++) {
                            if (maxSize <= Math.max(ShowSizes.get(i).width, ShowSizes.get(i).height)) {
                                parameters.setPreviewSize(ShowSizes.get(i).width, ShowSizes.get(i).height);
                                break;
                            }
                        }
                    }
                    camera.setParameters(parameters);
                    camera.setParameters(parameters);//把上面的设置 赋给摄像头

                    SurfaceTexture st = new SurfaceTexture(MODE_PRIVATE);
                    try {
                        camera.setPreviewTexture(st);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    camera.startPreview();

                    camera.autoFocus(new Camera.AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(boolean success, Camera camera) {
                            if (success) {
                                Log.e("TAG", "对焦成功...");
                                camera.cancelAutoFocus();
                            }
                        }
                    });

                }

                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    Log.e("TAG", "surfaceCreated...");
                    camera = Camera.open(0);
                    try {
                        camera.setDisplayOrientation(90);
                        camera.setPreviewDisplay(holder);
                    } catch (IOException e) {
                        camera.release();
                        camera = null;
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    //顾名思义可以看懂
                    Log.e("TAG", "surfaceDestroyed...");
                    camera.stopPreview();
                    camera.release();
                    camera = null;
                }
            });
        }
    }

    /**
     * 拍摄
     */
    private void takePicture() {
        if (camera != null) {
            camera.takePicture(null, null, picture);

        } else {
            Log.e("TAG", "camera=null");
        }
    }


    /**
     * 拍完照从回调中获取照片，并保存到本地
     */
    private Camera.PictureCallback picture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            relativeLayout.removeAllViews();
            cv = new CameraView(TakePhotoActivity.this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.FILL_PARENT,
                    LinearLayout.LayoutParams.FILL_PARENT);
            relativeLayout.addView(cv, params);
            dList.add(PATH_IMAGES + DateUtil.getDate() + ".jpg");
            Log.e("TAG", "" + dList.size());

            saveFile(data, dList.get(dList.size()-1));

        }
    };


    /**
     * 本地保存照片
     */
    public void saveFile(byte[] data, String path) {
        FileOutputStream outputStream = null;
        try {
            //设置照片文件夹
            File file = new File(PATH_IMAGES);
            if (!file.exists()) {
                file.mkdirs();
            }
            Log.e("TAG", path);

            //保存为jpeg
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] bytes = baos.toByteArray();
            //SecretKeySpec此类来根据一个字节数组构造一个 SecretKey
            SecretKeySpec sks = new SecretKeySpec(AES_KEY.getBytes(),
                    "AES");
            //Cipher类为加密和解密提供密码功能,获取实例
            Cipher cipher = Cipher.getInstance("AES");
            //初始化
            cipher.init(Cipher.ENCRYPT_MODE, sks);
            //CipherOutputStream 为加密输出流
                        outputStream = new FileOutputStream(path);
            CipherOutputStream cos = new CipherOutputStream(outputStream, cipher);
            cos.write(bytes);
            cos.flush();
            cos.close();


            //=================拍照完毕后存储文件hash值======================
            String _filename = dList.get(0).substring(dList.get(0).length() - 23, dList.get(0).length());
            String hash = FileUtil.getFileHash(path);
            String date = _filename.substring(0,10);
            String time = _filename.substring(11,19);
            Diary diary =  new Diary(date, time, 1, "hash",hash);
            DiaryDataBaseManager diaryDataBaseManager = new DiaryDataBaseManager(getApplicationContext());
            diaryDataBaseManager.insert(diary);
//            Diary queryRes = diaryDataBaseManager.queryDiary(new Diary(date,time,0));
//            System.out.println("******filename****:"+_filename);
//            System.out.println("******filepath****:"+filePath);
//            System.out.println("******hash****:"+hash);
//            System.out.println("******queryHash****:"+queryRes.hashcode);


            //=================拍照完毕后存储文件hash值======================




//            outputStream.close();
//            outputStream = new FileOutputStream(path);
//            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
//            bufferedOutputStream.write(baos.toByteArray(), 0, baos.toByteArray().length);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                //结束TakePhotoActivity返回MainActivity

                outputStream.close();
                Log.e("TAG", "saveFile...");
                Toast.makeText(TakePhotoActivity.this, dList.size() + "张", Toast.LENGTH_SHORT).show();
                if (dList.size() == 1) {
                    Intent intent = new Intent();
                    intent.putExtra("imgPath", dList.get(0));
                    intent.putExtra("imgName", dList.get(0).substring(dList.get(0).length() - 23, dList.get(0).length()));
                    setResult(222, intent);
                    TakePhotoActivity.this.finish();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 后退
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }
}