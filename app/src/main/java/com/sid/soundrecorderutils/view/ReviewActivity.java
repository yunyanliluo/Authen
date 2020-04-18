package com.sid.soundrecorderutils.view;

import android.app.Activity;
import android.content.Context;
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
import com.sid.soundrecorderutils.util.FileUtil;
import com.sid.soundrecorderutils.util.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class ReviewActivity extends BaseActivity {

    private RecyclerView recyclerView;//声明RecyclerView
    private DiaryRecycleAdapter adapterDiary;//声明适配器
    private Context context;
    private List<String> ImageList;
    private List<String> AudioList;
    private Boolean isImageMode = false; //true for Image, false for Audio
    private static DiaryDataBaseManager diaryDataBaseManager;
    private TextView mTvRvTitle;
    private ImageView mIvRvBack;

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

        mTvRvTitle = (TextView) findViewById(R.id.tv_rvtitle);
        mIvRvBack = (ImageView) findViewById(R.id.iv_rvback);
        mIvRvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if(isImageMode) {
            mTvRvTitle.setText("所有照片");
            showImage();
            adaptList(ImageList,isImageMode);
        }
        else {
            mTvRvTitle.setText("所有录音");
            showAudio();
            adaptList(AudioList, isImageMode);
        }


    }
    private void adaptList(List<String> list, boolean isImageMode) {
        adapterDiary = new DiaryRecycleAdapter(context, list, isImageMode);
        LinearLayoutManager manager = new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapterDiary);
    }

    private void showAudio() {
        Map<String, String> map = new HashMap<>();
        map = FileUtil.getFileName(AudioRecoderUtils.MP3_PATH);
        String[] arrs = null;
        arrs = new String[map.values().size()];
        arrs = map.values().toArray(arrs);
        LogUtils.e("show", "mp3 arrs:" + Arrays.toString(arrs));
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

    private void showImage() {
        Map<String, String> map = new HashMap<>();
        map = FileUtil.getFileName(TakePhotoActivity.PATH_IMAGES);
        String[] arrs = null;
        arrs = new String[map.values().size()];
        arrs = map.values().toArray(arrs);
        LogUtils.e("show", "img arrs:" + Arrays.toString(arrs));
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
}
