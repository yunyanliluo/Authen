package com.sid.soundrecorderutils.record;

import android.app.Activity;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.sid.soundrecorderutils.db.Diary;
import com.sid.soundrecorderutils.db.DiaryDataBaseManager;
import com.sid.soundrecorderutils.util.DateUtil;
import com.sid.soundrecorderutils.util.FileUtil;
import com.sid.soundrecorderutils.util.LogUtils;
import com.sid.soundrecorderutils.view.MainActivity;
import com.sid.soundrecorderutils.view.TakePhotoActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
/**
 * 录音类
 */
public class AudioRecoderUtils {
    private String TAG = "AudioRecoderUtils";
    private String filePath;
    private String filePathtmp;
    private String fileName;
    private String fileNametmp;
    private FileInputStream fis = null;
    public static String MP3_PATH = Environment.getExternalStorageDirectory() + "/SoundRecord/record/";
    private String FolderPath = MP3_PATH;
    private MediaRecorder mMediaRecorder;
    public static final int MAX_LENGTH = 1000 * 60 * 10;// 最大录音时长1000*60*10;
    private Context context;
    private OnAudioStatusUpdateListener audioStatusUpdateListener;

    public AudioRecoderUtils(Context context) {
        this.context = context;
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
            fileNametmp = DateUtil.getDate()+"tmp" + ".mp3";
            filePathtmp=FolderPath + fileNametmp;
            filePath = FolderPath + fileName;
            /* ③准备 */
            mMediaRecorder.setOutputFile(filePathtmp);
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
            //加密
            fis = new FileInputStream(filePathtmp);
//            byte[] oldByte = new byte[(int) filePathtmp.length()];
            FileOutputStream fos = new FileOutputStream(filePath);
            //SecretKeySpec此类来根据一个字节数组构造一个 SecretKey
            SecretKeySpec sks = new SecretKeySpec(TakePhotoActivity.AES_KEY.getBytes(),
                    "AES");
            //Cipher类为加密和解密提供密码功能,获取实例
            Cipher cipher = Cipher.getInstance("AES");
            //初始化
            cipher.init(Cipher.ENCRYPT_MODE, sks);
            //CipherOutputStream 为加密输出流
            CipherOutputStream cos = new CipherOutputStream(fos, cipher);
            int b;
            byte[] d = new byte[1024];
            while ((b = fis.read(d)) != -1) {
                cos.write(d, 0, b);
            }
            cos.flush();
            cos.close();
            fos.close();
            fis.close();

            //加密结束
            //删除多余音频
            File file = new File(filePathtmp);
            // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
            if (file.exists() && file.isFile()) {
                file.delete();
            }
            //=================录音完毕后存储文件hash值======================
            String _filename = fileName;
            String hash = FileUtil.getFileHash(filePath);
            String date = _filename.substring(0,10);
            String time = _filename.substring(11,19);
            Diary diary =  new Diary(date, time, 0, "",hash);
            DiaryDataBaseManager diaryDataBaseManager = new DiaryDataBaseManager(context);
            diaryDataBaseManager.insert(diary);

//            Diary queryRes = diaryDataBaseManager.queryDiary(new Diary(date,time,0));
//            System.out.println("******filename****:"+_filename);
//            System.out.println("******filepath****:"+filePath);
//            System.out.println("******hash****:"+hash);
//            System.out.println("******queryHash****:"+queryRes.hashcode);

            //=================录音完毕后存储文件hash值======================
            audioStatusUpdateListener.onStop(filePath);



            filePath = "";
        } catch (RuntimeException | FileNotFoundException e) {
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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
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
            File file = new File(filePathtmp);
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