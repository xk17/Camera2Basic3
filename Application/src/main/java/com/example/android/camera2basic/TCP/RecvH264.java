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
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/**
 * Created by XK on 4/23/17.
 */


public class RecvH264 {
    private final  String TAG = "RecvH264V3";
    private Quene quene;
    private Socket recvSocket;
//    private CameraActivity mVideoAC;
    public RecvH264(Socket socket){
        this.recvSocket = socket;
        this.quene = CameraActivity.quene;
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

    public void startRecvH264(){

        //recv thread
        isRecv = true;
        if (isRecv){
            new recvSocketThread().start();
        }

    }

    private boolean isRecv = false;
//    private Socket recvSocket = null;
    private class recvSocketThread extends Thread{
        @Override
        public void run() {
            super.run();
            try {
//                recvSocket = new Socket("10.105.36.224",18888);
//                recvSocket = new Socket("192.168.1.105",18888);
                createFile();
                BlockingQueue<byte[]> H264RecvQueue =  quene.getH264RecvQueue();
//                recvSocket = new Socket("10.1.1.1",8888);
                Log.d(TAG,"okay");
                InputStream ins = recvSocket.getInputStream();
                H264RecvQueue .clear();
                while(true){
                    while(isRecv){
                        Log.d(TAG, "recv"+H264RecvQueue .size());
                        byte[] readByte = new byte[2000];
                        int n;
                        Log.d(TAG, "ins"+ins.read(readByte));
                        while((n = ins.read(readByte))!=-1){
                            Log.d(TAG,"receive");
                            byte[] number = new byte[4];
                            byte[] toOffer = new byte[n-4];
                            System.arraycopy(readByte,0,number,0,4);
                            System.arraycopy(readByte,4,toOffer,0,n-4);
//                            byte[] toOffer = new byte[n];
//                            System.arraycopy(readByte,0,toOffer,0,n);
                            H264RecvQueue .offer(toOffer);
                            int num = byteArrayToInt(number);
                            Log.d(TAG, "接收序号："+ num);
                            outputStream.write(toOffer);
                            Log.d(TAG,""+H264RecvQueue .size());

                        }

                    }
                }
            }catch (IOException e){
                Log.d(TAG,"wrong");
            }
        }
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
