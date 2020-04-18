package com.sid.soundrecorderutils.util;

import android.content.Context;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FileUtil {
    /**
     * 遍历所有文件
     */
    private static String TAG = "FileUtil";
    public static Map<String, String> getFileName(final String fileAbsolutePath) {
        Map<String, String> map = new HashMap<>();
        Log.d(TAG, "getFileName: " + fileAbsolutePath);
        if(fileAbsolutePath == null) return map;
        File file = new File(fileAbsolutePath);
        File[] subFile = file.listFiles();
        if(subFile == null) return map;
        for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
            // 判断是否为文件夹
            if (!subFile[iFileLength].isDirectory()) {
                String filename = subFile[iFileLength].getName();
                map.put(String.valueOf(iFileLength),filename);
            }
        }
        return map;
    }

    public static File getFile(final String fileAbsolutePath) {
        Log.d(TAG, "getFile: " + fileAbsolutePath);
        if(fileAbsolutePath == null) return null;
        File file = new File(fileAbsolutePath);
        return file;
    }

    /**
     * 根据uri获取文件名
     *
     * @param context
     * @param fileUri
     * @return
     */
    public static String getFileRealNameFromUri(Context context, Uri fileUri) {
        if (context == null || fileUri == null) return null;
        DocumentFile documentFile = DocumentFile.fromSingleUri(context, fileUri);
        if (documentFile == null) return null;
        return documentFile.getName();
    }

    /** 删除单个文件
     * @param filePath 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteSingleFile(String filePath, Context context) {
        File file = new File(filePath);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                Log.e("--Method--", "Copy_Delete.deleteSingleFile: 删除单个文件" + filePath + "成功！");
                return true;
            } else {
                Toast.makeText(context, "删除单个文件" + filePath + "失败！", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            Toast.makeText(context, "删除单个文件失败：" + filePath + "不存在！", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
