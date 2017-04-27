package com.example.android.camera2basic.Fragment;

import android.content.Context;
import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.camera2basic.CameraActivity;
import com.example.android.camera2basic.Quene;
import com.example.android.camera2basic.R;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

/**
 * Created by xk on 4/23/17.
 */

public class ShowFragment extends Fragment {
    private final String TAG = "ShowFragment";

//    CameraActivity mainAC;
    private Quene quene;
    private BlockingQueue<byte[]> H264RecvQueue;
    public ShowFragment(){
        quene = CameraActivity.quene;
        H264RecvQueue  = quene.getH264RecvQueue();
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_video_show, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();

//        initMediaCodec();
//        new recvSocketThread().start();
//        new decodeH2Thread().start();
//        getActivity().findViewById(R.id.videoTextureView);


        initMediaCodec();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

//        mainAC = (CameraActivity) getActivity();
//        quene = CameraActivity.quene;
//        initMediaCodec();
    }

    private MediaCodec mPlayCodec;
    private int Video_Width = 500;
    private int Video_Height = 300;
    private int PlayFrameRate = 15;
    private Boolean isUsePpsAndSps = false;
    private SurfaceView mPlaySurface = null;
    private SurfaceHolder mPlaySurfaceHolder;
    public void initMediaCodec(){
//    public void initMediaCodec(SurfaceView s){

        CameraActivity v3 = (CameraActivity) getActivity();
        mPlaySurface = (SurfaceView) v3.findViewById(R.id.videoPlay);
        mPlaySurfaceHolder = mPlaySurface.getHolder();
        //回调函数来啦
        mPlaySurfaceHolder.addCallback(new SurfaceHolder.Callback(){
            @Override
            public void surfaceCreated(SurfaceHolder holder){
                try {
                    //通过多媒体格式名创建一个可用的解码器
                    mPlayCodec = MediaCodec.createDecoderByType("video/avc");
                    Log.d(TAG, "解码器配置成功");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //初始化编码器
                final MediaFormat mediaformat = MediaFormat.createVideoFormat("video/avc", Video_Width, Video_Height);

                //获取h264中的pps及sps数据
                if (isUsePpsAndSps) {
                    byte[] header_sps = {0, 0, 0, 1, 103, 66, 0, 42, (byte) 149, (byte) 168, 30, 0, (byte) 137, (byte) 249, 102, (byte) 224, 32, 32, 32, 64};
                    byte[] header_pps = {0, 0, 0, 1, 104, (byte) 206, 60, (byte) 128, 0, 0, 0, 1, 6, (byte) 229, 1, (byte) 151, (byte) 128};
                    mediaformat.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps));
                    mediaformat.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps));
                }
                mediaformat.setInteger(MediaFormat.KEY_FRAME_RATE, PlayFrameRate);
                //set output image format
                mediaformat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
                mPlayCodec.configure(mediaformat, holder.getSurface(), null, 0);
//                mPlayCodec.configure(mediaformat, null, null, 0);
                mPlayCodec.start();
            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {}
        });
    }
    private boolean isPlay = false;
    public void startPlay(){
        isPlay = true;
//        new decodeH2Thread().start();
        new Thread(new decodeH264Thread()).start();
    }
//    long pts = 0;
//    long generateIndex = 0;
//  //解码并显示
//    class decodeH2Thread extends Thread{
//        @Override
//        public void run() {
//            super.run();
//            while(true){
//                if (isPlay){
//                    MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
//                    long startMs = System.currentTimeMillis();
//                    long timeoutUs = 10000;
//
//                    int inIndex = mPlayCodec.dequeueInputBuffer(timeoutUs);
//                    Log.d(TAG, "输入buffer序号"+inIndex);
//                    if (inIndex >= 0) {
//                        pts = computePresentationTime(generateIndex);
//                        ByteBuffer byteBuffer = mPlayCodec.getInputBuffer(inIndex);
//
//                        byteBuffer.clear();
//                        byte[] b = quene.getOneNalu();
//                        if (b!=null){
//                            byteBuffer.put(b);
//                            Log.d(TAG, "将数据放入解码器"+b.length);
//                            mPlayCodec.queueInputBuffer(inIndex, 0, b.length, pts, 1);
////                            mPlayCodec.queueInputBuffer(inIndex, 0, b.length, 0, 0);
//
////                            generateIndex += 1;
//                        }else{
//                            Log.d(TAG, "获得数据单元为空");
//                            byte[] dummyFrame = new byte[]{0x00, 0x00, 0x01, 0x20};
//                            byteBuffer.put(dummyFrame);
//                            mPlayCodec.queueInputBuffer(inIndex, 0, dummyFrame.length, pts, 1);
//                            generateIndex += 1;
////                            mPlayCodec.queueInputBuffer(inIndex, 0, dummyFrame.length, 0, 0);
//
//                        }
//                    }
//
//
//                    int outIndex = mPlayCodec.dequeueOutputBuffer(info, timeoutUs);
//                    Log.d(TAG, "outIndex"+outIndex);
//                    if (outIndex >= 0) {
//                        ByteBuffer outputBuffer = mPlayCodec.getOutputBuffer(outIndex);
//                        outputBuffer.position(info.offset);
//                        outputBuffer.limit(info.offset + info.size);
//                        Log.d(TAG,"length"+info.offset + info.size);
//
//                        boolean doRender = (info.size != 0);
//                        mPlayCodec.releaseOutputBuffer(outIndex, doRender);
//                        try {
//                            Log.d(TAG, "sleep");
//                            Thread.sleep(10);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//
//                    } else {
//                        Log.d(TAG, "no output");
//                    }
//
//                }
//            }
//        }
//    }

    private long computePresentationTime(long frameIndex) {
        return 132 + frameIndex * 1000000/ 24;
    }

    private boolean mStopFlag = false;
    private class decodeH264Thread implements Runnable{
        @Override
        public void run() {

            try {
                Log.d(TAG, "进入解码线程");
                decodeLoop();
            } catch (Exception e) {
                Log.d(TAG, "decodeLoop error");
            }


        }
        //标记 camera旧api 已改

        private byte[] streamBuffer = null;
        private void decodeLoop(){

//            ByteBuffer[] inputBuffers = mPlayCodec.getInputBuffers();
            //解码后的数据，包含每一个buffer的元数据信息，例如偏差，在相关解码器中有效的数据大小


            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            long startMs = System.currentTimeMillis();
            long timeoutUs = 10000;

                while (true){
                    int inIndex = mPlayCodec.dequeueInputBuffer(timeoutUs);
                    if (inIndex >= 0) {
                        ByteBuffer byteBuffer = mPlayCodec.getInputBuffer(inIndex);
//                        ByteBuffer byteBuffer = inputBuffers[inIndex];
                        byteBuffer.clear();

                        //队列获取文件数据
                        byte[] b = getOneNalu();
                        if (b!=null){
                            try{
                                byteBuffer.put(b);
                                Log.d(TAG, "放入一桢数据成功");
                                Thread.sleep(30);
                            }catch (InterruptedException e){}
                        } else {
                            Log.d(TAG, "获得数据单元为空");
                            byte[] dummyFrame = new byte[]{0x00, 0x00, 0x01, 0x20};
                            byteBuffer.put(dummyFrame);
                            mPlayCodec.queueInputBuffer(inIndex, 0, dummyFrame.length, 0, 0);
                        Log.d(TAG, "放入一桢数据成功");
                        }


                        //在给指定Index的inputbuffer[]填充数据后，调用这个函数把数据传给解码器
//                        mPlayCodec.queueInputBuffer(inIndex, 0, nextFrameStart - startIndex, 0, 0);
                        mPlayCodec.queueInputBuffer(inIndex, 0, b.length, 0, 0);

                    } else {
                        continue;
                    }
                    int outIndex = mPlayCodec.dequeueOutputBuffer(info, timeoutUs);
                    if (outIndex >= 0) {
                        while (info.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        boolean doRender = (info.size != 0);
                        mPlayCodec.releaseOutputBuffer(outIndex, doRender);
                    } else {
                        Log.d(TAG, "获得数据单元为空");
                    }
                }
//                mStopFlag = true;
            }

        }


    private byte[] currentBuff = new byte[102400];
    private int currentBuffStart = 0;//valid data start
    private int currentBuffEnd = 0;
    int cnt = 0;

    public byte[] getOneNalu(){
        int n = getNextIndex();
        if (n <= 0){
            Log.d(TAG,"获得数据queue size"+ H264RecvQueue.size());
            return null;
        }
        Log.d(TAG,"get one"+n);
        byte[] naluu = new byte[n-currentBuffStart];
//        Log.d(TAG,n+"--n");
//        Log.d(TAG,currentBuffStart+"");
        System.arraycopy(currentBuff, currentBuffStart, naluu, 0, n-currentBuffStart);

        //handle currentBuff
        System.arraycopy(currentBuff, n , currentBuff, 0, currentBuff.length - n);

        //set index
        currentBuffStart = 0;
        currentBuffEnd = currentBuffEnd - naluu.length;
        return naluu;
    }
    //added by deonew
    private int nextNaluHead = -1;
    public int getNextIndex(){
        //int nextNaluHead;
        nextNaluHead = getNextIndexOnce();

        //currentBuff don't contain a nalu
        //poll data

        while(nextNaluHead == -1) {
            if (H264RecvQueue.isEmpty()){
                Log.d(TAG,"queue empty");
                break;}
//            }else{
            byte[] tmp =H264RecvQueue.poll();
            System.arraycopy(tmp,0,currentBuff,currentBuffEnd,tmp.length);
            currentBuffEnd = currentBuffEnd + tmp.length;
            nextNaluHead = getNextIndexOnce();
            // }
            cnt++;
//            Log.d(TAG,"poll"+cnt);
        }
        nextNaluHead = nextNaluHead - 3;
        // currentBuffStart = nextNaluHead;
        return nextNaluHead;
    }

    //get next 000000[01]
    public int getNextIndexOnce(){
        int nextIndex = -1;
        byte[] naluHead = {0,0,0,1};
        byte[] correctBuff = {0,1,2,0};
        int i=0;
        int index = 0;
        for(i = currentBuffStart+1; i < currentBuffEnd;i++){
            while (index > 0 && currentBuff[i] != naluHead[index]) {
                index = correctBuff[index - 1];
            }
            if (currentBuff[i] == naluHead[index]) {
                index++;
                if (index == 4){
                    nextIndex = i;//i = 00000001中的01
                    break;
                }
            }
        }
        return nextIndex;
    }

}
