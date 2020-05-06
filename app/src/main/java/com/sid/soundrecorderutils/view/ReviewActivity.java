package com.sid.soundrecorderutils.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sid.soundrecorderutils.R;
import com.sid.soundrecorderutils.db.Diary;
import com.sid.soundrecorderutils.db.DiaryDataBaseManager;
import com.sid.soundrecorderutils.history.DiaryRecycleAdapter;
import com.sid.soundrecorderutils.record.AudioRecoderUtils;
import com.sid.soundrecorderutils.service.UploadService;
import com.sid.soundrecorderutils.util.FileUtil;
import com.sid.soundrecorderutils.util.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class ReviewActivity extends BaseActivity {
    private Context context;

    private RecyclerView recyclerView;//声明RecyclerView
    private DiaryRecycleAdapter adapterDiary;//声明适配器
    private TextView mTvRvTitle;
    private ImageView mIvRvBack, mIvImage, mIvAudio;
    private Boolean isImageMode = false; //true for Image, false for Audio
    private List<String> ImageList;
    private List<String> AudioList;
    private static DiaryDataBaseManager diaryDataBaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        if(diaryDataBaseManager == null) {
            diaryDataBaseManager = new DiaryDataBaseManager(this);
        }
        setContentView(R.layout.activity_review);
        recyclerView = (RecyclerView) findViewById(R.id.lo_recyclerview);

        Bundle bundle = getIntent().getExtras();
        isImageMode = (boolean) bundle.getSerializable("isImageMode");

        AudioList = new ArrayList<>();
        ImageList = new ArrayList<>();

        //顶部标题和后退按钮
        mTvRvTitle = (TextView) findViewById(R.id.tv_rvtitle);
        mIvRvBack = (ImageView) findViewById(R.id.iv_rvback);
        mIvRvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //底部菜单的image按钮
        mIvImage = (ImageView) findViewById(R.id.iv_image);
        mIvImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isImageMode) return;
                review(true);
                finish();
            }
        });

        //底部菜单的audio按钮
        mIvAudio = (ImageView) findViewById(R.id.iv_audio);
        mIvAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isImageMode == false) return;
                review(false);
                finish();
            }
        });

        if(isImageMode) {
            mTvRvTitle.setText("所有照片");
            mIvImage.setImageDrawable(getResources().getDrawable(R.drawable.tupian_blue));
            mIvAudio.setImageDrawable(getResources().getDrawable(R.drawable.yinpin_4));
            showImage();
            adaptList(ImageList,isImageMode);
        }
        else {
            mTvRvTitle.setText("所有录音");
            mIvImage.setImageDrawable(getResources().getDrawable(R.drawable.tupian));
            mIvAudio.setImageDrawable(getResources().getDrawable(R.drawable.yinpin_blue));
            showAudio();
            adaptList(AudioList, isImageMode);
        }


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
     * 设置布局管理器和适配器，显示页面
     * @param list
     * @param isImageMode
     */
    private void adaptList(List<String> list, boolean isImageMode) {
        adapterDiary = new DiaryRecycleAdapter(context, list, isImageMode); //适配器
        LinearLayoutManager manager = new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false); //布局管理器
        recyclerView.setLayoutManager(manager); //设置布局管理器
        recyclerView.setAdapter(adapterDiary); //设置适配器
    }

    /**
     * 查询本地音频文件，查询数据库discription，保存到AudioList
     */
    private void showAudio() {
        //查询本地保存的音频文件
        Map<String, String> map = new HashMap<>();
        map = FileUtil.getFileName(AudioRecoderUtils.MP3_PATH);
        String[] arrs = null;
        arrs = new String[map.values().size()];
        arrs = map.values().toArray(arrs);
        LogUtils.e("show", "mp3 arrs:" + Arrays.toString(arrs));

        //从本地数据库查询discription
        //根据date，time查询discription，arr_dis是arr + "_" + discription
        for (String arr : arrs) {
            String arr_dis = new String();
            Diary startQueryDiary;
            startQueryDiary = new Diary(arr.substring(0,10), arr.substring(11,19), 0);
            Diary diaryQuery = diaryDataBaseManager.queryDiary(startQueryDiary);

            if(diaryQuery != null)
                Log.d(TAG, "showAudio: " + diaryQuery.toString());
            if(diaryQuery == null || diaryQuery.date == null) {
                diaryDataBaseManager.insert(arr.substring(0,10), arr.substring(11,19), 0);
                Log.d(TAG, "showAudio: insert: " + arr.substring(0,10) + "/" + arr.substring(11,19) + "/0" );
                arr_dis = arr + "_";
            }
            else if(diaryQuery.discription == null) {
                arr_dis = arr + "_";
            }
            else {
                arr_dis = arr + "_" + diaryQuery.discription;
            }

            AudioList.add(arr_dis);//\n\n");
            Log.d("filename", ": " + arr);
            Log.d(TAG, "showAudio: " + arr_dis);
        }
    }

    /**
     * 查询本地拍摄文件，查询数据库discription，保存到ImageList
     */
    private void showImage() {
        //查询本地保存的拍摄文件
        Map<String, String> map = new HashMap<>();
        map = FileUtil.getFileName(TakePhotoActivity.PATH_IMAGES);
        String[] arrs = null;
        arrs = new String[map.values().size()];
        arrs = map.values().toArray(arrs);
        LogUtils.e("show", "img arrs:" + Arrays.toString(arrs));

        //从本地数据库查询discription
        //根据date，time查询discription，arr_dis是arr + "_" + discription
        for (String arr : arrs) {
            String arr_dis = new String();
            Diary startQueryDiary;
            startQueryDiary = new Diary(arr.substring(0,10), arr.substring(11,19), 1);
            Diary diaryQuery = diaryDataBaseManager.queryDiary(startQueryDiary);

            if(diaryQuery != null)
                Log.d(TAG, "showImage: " + diaryQuery.toString());
            if(diaryQuery == null || diaryQuery.date == null || diaryQuery.date == "null") {
                diaryDataBaseManager.insert(arr.substring(0,10), arr.substring(11,19), 1);
                Log.d(TAG, "showImage: insert: " + arr.substring(0,10) + "/" + arr.substring(11,19) + "/1" );
                arr_dis = arr + "_";
            }
            else if(diaryQuery.discription == null || diaryQuery.discription == "null") {
                arr_dis = arr + "_";
            }
            else {
                arr_dis = arr + "_" + diaryQuery.discription;
            }

            ImageList.add(arr_dis);//\n\n");
            Log.d("filename", ": " + arr);
            Log.d(TAG, "showImage: " + arr_dis);
        }
    }

    private void review(boolean isImageMode) {
        Intent intent = new Intent(this, ReviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("isImageMode", isImageMode);
        intent.putExtras(bundle);
        startActivity(intent);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(networkChangeReceiver);
    }

//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
//            finish();
//        }
//        return super.onKeyUp(keyCode, event);
//    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
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
