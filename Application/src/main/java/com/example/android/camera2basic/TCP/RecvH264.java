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
//                recvSocket = new Socket("10.105.36.224",18888);
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
                        Log.d(TAG, "ins"+ins.read(readByte));
                        while((n = ins.read(readByte))!=-1){
                            Log.d(TAG,"receive");
                            byte[] toOffer = new byte[n];
                            System.arraycopy(readByte,0,toOffer,0,n);
                            quene.getH264RecvQueue().offer(toOffer);
                            Log.d(TAG,""+quene.getH264RecvQueue().size());
                        }
                    }
                }
            }catch (IOException e){
                Log.d(TAG,"wrong");
            }
        }
    }
}
