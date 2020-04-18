package com.sid.soundrecorderutils.ftp;

import android.util.Log;

import com.sid.soundrecorderutils.util.LogUtils;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;

public class FtpClient {

    private String mUrl = "139.9.140.216";       //ftp服务器地址 如： 192.168.1.110
    private String mPort = "21";                 //端口如 ： 21
    private String mName = "authenbase";         //登录名
    private String mPassword = "1@mb3@ut1ful";   //密码
    private String mFtpPath = "~";               //上传到ftp服务器的路径

    /**
     * 通过ftp上传文件
     *
     * @param fileNamePath 要上传的文件路径
     * @param fileName     要上传的文件名
     * @return
     */
    public String ftpUpload(String fileNamePath, String fileName) {
        FTPClient ftpClient = new FTPClient();
        FileInputStream fis = null;
        String returnMessage = "0";
        try {
            ftpClient.connect(mUrl, Integer.parseInt(mPort));
            boolean loginResult = ftpClient.login(mName, mPassword);
            int returnCode = ftpClient.getReplyCode();
            if (loginResult && FTPReply.isPositiveCompletion(returnCode)) {// 如果登录成功
                ftpClient.makeDirectory(mFtpPath);
                // 设置上传目录
                ftpClient.changeWorkingDirectory(mFtpPath);
                LogUtils.e("TAG", "当前文件路径:" + fileNamePath);
                Log.e("TAG", "当前文件大小:" + new File(fileNamePath).length());
                ftpClient.setBufferSize((int) new File(fileNamePath).length());
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();
                fis = new FileInputStream(fileNamePath);
                ftpClient.storeFile(fileName, fis);
                returnMessage = "1";   //上传成功
            } else {// 如果登录失败
                returnMessage = "0";
                Log.e("TAG", "登录失败");
                returnMessage = "登录失败";
            }
        } catch (SocketException e) {
            LogUtils.e("TAG", "网络异常..." + e.toString());
            returnMessage = "网络异常";
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("TAG", "文件损坏或不存在:" + e.toString());
            returnMessage = "文件损坏或不存在";
        } finally {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("TAG", "关闭FTP连接发生异常");
                returnMessage = "关闭FTP连接发生异常";
            }
        }
        return returnMessage;
    }
}