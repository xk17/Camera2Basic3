package com.example.android.camera2basic;

import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by xuke on 2017/4/24.
 */

public class Quene {
    private BlockingQueue<byte[]> H264SendQueue;
    private BlockingQueue<byte[]> H264RecvQueue;

    public Quene(){
        H264SendQueue = new ArrayBlockingQueue<byte[]>(10000);
        H264RecvQueue = new ArrayBlockingQueue<byte[]>(10000);
    }
    private String TAG = "Quene";
    //h264 send queue
    public
    BlockingQueue<byte[]> getH264SendQueue(){
        return H264SendQueue;
    }
    //h264 recv queue
    public synchronized BlockingQueue<byte[]> getH264RecvQueue(){
        return H264RecvQueue;
    }
    int totalSendcnt = 0;
//    将编码出的数据 每1000byte作为发送队列中的一个单元，即拆分成 i*1000 + b.length%1000
    public void offerSendH264Queue(byte[] b){
        int m = b.length%1000;
        int n = b.length/1000;
        if (m !=0){
            n++;
        }
        for(int i = 0;i< n;i++){
            int len = 1000;
            if (i == n - 1 ){
                len = b.length - i*1000;
            }
            if (len == 0)
                break;
            byte[] tmp = new byte[len];
            System.arraycopy(b,i*1000,tmp,0,len);
            H264SendQueue.offer(tmp);
            totalSendcnt++;
        }
    }

//    private byte[] currentBuff = new byte[102400];
//    private int currentBuffStart = 0;//valid data start
//    private int currentBuffEnd = 0;
//    int cnt = 0;
//
//    public byte[] getOneNalu(){
//        int n = getNextIndex();
//        if (n <= 0){
//            Log.d(TAG,"nulllll"+"   "+n);
//            Log.d(TAG,"queue size"+ H264RecvQueue.size());
////            Log.d(TAG,n+"");
//            return null;
//        }
//        Log.d(TAG,"get one"+n);
//        byte[] naluu = new byte[n-currentBuffStart];
////        Log.d(TAG,n+"--n");
////        Log.d(TAG,currentBuffStart+"");
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
//    private int nextNaluHead = -1;
//    public int getNextIndex(){
//        //int nextNaluHead;
//        nextNaluHead = getNextIndexOnce();
//
//        //currentBuff don't contain a nalu
//        //poll data
//        while(nextNaluHead == -1) {
//            if (H264RecvQueue.isEmpty()){
//                Log.d(TAG,"queue empty");
//                break;}
////            }else{
//                byte[] tmp =H264RecvQueue.poll();
//                System.arraycopy(tmp,0,currentBuff,currentBuffEnd,tmp.length);
//                currentBuffEnd = currentBuffEnd + tmp.length;
//                nextNaluHead = getNextIndexOnce();
//           // }
//            cnt++;
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
//        int i=0;
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
}
