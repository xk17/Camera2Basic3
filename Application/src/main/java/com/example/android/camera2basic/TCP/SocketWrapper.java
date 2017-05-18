package com.example.android.camera2basic.TCP;

import android.os.Environment;
import android.util.Log;

import com.example.android.camera2basic.CameraActivity;
import com.example.android.camera2basic.Quene;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;

/**
 * Created by xuke on 2017/4/26.
 */

public class SocketWrapper {
    private String TAG = "socket";
    private Socket clientSocket;
    private OutputStream sendStream;
    private InputStream recvStream;
    private Quene quene;
    private boolean client;
    private SendThread sendThread;
    private RecvThread recvThread;

//    InetAddress inetAddr;
//    DatagramSocket clientSocket;
//    DatagramPacket receive;
//    DatagramSocket serverSocket;

    public SocketWrapper(Boolean client){
        this.client = client;
        this.quene = CameraActivity.quene;
        new initSocket().start();
        this.sendThread = new SendThread();
        this.recvThread = new RecvThread();
    }


    //控制条件
    private boolean isRecv = false;
    private boolean isSend = false;

    public void startSend(){
        isSend = true;
        if (isSend){
            sendThread.start();
        }
    }
    public void startRecv(){
        //recv thread
        isRecv = true;
        if (isRecv){
            recvThread.start();
        }
    }

    private class initSocket extends Thread{
        @Override
        public void run(){
            super.run();
//            TCP连接
            try {
                if (client) {
                    clientSocket = new Socket("10.105.38.31", 18888);
//                    clientSocket = new Socket("10.1.1.1", 8888);

                    Log.d(TAG, "客户端连接成功");
                }else {
                    ServerSocket serverSocket = new ServerSocket(18888);
                    Log.d(TAG, "服务器开始监听");
//                    while (true) {
                        clientSocket = serverSocket.accept();
                        Log.d(TAG, "服务器端收到请求");
//                    }
                }
                sendStream = clientSocket.getOutputStream();
                Log.d(TAG, "sendStream为空" + (sendStream == null));
                recvStream = clientSocket.getInputStream();
//                Log.d(TAG, "sendStream为空" + (sendStream == null));
                Log.d(TAG, "TCP请求连接");

                } catch(IOException e){
                    e.printStackTrace();
                }
//              UDP连接
//            if (client){
//                try {
//                    inetAddr = InetAddress.getByName("255.255.255.255");
//                    clientSocket = new DatagramSocket();
//                    Log.d(TAG, "UDP客户端广播发送");
//                } catch (UnknownHostException e) {
//                    e.printStackTrace();
//                } catch (SocketException e) {
//                    e.printStackTrace();
//                }
//
//
//            }else {
//                try {
//                    serverSocket = new DatagramSocket(8888);
//                    Log.d(TAG, "UDP服务端广播接收");
//                } catch (SocketException e) {
//                    e.printStackTrace();
//                }
//            }
        }
    }

    private class SendThread extends Thread{
        //get data frome queue
        @Override
        public void run() {
            super.run();
            int number = 1;
            while(true) {
                if (isSend) {
//                    TCP发送
////                    Log.d(TAG, "发送队列" + quene.getH264SendQueue());
                    if (!quene.getH264SendQueue().isEmpty()) {
                        //
                        Log.d(TAG, "get one");
                        try {
                            //maybe wrong
                            byte[] numberB = intToByteArray(number);
                            byte[] tmp = (byte[]) quene.getH264SendQueue().poll();
//                            byte[] total = new byte[tmp.length+4];
//                            System.arraycopy(numberB, 0, total, 0, 4);
//                            System.arraycopy(tmp, 0, total, 4, tmp.length);
                            byte[] total = new byte[tmp.length];
                            System.arraycopy(tmp, 0, total, 0, tmp.length);
                            Log.d(TAG, "发送队列长度"+total.length);
                            Log.d(TAG, "sendStream为空" + (sendStream == null));
                            if (sendStream != null) {
//                            sendStream.write(tmp);
                                sendStream.write(total);
//                                Log.d(TAG, "包序号：" + number);
                                sendStream.flush();

                            }
                            number += 1;
                        } catch (IOException e) {
                        }
                    }
//                    try {
//                        Thread.sleep(10);
//                    } catch (InterruptedException e) {
//                    }
//                    UDP发送 xk 2017／5／12 13：00
//                    if (!quene.getH264SendQueue().isEmpty()) {
//                        Log.d(TAG, "get one");
//                        byte[] tmp = (byte[]) quene.getH264SendQueue().poll();
//                        byte[] total = new byte[tmp.length];
//                        System.arraycopy(tmp, 0, total, 0, tmp.length);
//                        DatagramPacket sendPack = new DatagramPacket(total, total.length, inetAddr,
//                                8888);
//                        try {
//                            clientSocket.send(sendPack);
//                            Log.d(TAG, "UDP发送成功");
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//
//                    }
//                    clientSocket.close();

                }
            }
        }

    }

    private class RecvThread extends Thread{
        @Override
        public void run() {
            super.run();
            try {
                createFile();
                BlockingQueue<byte[]> H264RecvQueue =  quene.getH264RecvQueue();
                H264RecvQueue .clear();
                while(true){
                    while(isRecv){
                        Log.d(TAG, "recv"+H264RecvQueue .size());
                        byte[] readByte = new byte[2000];
                        int n;
                        while((n =  recvStream.read(readByte))!=-1){
                            Log.d(TAG,"receive");
                            Log.d(TAG, "接收数据的长度"+n);
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
                            Log.d(TAG,""+H264RecvQueue .size());
//                            try {
//                                Thread.sleep(5);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
                        }
//                        UDP接收 xk 2017/5/12 13:00
//                        receive = new DatagramPacket(new byte[1024], 1024);
//                        Log.d(TAG, "接收到数据的长度"+receive.getLength());
//                        serverSocket.receive(receive);
////                        byte[] dataByte = new byte[2000];
////                        System.arraycopy(receive.getData(),0,dataByte,0,receive.getLength());
//
//                        H264RecvQueue .offer(receive.getData());
                    }
                }
            }catch (IOException e){
                Log.d(TAG,"wrong");
            }
        }
    }

    private FileOutputStream outputStream;
    private static String path = Environment.getExternalStorageDirectory() + "/carxk2.h264";
    public void createFile() throws FileNotFoundException {
        File file = new File(path);
        if (file.exists()){
            file.delete();
        }

        Log.d(TAG, "init file");
        outputStream = new FileOutputStream(path, true);
    }


    //byte 数组与 int 的相互转换
    public static int byteArrayToInt(byte[] b) {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
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
