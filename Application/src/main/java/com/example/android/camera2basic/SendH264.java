package com.example.android.camera2basic;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by xuke on 2017/4/19.
 */

public class SendH264 {
    private String TAG = "TCP";
    private Camera2BasicFragment mainAC;
    private Socket send;
    private OutputStream sendStream;
    private SendThread sendThread;
    public void startSendH264(){
        sendThread.start();
    }

    public SendH264(Camera2BasicFragment v){
        Log.d(TAG,"建立TCP连接...");
        this.mainAC = v;
        sendThread = new SendThread();
    }

    //get data frome queue
    private class SendThread extends Thread{
        @Override
        public void run() {
            super.run();
            try{
                send = new Socket("10.105.36.264",18888);
                Log.d(TAG,"TCP连接成功！");
                //sendStream = send.getOutputStream();
            }catch (IOException e){
                Log.d(TAG,"TCP连接失败！");
            }

//            while(true){
//                if (!mainAC.getH264SendQueue().isEmpty()){
//                    Log.d(TAG,"从H264队列中取出NALU...");
//                    try{
//                        //maybe wrong
//                        byte[] tmp = (byte[])mainAC.getH264SendQueue().poll();
//                        if (sendStream != null){
//                            sendStream.write(tmp);
//                            sendStream.flush();
//                        }
//                    }catch (IOException e){}
//
//                }
//                try {
//                    Thread.sleep(10);
//                }catch (InterruptedException e){}
//            }
        }
    }
}
