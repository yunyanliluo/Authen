package com.sid.soundrecorderutils.record;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.sid.soundrecorderutils.util.FileUtil;

import java.io.File;
import java.io.IOException;

/**
 * 播放录音类
 */
public class RecordPlayer {

    private static MediaPlayer mediaPlayer;
    public static RecordPlayer recordPlayerManager;

    private Context mcontext;
    private Handler mHandler;

    public RecordPlayer(Context context, Handler handler) {
        this.mcontext = context;
        this.mHandler = handler;
        if(recordPlayerManager == null) {
            recordPlayerManager = this;
        }
        if(mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
    }

    public static RecordPlayer getInstance() {
        return recordPlayerManager;
    }

    // 获取录音时长
    public Integer getRecordDuration(File file) throws IOException {
        if (file.exists() && file != null) {
            Uri uri = Uri.fromFile(file);
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(mcontext, uri);
                mediaPlayer.prepare();
            }
            catch(IOException e) {
                Log.d("TAG", "playRecordFile: IOException");
                return 0;
            }
            return mediaPlayer.getDuration();
        }
        else {
            return 0;
        }
    }

    // 播放录音文件
    public void playRecordFile(File file) throws IOException {
        if (file.exists() && file != null) {
            Uri uri = Uri.fromFile(file);
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(mcontext, uri);
                mediaPlayer.prepare();
            }
            catch(IOException e) {
                Log.d("TAG", "playRecordFile: IOException");
                return;
            }
            mediaPlayer.start();
            mediaPlayer.getDuration();
            Message msg = mHandler.obtainMessage();
            msg.what = 200;
            msg.obj = mediaPlayer.getDuration();
            mHandler.sendMessage(msg);
            Log.e("TAG", "音频时长:" + mediaPlayer.getDuration());
            //监听MediaPlayer播放完成
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer paramMediaPlayer) {
                    // TODO Auto-generated method stub
                    //弹窗提示
                    Toast.makeText(mcontext,
                            "播放完成",
                            Toast.LENGTH_SHORT).show();
                    Message msg = mHandler.obtainMessage();
                    msg.what = 400;
                    mHandler.sendMessage(msg);
                }
            });
        }
    }

    // 暂停播放录音
    public void pausePalyer() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            Log.e("TAG", "暂停播放");
        }
    }

    // 停止播放录音
    public void stopPalyer() {
        // 这里不调用stop()，调用seekto(0),把播放进度还原到最开始
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
            Log.e("TAG", "停止播放");
        }
    }
}