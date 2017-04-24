package com.example.android.camera2basic.TCP;

import android.util.Log;

import com.example.android.camera2basic.CameraActivity;
import com.example.android.camera2basic.Quene;

import java.io.IOException;
import java.io.OutputStream;
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
    private OutputStream sendStream;
    private SendThread sendThread;

    public SendH264(){
        Log.d(TAG,"send h264s");
        this.quene = CameraActivity.quene;
//      sendQuene = quene;
        sendThread = new SendThread();
    }
    private boolean isSendingH264 = false;
    public void setSendH264Status(boolean value){
        isSendingH264 = value;
    }
    public void startSendH264(){
        sendThread.start();
    }
    public void connectToBox(){
        sendThread.connectToBox();
    }
    public void connectToPhone(){
        sendThread.connectToPhone();
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
        public synchronized void run() {
            super.run();
            try{
                send = new Socket("10.1.1.1",8888);
//                send = new Socket("10.105.36.224",18888);
                Log.d(TAG,"success");
//                send = new Socket("10.1.1.1",8888);//obu
                sendStream = send.getOutputStream();
            }catch (IOException e){
                Log.d(TAG,"worong");
            }

            while(true){
                Log.d(TAG, "发送队列"+quene.getH264SendQueue());
                if (!quene.getH264SendQueue().isEmpty()){
                    //
                    Log.d(TAG,"get one");
                    try{
                        //maybe wrong
                        byte[] tmp = (byte[])quene.getH264SendQueue().poll();
                        if (sendStream != null){
//                            sendStream.write(b);
                            sendStream.write(tmp);
                            sendStream.flush();
                        }
                    }catch (IOException e){}
                }
                try {
                    Thread.sleep(10);
                }catch (InterruptedException e){}
            }
        }
    }


}

