package com.sid.soundrecorderutils.tcp;

import com.sid.soundrecorderutils.manger.ThreadPoolManager;
import com.sid.soundrecorderutils.util.LogUtils;

import java.io.IOException;
import java.net.Socket;

public class SocketManager {

    private static final String TAG = "SocketManager";

    /**
     * 服务端IP 139.217.20.92
     */
    public static Socket clientSocket = null;
    private int PORT = 9988;     //端口号
    private String IP = "139.217.20.92";

    /**
     * 端口
     */

    public static SocketManager instance = new SocketManager();

    private SocketManager() {
        clientSocket = connect();
    }

    public static SocketManager getInstance() {
        return instance;
    }

    public static Socket getClientSocket() {
        return clientSocket;
    }

    public static void setClientSocket(Socket socket) {
        clientSocket = socket;
    }

    /**
     * 获取当前socket实例
     */
    public Socket connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    clientSocket = new Socket(IP, PORT);
                    setClientSocket(clientSocket);
                    readSocketData(clientSocket);
                    //sendHeartBeat(clientSocket);
                    LogUtils.e("TAG", "连接成功...");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return clientSocket;
    }

    /**
     * 发送心跳包
     */
    public static void sendHeartBeat(String data) {
        ThreadPoolManager.getSingleInstance().execute(new SendHeartBeat(data));
    }

    /**
     * 发送数据包
     */
    public static void sendData(String data) {
        ThreadPoolManager.getSingleInstance().execute(new SendDataBeat(data));
    }

    /**
     * 接收服务端数据
     */
    public void readSocketData(Socket socket) {
        ThreadPoolManager.getSingleInstance().execute(new ReceiveData(socket));
    }

    /**
     * 断开连接
     */
    public void disConnect() throws IOException {
        if (clientSocket != null) {
            clientSocket.close();
            clientSocket = null;
        }
    }

    /**
     * 释放单例, 及其所引用的资源
     */
    public void release() throws IOException {
        if (instance != null) {
            if (clientSocket != null) {
                clientSocket.close();
                clientSocket = null;
            }
            instance = null;
        }
    }
}