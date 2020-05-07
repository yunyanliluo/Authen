package com.sid.soundrecorderutils.service;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.sid.soundrecorderutils.api.API;
import com.sid.soundrecorderutils.db.Diary;
import com.sid.soundrecorderutils.db.DiaryDataBaseManager;
import com.sid.soundrecorderutils.tcp.SocketMsg;
import com.sid.soundrecorderutils.util.FileUtil;
import com.sid.soundrecorderutils.util.LogUtils;
import com.sid.soundrecorderutils.util.StringUtil;
import com.sid.soundrecorderutils.view.CallActivity;
import com.sid.soundrecorderutils.view.LoginActivity;
import com.sid.soundrecorderutils.view.MainActivity;
import com.sid.soundrecorderutils.view.RecordActivity;
import com.sid.soundrecorderutils.view.ReviewActivity;
import com.sid.soundrecorderutils.view.SelectActivity;
import com.sid.soundrecorderutils.view.SignupActivity;
import com.sid.soundrecorderutils.view.TakePhotoActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;

public class UploadService extends Service {
    private String TAG = "SoundRecordService";
    private boolean canRun;


    Handler toastHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case -1:
                    Toast.makeText(getApplicationContext(), msg.obj + "上传失败（文件未找到）", Toast.LENGTH_LONG).show();
                    break;
                case 0:
                    Toast.makeText(getApplicationContext(), msg.obj + "上传失败，延时上传", Toast.LENGTH_LONG).show();

                    break;
                case 1:
                    Toast.makeText(getApplicationContext(), msg.obj + "上传成功", Toast.LENGTH_LONG).show();
                    break;
                case -2:
                    Toast.makeText(getApplicationContext(), msg.obj + "上传失败,文件被篡改", Toast.LENGTH_LONG).show();
                    break;
                case -3:
                    Toast.makeText(getApplicationContext(), msg.obj + "上传失败(hash上传失败)，请稍后再试", Toast.LENGTH_LONG).show();
                    break;
            }


        }
    };


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final Context context = getApplicationContext();


        canRun = true;

        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void run() {

                System.out.println("启动服务");
                //对sharedpreference中的文件进行上传
                SharedPreferences sharedPreferences = getSharedPreferences("upload", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                String uploadList = sharedPreferences.getString("uploadList", "");
                System.out.println(uploadList);
                API api = new API(context);
                while (canRun && !uploadList.equals("")) {
                    System.out.println("启动服务-开始上传");
                    String[] res = StringUtil.getAndDelete(uploadList);
                    //剩余待上传列表
                    uploadList = res[1];
                    editor.putString("uploadList", uploadList);
                    editor.commit();
                    //第一个待上传文件
                    String filenameAndFilepath = res[0];
                    System.out.println("srvice-uploadList: " + uploadList);
                    System.out.println("filenameAndFilepath: " + filenameAndFilepath);
                    final String filename = filenameAndFilepath.split("\\|")[0];
                    String filepath = filenameAndFilepath.split("\\|")[1];
                    System.out.println("filename: " + filename);
                    System.out.println("filepath: " + filepath);

                    Message message = new Message();
                    message.obj = filename;

                    //=====================上传之前校验hash值=========================
                    DiaryDataBaseManager diaryDataBaseManager = new DiaryDataBaseManager(context);
                    Diary startQueryDiary = new Diary(filename.substring(0, 10), filename.substring(11, 19), 0);
                    Diary queryResult = diaryDataBaseManager.queryDiary(startQueryDiary);
                    String ordinalHash = queryResult.hashcode;
                    String currentHash = FileUtil.getFileHash(filepath);
                    if (!ordinalHash.equals(currentHash)) {
                        //hash不同，文件被篡改
                        message.what = -2;
                        toastHandler.sendMessage(message);
                        continue;
                    }
                    //=====================上传之前校验hash值=========================
                    //=====================上传文件之前先上传hash=====================
                    String[] res0 = api.hash(filename,currentHash,String.valueOf(System.currentTimeMillis()));
                    if(res0 == null){
                        Log.e("UPLOAD","上传hash失败");
                    }else if (res0[0].equals("-1")){
                        //返回值-1，上传失败
                        message.what = -3;
                        toastHandler.sendMessage(message);
                        continue;
                    }
                    //=====================上传文件之前先上传hash=====================


                    final int responseCode = api.upload(filename, filepath);
                    if (responseCode == -1) {
                        message.what = -1;
                    } else if (responseCode == 0) {
                        message.what = 0;
                    } else {
                        message.what = 1;
                    }
                    toastHandler.sendMessage(message);
                }
                System.out.println("启动服务-上传完毕");
                stopSelf();
            }
        }).start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //关掉上传进程
        canRun = false;
        System.out.println("启动服务-停止服务");


    }


}
