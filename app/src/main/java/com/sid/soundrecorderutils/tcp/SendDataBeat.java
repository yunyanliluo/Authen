package com.sid.soundrecorderutils.tcp;

import com.sid.soundrecorderutils.util.LogUtils;

import java.io.PrintWriter;
import java.net.Socket;

public class SendDataBeat implements Runnable {
    private String TAG = "SendDataBeat";
    private String sendData;

    public SendDataBeat(String data) {
        this.sendData = data;
    }

    private PrintWriter printWriter = null;

    public void run() {
        Socket socket = SocketManager.getClientSocket();
        int code = sendHeartBeatPackage(socket, sendData);//数据包
        if (code != 1) {
            LogUtils.e(TAG, "发送失败");
        }
    }

    /**
     * 发送心跳包到服务端
     */
    public int sendHeartBeatPackage(Socket socket1, String msg) {
        int returnCode = 0;
        if (socket1.isClosed() || socket1 == null) {
            returnCode = 0;
        }
        try {
            printWriter = new PrintWriter(socket1.getOutputStream());
            printWriter.println(msg);
            printWriter.flush();
            socket1.getOutputStream().flush();
            returnCode = 1;
        } catch (Exception e) {
            returnCode = 0;
        }
        return returnCode;
    }
}