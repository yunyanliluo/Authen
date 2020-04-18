package com.sid.soundrecorderutils.tcp;
import com.sid.soundrecorderutils.util.LogUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ReceiveData implements Runnable {
    private Socket socket = null;
    private byte[] buffer = new byte[1024];
    private int bytes;
    InputStream mmInStream = null;

    public ReceiveData(Socket mSocket) {
        this.socket = mSocket;
    }

    @Override
    public void run() {
        try {
            mmInStream = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                if ((bytes = mmInStream.read(buffer)) > 0) {
                    byte[] buf_data = new byte[bytes];
                    for (int i = 0; i < bytes; i++) {
                        buf_data[i] = buffer[i];
                    }
                    String msg = new String(buf_data);
                    LogUtils.e("TAG", "来自服务端的数据:" + msg);
                }
            } catch (IOException e) {
                try {
                    mmInStream.close();
                    LogUtils.e("TAG", "读取出现异常...");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                break;
            }
        }
    }
}