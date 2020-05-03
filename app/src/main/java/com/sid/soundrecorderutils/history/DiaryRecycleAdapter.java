package com.sid.soundrecorderutils.history;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sid.soundrecorderutils.R;
import com.sid.soundrecorderutils.db.Diary;
import com.sid.soundrecorderutils.db.DiaryDataBaseManager;
import com.sid.soundrecorderutils.ftp.FtpClient;
import com.sid.soundrecorderutils.record.AudioRecoderUtils;
import com.sid.soundrecorderutils.record.RecordPlayer;
import com.sid.soundrecorderutils.util.EditTextUtil;
import com.sid.soundrecorderutils.util.FileUtil;
import com.sid.soundrecorderutils.view.MainActivity;
import com.sid.soundrecorderutils.view.ReviewActivity;
import com.sid.soundrecorderutils.view.TakePhotoActivity;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
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
import javax.net.ssl.HandshakeCompletedListener;

public class DiaryRecycleAdapter extends RecyclerView.Adapter<DiaryRecycleAdapter.DiaryViewHolder>{
    private static final String TAG = DiaryRecycleAdapter.class.getSimpleName();
    private Context context;
    private List<String> stringList;
    private Boolean isImageMode = false; //true for Image, false for Audio
    private View inflater;
    private static RecordPlayer recordPlayerManager;
    private static DiaryDataBaseManager diaryDataBaseManager;

    //构造方法，传入数据
    public DiaryRecycleAdapter(Context context, List<String> stringList, boolean isImageMode){
        this.context = context;
        this.stringList = stringList;
        this.isImageMode = isImageMode;
        recordPlayerManager = new RecordPlayer(context, new Handler()).getInstance();
        if(diaryDataBaseManager == null) {
            diaryDataBaseManager = new DiaryDataBaseManager(context);
        }
    }

    @Override
    public DiaryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //创建ViewHolder，返回每一项的布局
        inflater = LayoutInflater.from(context).inflate(R.layout.item_diary,parent,false);
        DiaryViewHolder myViewHolder = new DiaryViewHolder(inflater);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final DiaryViewHolder holder, int position) {
        //将数据和控件绑定
        String diary = stringList.get(position);
        //distinguish date, title and content from a diary item
        holder.mTvDate.setText(diary.substring(0,4) + "年" + diary.substring(5,7) + "月" + diary.substring(8,10) + "日 "
                                +diary.substring(11,13) + "时" + diary.substring(14,16) + "分" + diary.substring(17,19) + "秒");
        Log.d(TAG, "onBindViewHolder: " + holder.mTvDate.getText());
        String discription = new String();
        Log.d(TAG, "onBindViewHolder: " + diary.length());
        Log.d(TAG, "onBindViewHolder: " + diary.substring(24));
        if(diary != null && diary.length()>24 && diary.substring(24)!="null"){
            discription = diary.substring(24);
            holder.mTvContent.setText(discription);
        }
        else { //显示hint
//            holder.mEtContent.setText("         一个人乘滴滴总是感觉害怕。最近赶项目，老板催得紧，迫不得已又在十点钟下班回家。\n          嘤嘤嘤，我又累又害怕，打开真刻APP录音才感觉大胆些……");
        }
        //设置可编辑的discription
        holder.mTvContent.setTag(position);
        holder.mTvContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final TextView tv = (TextView)v;
                final String content = tv.getText().toString();
                final EditText ETView = getEditTextUtil(tv);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setView(ETView);
                alertDialogBuilder.setTitle("编辑中……");
                alertDialogBuilder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String content_update = ETView.getText().toString();
                        Log.d(TAG, "onClick: new discription: " + content_update);
                        Integer v_position = (Integer) v.getTag();
                        Log.d(TAG, "onBindViewHolder: TextView of content: onClick: " + v_position);
                        updateContent(v_position, content_update);
                        tv.setText(content_update);
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
            }
        });

        //设置上传按钮
        holder.mIvUpload.setTag(position);
        holder.mIvUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setMessage("上传后，本文件将永久存档，不可篡改").setPositiveButton("上传", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Integer v_position = (Integer) v.getTag();
                        Log.d(TAG, "onBindViewHolder: onClick: " + v_position);
                        uploadFile(v_position);
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
            }
        });

        //设置删除按钮
        holder.mIvRemove.setTag(position);
        holder.mIvRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setMessage("删除后，将一同清除本地文件").setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Integer v_position = (Integer) v.getTag();
                        Log.d(TAG, "onBindViewHolder: onClick: " + v_position);
                        deleteFile(v_position);
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
            }
        });

        //根据图片或音频模式布局
        if(isImageMode) { //image
            Drawable drawable_cam = context.getResources().getDrawable(R.drawable.camera3_tran);
            drawable_cam.setBounds(0,0,30,30);
            holder.mIvCircle.setImageDrawable(drawable_cam);

            holder.mIvContent.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams params = holder.mIvContent.getLayoutParams();
            params.height = dpTopx(context,300);
            params.width = dpTopx(context,300);
            holder.mIvContent.setLayoutParams(params);
            Uri uri = Uri.fromFile(new File(TakePhotoActivity.PATH_IMAGES + diary.substring(0,23)));
            Log.d(TAG, "onBindViewHolder: " + TakePhotoActivity.PATH_IMAGES + diary.substring(0,23));
//            holder.mIvContent.setImageURI(uri);
            //------------------
            try {
                FileInputStream fis = null;
                fis = new FileInputStream(TakePhotoActivity.PATH_IMAGES + diary.substring(0,23));
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
                byte[] bytes= out.toByteArray();
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                holder.mIvContent.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //-----------
            holder.mTvTimelen.setVisibility(View.INVISIBLE);
            holder.mIvPlay.setVisibility(View.INVISIBLE);
        }
        else { //audio
            Drawable drawable_rec = context.getResources().getDrawable(R.drawable.recorder_tran);
            drawable_rec.setBounds(0,0,30,30);
            holder.mIvCircle.setImageDrawable(drawable_rec);

            holder.mIvContent.setVisibility(View.INVISIBLE);

            holder.mTvTimelen.setVisibility(View.VISIBLE);
            try {
                holder.mTvTimelen.setText(getDuration(diary.substring(0,23)));
            } catch (IOException e) {
                e.printStackTrace();
            }

            holder.mIvPlay.setVisibility(View.VISIBLE);
            holder.mIvPlay.setTag(position);
            Log.d(TAG, "onBindViewHolder: position:" + position);
            holder.mIvPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Integer v_position = (Integer) v.getTag();
                    Log.d(TAG, "onBindViewHolder: onClick: " + v_position);
                    playRecord(v_position);
                }
            });
        }

        //点击图片可以放大
        holder.mIvContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onThumbnailClick(v);
            }
        });

        //点击日记可以显示或消失上传和删除按钮
        holder.mLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.mIvUpload.getVisibility() == View.INVISIBLE) {
                    holder.mIvUpload.setVisibility(View.VISIBLE);
                    holder.mIvRemove.setVisibility(View.VISIBLE);
                }else {
                    holder.mIvUpload.setVisibility(View.INVISIBLE);
                    holder.mIvRemove.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        //返回Item总条数
        return stringList.size();
    }

    //内部类，绑定控件
    static class DiaryViewHolder extends RecyclerView.ViewHolder{
        TextView mTvDate;
        TextView mTvContent;
        TextView mTvTimelen;
        ImageView mIvCircle;
        ImageView mIvContent;
        ImageView mIvPlay;
        ImageView mIvUpload;
        ImageView mIvRemove;
        LinearLayout mLl;

        public DiaryViewHolder(View itemView) {
            super(itemView);
            mTvDate = (TextView) itemView.findViewById(R.id.tv_date);
            mTvContent = (TextView) itemView.findViewById(R.id.tv_content);
            mTvTimelen = (TextView) itemView.findViewById(R.id.tv_timelen);
            mIvCircle = (ImageView) itemView.findViewById(R.id.iv_circle);
            mIvContent = (ImageView) itemView.findViewById(R.id.iv_content);
            mIvPlay = (ImageView) itemView.findViewById(R.id.iv_play);
            mIvUpload = (ImageView) itemView.findViewById(R.id.iv_upload);
            mIvRemove = (ImageView) itemView.findViewById(R.id.iv_remove);
            mLl = (LinearLayout) itemView.findViewById(R.id.ll_item);
        }
    }

    /**
     * 图片大小单位 dp转px
     */
    private int dpTopx(Context context, float dp){
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    /**
     * 图片点击放大
     * @param v
     */
    public void onThumbnailClick(View v) {
        // 全屏显示的方法
        final Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        ImageView iv = (ImageView)v;
        ImageView imgView = getView(iv);
        dialog.setContentView(imgView);
        dialog.show();

        // 点击图片消失
        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dialog.dismiss();
            }
        });
    }

    /**
     * 从一个已有的imageview，生成一个新的imageview
     * @param iv
     * @return
     */
    private ImageView getView(ImageView iv) {
        ImageView imgView = new ImageView(context);
        imgView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        Drawable drawable = iv.getDrawable();
//        InputStream is = context.getResources().openRawResource(R.drawable.girl);
//        Drawable drawable = BitmapDrawable.createFromStream(is, null);
        imgView.setImageDrawable(drawable);

        return imgView;
    }

    /**
     * 从一个TextView，生成一个可编辑的EditText
     * @param tv 想要修改的某一条记录的TextView组件
     * @return
     */
    private EditText getEditTextUtil(TextView tv) {
        String content = tv.getText().toString();
        EditText etuView = new EditText(context);
        etuView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        Log.d(TAG, "getEditTextUtil: " + content);
        if(content==null ||content=="") {
            etuView.setHint("在这里写点儿什么……");
        }
        else {
            etuView.setText(content);
        }

        return etuView;
    }


    /**
     * 修改某一条记录的文字描述，更新到本地数据库
     * @param position 读取本地存储的所有音频/视频文件时，待上传文件所在的编号.stringList.get(position).substring(0,23)是文件名
     * @param content_update 新的文字描述
     */
    private void updateContent(Integer position, String content_update) {
        Log.d(TAG, "updateContent: new discription: " + content_update);
        String date = stringList.get(position).substring(0,10);
        String time = stringList.get(position).substring(11,19);
        Diary diary;
        if(isImageMode) {
            diary = new Diary(date, time, 1, content_update);
        }
        else {
            diary = new Diary(date, time, 0, content_update);
        }
        if(diary != null)
            Log.d(TAG, "updateContent: " +diary.toString());
        diaryDataBaseManager.update(diary);
    }

    /**
     * 播放录音文件
     * @param position 读取本地存储的所有音频文件时，待上传文件所在的编号.stringList.get(position).substring(0,23)是文件名
     */
    private void playRecord(Integer position) {
        String filename = stringList.get(position).substring(0,23);
        File mp3file = FileUtil.getFile(AudioRecoderUtils.MP3_PATH + filename);


//        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        try {

            recordPlayerManager.playRecordFile(mp3file);



        } catch(IOException e) {
            Log.d(TAG, "playRecord: IOException");
        }

//        try {
//
//
//
//        }
//        catch(IOException e) {
//            Log.d(TAG, "playRecord: IOException");
//        }
    }

    /**
     * 获取录音时长
     * @param filename 录音文件文件名
     * @return
     * @throws IOException
     */
    private String getDuration(String filename) throws IOException {
        Integer duration = recordPlayerManager.getRecordDuration(FileUtil.getFile(AudioRecoderUtils.MP3_PATH + filename));
        String str_ss = new String();
        String str_mm = new String();
        duration /= 1000; //ms -> second
        int ss = duration % 60;
        if(ss > 9) {
            str_ss = Integer.toString(ss);
        }
        else {
            str_ss = "0" + Integer.toString(ss);
        }
        int mm = duration / 60;
        if(ss > 9) {
            str_mm = Integer.toString(mm);
        }
        else {
            str_mm = "0" + Integer.toString(mm);
        }
        return str_mm + ":" + str_ss;
    }

    /**
     * 上传文件
     * @param position 读取本地存储的所有音频/视频文件时，待上传文件所在的编号.stringList.get(position).substring(0,23)是文件名
     */
    private void uploadFile(Integer position) {
        String filename = stringList.get(position).substring(0,23);
        if(isImageMode) {
            uploadFromDiary(TakePhotoActivity.PATH_IMAGES + filename, filename, new ReviewActivity(), context);
        }
        else {
            uploadFromDiary(AudioRecoderUtils.MP3_PATH + filename, filename, new ReviewActivity(), context);
        }
    }

    /**
     * 上传文件的ftp操作
     * @param filePath 完整路径
     * @param fileName 文件名
     * @param activity
     * @param context
     */
    private void uploadFromDiary(final String filePath, final String fileName, final Activity activity, final Context context) {
        // 网络操作，但开一个线程进行处理
        new Thread(new Runnable() {
            @Override
            public void run() {
                FtpClient ftpClient = new FtpClient();
                Log.e("TAG", "filePath:" + filePath);
                Log.e("TAG", "fileName:" + fileName);
                final String str = ftpClient.ftpUpload(filePath, fileName);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (str.equals("1")) {
                            Toast.makeText(context, "上传成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * 删除某一条记录及本地文件
     * @param position 读取本地存储的所有音频/视频文件时，待上传文件所在的编号.stringList.get(position).substring(0,23)是文件名
     */
    private void deleteFile(Integer position) {
        String filename = stringList.get(position).substring(0,23);
        if(isImageMode) {
            FileUtil.deleteSingleFile(TakePhotoActivity.PATH_IMAGES + filename, context);
        }
        else {
            FileUtil.deleteSingleFile(AudioRecoderUtils.MP3_PATH + filename, context);
        }
        Intent intent = new Intent(context, ReviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("isImageMode", isImageMode);
        intent.putExtras(bundle);
        context.startActivity(intent);
        System.exit(0);
    }
}

