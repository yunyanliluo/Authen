package com.sid.soundrecorderutils.tcp;

import com.sid.soundrecorderutils.util.LogUtils;

import java.io.PrintWriter;
import java.net.Socket;

public class SendHeartBeat implements Runnable {
    private String TAG = "SendHeartBeat";
    private String sendData;

    public SendHeartBeat(String data) {
        this.sendData = data;
    }

    private PrintWriter printWriter = null;

    public void run() {
        while (true) {
            Socket socket = SocketManager.getClientSocket();
            int code = sendHeartBeatPackage(socket, sendData);
            if (code != 1) {
                LogUtils.e(TAG, "发送失败");
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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