package com.sid.soundrecorderutils.record;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaDataSource;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.sid.soundrecorderutils.util.FileUtil;
import com.sid.soundrecorderutils.view.TakePhotoActivity;

import java.io.ByteArrayOutputStream;
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

    public File changefile(File file)  {//对音频文件解密
        File tempMp3=null;
        try {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
            SecretKeySpec sks = new SecretKeySpec(TakePhotoActivity.AES_KEY.getBytes(),
                    "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, sks);
            //CipherInputStream 为加密输入流
            CipherInputStream cis = new CipherInputStream(fis, cipher);
            int b;
            byte[] d = new byte[1024];
            while ((b = cis.read(d)) != -1) {
                out.write(d, 0, b);
            }
            out.flush();
            out.close();
            cis.close();
            //获取字节流显示图片
            byte[] bytes= out.toByteArray();//bytes

            tempMp3 = File.createTempFile("temp234", ".mp3");
            FileOutputStream fos = new FileOutputStream(tempMp3);
            fos.write(bytes);
            fos.close();
            //JVM退出前删掉文件
            tempMp3.deleteOnExit();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        return  tempMp3;
    }
    // 获取录音时长
    public Integer getRecordDuration(File file) throws IOException {
        if (file.exists() && file != null) {
//            Uri uri = Uri.fromFile(file);
            FileInputStream fis = new FileInputStream(changefile(file));
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(fis.getFD());
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
        //解密 byte转换为MP3
        //------------------

        if (file.exists() && file != null) {
//            Uri uri = Uri.fromFile(file);
            FileInputStream fis = new FileInputStream(changefile(file));
            try {
                mediaPlayer.reset();
                    mediaPlayer.setDataSource(fis.getFD());
//                mediaPlayer.SetDataSource(new StreamMediaDataSource(new System.IO.MemoryStream(bytes)));
//                    mediaPlayer.setDataSource(mcontext, uri);
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