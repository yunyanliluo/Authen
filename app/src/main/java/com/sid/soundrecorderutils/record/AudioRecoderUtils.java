package com.sid.soundrecorderutils.record;

import android.app.Activity;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.sid.soundrecorderutils.db.DiaryDataBaseManager;
import com.sid.soundrecorderutils.util.DateUtil;
import com.sid.soundrecorderutils.util.LogUtils;
import com.sid.soundrecorderutils.view.MainActivity;

import java.io.File;
import java.io.IOException;

/**
 * 录音类
 */
public class AudioRecoderUtils {
    private String TAG = "AudioRecoderUtils";
    private String filePath;
    private String fileName;
    public static String MP3_PATH = Environment.getExternalStorageDirectory() + "/SoundRecord/record/";
    private String FolderPath = MP3_PATH;
    private MediaRecorder mMediaRecorder;
    public static final int MAX_LENGTH = 1000 * 60 * 10;// 最大录音时长1000*60*10;

    private OnAudioStatusUpdateListener audioStatusUpdateListener;

    public AudioRecoderUtils(Context context) {
        File path = new File(FolderPath);
        if (!path.exists())
            path.mkdirs();
    }

    private long startTime;
    private long endTime;
    private long pauseTime, beginPauseTime; //暂停累计时长和暂停起始时刻


    /**
     * 开始录音 使用amr格式
     * 录音文件
     *
     * @return
     */
    public void startRecord() {
        // 开始录音
        /* ①Initial：实例化MediaRecorder对象 */
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }
        try {
            /* ②setAudioSource/setVedioSource */
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// 设置麦克风
            /* ②设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default 声音的（波形）的采样 */
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            /*
             * ②设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default THREE_GPP(3gp格式
             * ，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
             */
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            fileName = DateUtil.getDate() + ".mp3";
            filePath = FolderPath + fileName;
            /* ③准备 */
            mMediaRecorder.setOutputFile(filePath);
            mMediaRecorder.setMaxDuration(MAX_LENGTH);
            mMediaRecorder.prepare();
            /* ④开始 */
            mMediaRecorder.start();
            // AudioRecord audioRecord.
            /* 获取开始时间* */
            startTime = System.currentTimeMillis();
            pauseTime = 0;
            beginPauseTime = startTime-1;
            updateMicStatus();
            Log.e("fan", "startTime" + startTime);
        } catch (IllegalStateException e) {
            Log.i(TAG, "call startAmr(File mRecAudioFile) failed! IllegalStE " + e.getMessage());
        } catch (IOException e) {
            Log.i(TAG, "call startAmr(File mRecAudioFile) failed! IOE " + e.getMessage());
        }
    }

    /**
     * 停止录音
     */
    public long stopRecord() {
        if (mMediaRecorder == null) {
            return 0L;
        }
        endTime = System.currentTimeMillis();
        try {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            audioStatusUpdateListener.onStop(filePath);
            filePath = "";
        } catch (RuntimeException e) {
            if (mMediaRecorder != null) {
                mMediaRecorder.reset();
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
//            diaryDataBaseManager.insert(fileName.substring(0,10), fileName.substring(11,19), 0);
            filePath = "";
        }
        return endTime - startTime;
    }

    /**
     * 取消录音
     */
    public void cancelRecord() {
        try {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        } catch (RuntimeException e) {
            try {
                mMediaRecorder.reset();
                mMediaRecorder.release();
                mMediaRecorder = null;
            } catch (NullPointerException e1) {
                Log.e("TAG", "空指针异常...");
            }
        }
        try {
            File file = new File(filePath);
            if (file.exists())
                file.delete();
            filePath = "";
        } catch (NullPointerException e) {
            LogUtils.e(TAG, "cancelRecord:" + e.toString());
        }
    }

    private final Handler mHandler = new Handler();
    private Runnable mUpdateMicStatusTimer = new Runnable() {
        public void run() {
            updateMicStatus();
        }
    };

    private int BASE = 1;
    private int SPACE = 100;// 间隔取样时间

    public void setOnAudioStatusUpdateListener(OnAudioStatusUpdateListener audioStatusUpdateListener) {
        this.audioStatusUpdateListener = audioStatusUpdateListener;
    }

    /**
     * 暂停录音
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void pauseRecord() {
        try {
            mMediaRecorder.pause();
            beginPauseTime = System.currentTimeMillis();
        } catch (RuntimeException e) {
            ;
        }
    }

    /**
     * 继续录音
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void resumeRecord() {
        try {
            mMediaRecorder.resume();
            pauseTime += System.currentTimeMillis() - beginPauseTime;
            beginPauseTime = startTime-1;
        } catch (RuntimeException e) {
            ;
        }
    }

    /**
     * 更新麦克状态
     */
    private void updateMicStatus() {
        if (mMediaRecorder != null) {
            double ratio = (double) mMediaRecorder.getMaxAmplitude() / BASE;
            double db = 0;// 分贝
            if (ratio > 1) {
                db = 20 * Math.log10(ratio);
                if (null != audioStatusUpdateListener) {
                    if(beginPauseTime == startTime-1) {
                        audioStatusUpdateListener.onUpdate(db, System.currentTimeMillis() - startTime - pauseTime);
                    }
                    else {
                        audioStatusUpdateListener.onUpdate(db, beginPauseTime - startTime - pauseTime);
                    }
                }
            }
            mHandler.postDelayed(mUpdateMicStatusTimer, SPACE);
        }
    }

    public interface OnAudioStatusUpdateListener {
        /**
         * 录音中...
         *
         * @param db   当前声音分贝
         * @param time 录音时长
         */
        public void onUpdate(double db, long time);

        /**
         * 停止录音
         *
         * @param filePath 保存路径
         */
        public void onStop(String filePath);
    }
}