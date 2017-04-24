/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.camera2basic;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.android.camera2basic.Fragment.Camera2BasicFragment;
import com.example.android.camera2basic.Fragment.ShowFragment;
import com.example.android.camera2basic.TCP.RecvH264;
import com.example.android.camera2basic.TCP.SendH264;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class CameraActivity extends FragmentActivity{
    private final String TAG = "CameraActivity";

    private ShowFragment showFragment;
    private Camera2BasicFragment camera2BasicFragment;
    public static Quene quene;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //camera preview fragment
        camera2BasicFragment = Camera2BasicFragment.newInstance();
        setContentView(R.layout.activity_camera);
        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.cameraPreview, camera2BasicFragment)
                    .commit();
        }

        //show video fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        showFragment = new ShowFragment();
        fragmentManager.beginTransaction().add(R.id.showView,showFragment).commit();
        Log.d(TAG,"init codec");

        quene = new Quene();

        sendH264 = new SendH264();

        recvH264= new RecvH264();


        Button button = (Button)findViewById(R.id.recvH264V3);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"接收——按钮");
                startRecvH264();
            }
        });
        Button playBtn = (Button)findViewById(R.id.playH264V3);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"播放——按钮");
                startPlay();
            }
        });
        Button recordBtn = (Button)findViewById(R.id.record);
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"录制——按钮");
                startRecord();
            }
        });

        Button sendBtn = (Button)findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"发送——按钮");
                startSend();
            }
        });
    }

    private RecvH264 recvH264 = null;
    private boolean isRecvH264 = false;
    public void startRecvH264(){
        isRecvH264 = true;
        if (isRecvH264){
            recvH264.startRecvH264();
        }
    }

    //
    public void startPlay(){
        showFragment.startPlay();
    }

    //record
    public void startRecord(){
        camera2BasicFragment.startRecord();
    }

    //send h264
    private SendH264 sendH264;
    private boolean isSendH264 = false;
    public void startSend(){
        Log.d(TAG,"end start");
        isSendH264 = true;
        sendH264.startSendH264();
    }

    public Quene getQuene(){
        return this.quene;
    }
}







//    public class SendQueue{
//        //h264 data queue
//        private BlockingQueue<byte[]> H264SendQueue = new ArrayBlockingQueue<byte[]>(10000);
//        public BlockingQueue getH264SendQueue(){
//            return H264SendQueue;
//        }
//        public void offerSendH264Queue(byte[] b){
//            int n = b.length/1000;
//            for(int i = 0;i< n+1;i++){
//                int len = 1000;
//                if (i == n){
//                    len = b.length - i*1000;
//                }
//                if (len == 0)
//                    break;
//                byte[] tmp = new byte[len];
//                System.arraycopy(b,i*1000,tmp,0,len);
//                H264SendQueue.offer(tmp);
//                i++;
//
//            }
//        }
//    }




//    //h264 recv queue
//    private BlockingQueue<byte[]> H264RecvQueue = new ArrayBlockingQueue<byte[]>(10000);
//    public BlockingQueue getH264RecvQueue(){
//        return H264RecvQueue;
//    }
//    private byte[] currentBuff = new byte[102400];
//    private int currentBuffStart = 0;//valid data start
//    private int currentBuffEnd = 0;
////    int cnt = 0;
//
//    public byte[] getOneNalu(){
//        int n = getNextIndex();
//        if (n <= 0){
//            Log.d(TAG,"nulllll"+"   "+n);
////            Log.d(TAG,n+"");
//            return null;
//        }
////        Log.d(TAG,"get one"+n);
//        byte[] naluu = new byte[n-currentBuffStart];
//        Log.d(TAG,n+"--n");
//        Log.d(TAG,currentBuffStart+"");
//        System.arraycopy(currentBuff, currentBuffStart, naluu, 0, n-currentBuffStart);
//
//        //handle currentBuff
//        System.arraycopy(currentBuff, n , currentBuff, 0, currentBuff.length - n);
//
//        //set index
//        currentBuffStart = 0;
//        currentBuffEnd = currentBuffEnd - naluu.length;
//        return naluu;
//    }
//    //added by deonew
////    private int nextNaluHead = -1;
//    public int getNextIndex(){
//        int nextNaluHead;
//        nextNaluHead = getNextIndexOnce();
//
//        //currentBuff don't contain a nalu
//        //poll data
//        while(nextNaluHead == -1) {
//            if (getH264RecvQueue().isEmpty()){
//                Log.d(TAG,"queue empty");
//                break;
//            }else{
//                byte[] tmp = (byte[])getH264RecvQueue().poll();
//                System.arraycopy(tmp,0,currentBuff,currentBuffEnd,tmp.length);
//                currentBuffEnd = currentBuffEnd + tmp.length;
//                nextNaluHead = getNextIndexOnce();
//            }
////            cnt++;
////            Log.d(TAG,"poll"+cnt);
//        }
//        nextNaluHead = nextNaluHead - 3;
//        // currentBuffStart = nextNaluHead;
//        return nextNaluHead;
//    }
//
//    //get next 000000[01]
//    public int getNextIndexOnce(){
//        int nextIndex = -1;
//        byte[] naluHead = {0,0,0,1};
//        byte[] correctBuff = {0,1,2,0};
//        int i;
//        int index = 0;
//        for(i = currentBuffStart+1; i < currentBuffEnd;i++){
//            while (index > 0 && currentBuff[i] != naluHead[index]) {
//                index = correctBuff[index - 1];
//            }
//            if (currentBuff[i] == naluHead[index]) {
//                index++;
//                if (index == 4){
//                    nextIndex = i;//i = 00000001中的01
//                    break;
//                }
//            }
//        }
//        return nextIndex;
//    }





    //send
//    public void startSend(){
//        startSend();
//    }




