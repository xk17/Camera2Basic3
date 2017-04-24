package com.example.android.camera2basic.TCP;

import android.util.Log;

import com.example.android.camera2basic.CameraActivity;
import com.example.android.camera2basic.Quene;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * Created by XK on 4/23/17.
 */


public class RecvH264 {
    private final  String TAG = "RecvH264V3";
    private Quene quene;
//    private CameraActivity mVideoAC;
    public RecvH264(){
        this.quene = CameraActivity.quene;
    }
    public void startRecvH264(){

        //recv thread
        isRecv = true;
        if (isRecv){
            new recvSocketThread().start();
        }

    }

    private boolean isRecv = false;
    private Socket recvSocket = null;
    private class recvSocketThread extends Thread{
        @Override
        public void run() {
            super.run();
            try {
//                recvSocket = new Socket("10.105.39.244",18888);
//                recvSocket = new Socket("192.168.1.105",18888);
                recvSocket = new Socket("10.1.1.1",8888);
                Log.d(TAG,"okay");
                InputStream ins = recvSocket.getInputStream();

                quene.getH264RecvQueue().clear();
                while(true){
                    while(isRecv){
                        Log.d(TAG, "recv"+quene.getH264RecvQueue().size());
                        byte[] readByte = new byte[2000];
                        int n;
                        while((n = ins.read(readByte))!=-1){
                            Log.d(TAG,"receive");
                            byte[] number = new byte[4];
                            byte[] toOffer = new byte[n-4];
                            System.arraycopy(readByte,0,number,0,4);
                            System.arraycopy(readByte,4,toOffer,0,n-4);
                            quene.getH264RecvQueue().offer(toOffer);
                            int num = byteArrayToInt(number);
                            Log.d(TAG, "接收序号："+ num);
                            Log.d(TAG,""+quene.getH264RecvQueue().size());
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
