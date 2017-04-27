package com.example.android.camera2basic.TCP;

import android.util.Log;

import com.example.android.camera2basic.CameraActivity;
import com.example.android.camera2basic.Quene;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/**
 * Created by deonew on 4/13/17.
 */

public class SendH264 {
    private final String TAG = "SENDH264";
//    private CameraActivity mainAC;
    private Quene quene;
    private Socket send;
    private Socket s;
    private ServerSocket server;
    private OutputStream sendStream;
    private SendThread sendThread;

    public SendH264(){
        Log.d(TAG,"send h264s");
        this.quene = CameraActivity.quene;
//      sendQuene = quene;
        sendThread = new SendThread();
        sendThread.start();
    }
    private boolean isSendingH264 = false;
    public void setSendH264Status(boolean value){
        isSendingH264 = value;
    }
    public void startSendH264(){
        startSend();
//        sendThread.start();
    }
    public void connectToBox(){
        sendThread.connectToBox();
    }
    public void connectToPhone(){
        sendThread.connectToPhone();
    }
    public Socket getSocket(){
        return send;
    }
    public Socket getServerSocket(){
        return s;
    }
    private boolean isSend = false;
    private void startSend(){
        isSend = true;
    }
    //get data frome queue
    private class SendThread extends Thread{
        public void connectToBox(){
            try{
                send = new Socket("10.1.1.1",8888);
                Log.d("qqqqqqqqqqq","success");
//                send = new Socket("10.1.1.1",8888);//obu
                sendStream = send.getOutputStream();
            }catch (IOException e){
                Log.d("qqqqqqqqqqqqqqqqqqqqqqq","worong");
            }
        }
        public void connectToPhone(){
            try{
                send = new Socket("10.105.38.183",8888);
                Log.d("qqqqqqqqqqq","success");
//                send = new Socket("10.1.1.1",8888);//obu
                sendStream = send.getOutputStream();
            }catch (IOException e){
                Log.d("qqqqqqqqqqqqqqqqqqqqqqq","worong");
            }
        }

        @Override
        public void run() {
            super.run();
//            try{
//                send = new Socket("192.168.1.167",18888);
//                Log.d(TAG, "TCP请求连接");
////                send = new Socket("10.1.1.1",8888);//obu
//                sendStream = send.getOutputStream();
//            }catch (IOException e){
//                Log.d(TAG,"wrong");
//            }

            try {
                Log.d(TAG, "开始监听");
                server = new ServerSocket(18888);
                boolean isAccept = true;
                while (isAccept){
                    s = server.accept();
                    Log.d(TAG,"收到请求");
//                  server端的输出
                    sendStream = s.getOutputStream();
                    isAccept = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            int number = 1;

            while(true) {
                if (isSend) {
                    Log.d(TAG, "发送队列" + quene.getH264SendQueue());
                    if (!quene.getH264SendQueue().isEmpty()) {
                        //
                        Log.d(TAG, "get one");
                        try {
                            //maybe wrong
                            byte[] tmp = (byte[]) quene.getH264SendQueue().poll();
                            byte[] total = new byte[tmp.length + 4];
                            System.arraycopy(intToByteArray(number), 0, total, 0, 4);
                            System.arraycopy(tmp, 0, total, 4, tmp.length);
                            if (sendStream != null) {
//                            sendStream.write(b);
//                            sendStream.write(tmp);
                                sendStream.write(total);
                                Log.d(TAG, "包序号：" + number);
                                sendStream.flush();
                                number += 1;
                            }
                        } catch (IOException e) {
                        }
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }

    }
    public static byte[] intToByteArray(int a) {
        return new byte[] {
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }

}

