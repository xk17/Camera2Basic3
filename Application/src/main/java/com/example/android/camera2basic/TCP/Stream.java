package com.example.android.camera2basic.TCP;

import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.example.android.camera2basic.CameraActivity;
import com.example.android.camera2basic.Fragment.ShowFragment;
import com.example.android.camera2basic.Quene;
import com.example.android.camera2basic.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

/**
 * Created by xuke on 2017/5/27.
 */

public class Stream {
    private Socket socket;
    public Quene quene;
    private OutputStream sendStream;
    private InputStream recvStream;
//    private ArrayList<Stream> streams;
    private String TAG = "Stream";

    public Stream(Socket socket){
        this.socket = socket;
        this.quene = new Quene();

        try {
            this.sendStream = socket.getOutputStream();
            this.recvStream = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        this.streams = CameraActivity.streams;
        new SendThread().start();
        new RecvThread().start();
    }

    private class SendThread extends Thread{
        //get data frome queue
        @Override
        public void run() {
            super.run();
//            int number = 1;
            while(true) {
                if (isSend) {
//                    TCP发送
                    if (!quene.getH264SendQueue().isEmpty()) {
                        //
//                        Log.d(TAG, "get one");
                        try {
                            //maybe wrong
//                            byte[] numberB = intToByteArray(number);
                            byte[] tmp = (byte[]) quene.getH264SendQueue().poll();
//                            byte[] total = new byte[tmp.length+4];
//                            System.arraycopy(numberB, 0, total, 0, 4);
//                            System.arraycopy(tmp, 0, total, 4, tmp.length);
                            byte[] total = new byte[tmp.length];
                            System.arraycopy(tmp, 0, total, 0, tmp.length);
//                            Log.d(TAG, "发送队列长度"+total.length);
                            if (sendStream != null) {
//                            sendStream.write(tmp);
                                sendStream.write(total);
                                sendStream.flush();

                            }
//                            number += 1;
                        } catch (IOException e) {
                        }
                    }
//                    try {
//                        Thread.sleep(10);
//                    } catch (InterruptedException e) {
//                    }
                }
            }
        }

    }

    private class RecvThread extends Thread{
        @Override
        public void run() {
            super.run();
            try {
//            createFile();
                BlockingQueue<byte[]> H264RecvQueue =  quene.getH264RecvQueue();
                H264RecvQueue .clear();
                while(true){
                    while(isRecv){
//                        Log.d(TAG, "recv"+H264RecvQueue .size());
                        byte[] readByte = new byte[2000];
                        int n;
                        while((n =  recvStream.read(readByte))!=-1){
//                            Log.d(TAG,"receive");
//                            Log.d(TAG, "接收数据的长度"+n);
//                          将mac地址取出,n为读到的长度
//                            if (n>4){
//                            byte[] number = new byte[4];
//                            byte[] toOffer = new byte[n-4];
//                            System.arraycopy(readByte,0,number,0,4);
//                            System.arraycopy(readByte,4,toOffer,0,n-4);
                            byte[] toOffer = new byte[n];
                            System.arraycopy(readByte,0,toOffer,0,n);
                            H264RecvQueue .offer(toOffer);
//                            int num = byteArrayToInt(number);
//                            Log.d(TAG, "接收mac地址/包序号："+ number);
//                            }else {
//                                byte[] toOffer = new byte[n];
//                                System.arraycopy(readByte,0,toOffer,0,n);
//                            }
//                            outputStream.write(toOffer);
//                            Log.d(TAG,""+H264RecvQueue .size());
//                            try {
//                                Thread.sleep(5);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
                        }

                    }
                }
            }catch (IOException e){
                Log.d(TAG,"wrong");
            }
        }
    }

    //控制条件
    private boolean isRecv = true;
    private boolean isSend = true;


    //byte 数组与 int 的相互转换
    public static int byteArrayToInt(byte[] b) {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    public static byte[] intToByteArray(int a) {
        return new byte[]{
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }
}
