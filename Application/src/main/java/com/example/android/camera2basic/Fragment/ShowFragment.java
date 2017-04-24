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

/**
 * Created by xk on 4/23/17.
 */

public class ShowFragment extends Fragment {
    private final String TAG = "ShowFragment";

//    CameraActivity mainAC;
    private Quene quene;
    public ShowFragment(){
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

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

//        mainAC = (CameraActivity) getActivity();
        quene = CameraActivity.quene;
        initMediaCodec();
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
        new decodeH2Thread().start();
    }
    long pts = 0;
    long generateIndex = 0;
//  解码并显示
    class decodeH2Thread extends Thread{
        @Override
        public void run() {
            super.run();
            while(true){
                if (isPlay){
                    MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                    long startMs = System.currentTimeMillis();
                    long timeoutUs = 10000;

                    int inIndex = mPlayCodec.dequeueInputBuffer(timeoutUs);
                    if (inIndex >= 0) {
                        pts = computePresentationTime(generateIndex);
                        ByteBuffer byteBuffer = mPlayCodec.getInputBuffer(inIndex);

                        byteBuffer.clear();
                        byte[] b = quene.getOneNalu();
                        if (b!=null){
                            byteBuffer.put(b);
                            Log.d(TAG, "解码器输入数据");
                            mPlayCodec.queueInputBuffer(inIndex, 0, b.length, 100, 0);
//                            mPlayCodec.queueInputBuffer(inIndex, 0, 1, pts, 0);
                            generateIndex += 1;
                        }else{
                            byte[] dummyFrame = new byte[]{0x00, 0x00, 0x01, 0x20};
                            byteBuffer.put(dummyFrame);
                            mPlayCodec.queueInputBuffer(inIndex, 0, dummyFrame.length, 100, 0);
                            generateIndex += 1;
                        }
                    }

                    int outIndex = mPlayCodec.dequeueOutputBuffer(info, timeoutUs);
//                    if (outIndex<0){
//                        Log.d(TAG,outIndex+"");//-1
//                        continue;
//                    }
                    Log.d(TAG, "outIndex"+outIndex);
                    if (outIndex >= 0) {
//                        ByteBuffer outputBuffer = mPlayCodec.getOutputBuffer(outIndex);
//                        outputBuffer.position(info.offset);
//                        outputBuffer.limit(info.offset + info.size);
//                        Log.d(TAG,"length"+info.offset + info.size);
//                        Log.d(TAG, "输出数据："+info.toString());

                        boolean doRender = (info.size != 0);
                        mPlayCodec.releaseOutputBuffer(outIndex, doRender);
                        Log.d(TAG, "no output");
                        try {
                            Log.d(TAG, "sleep");
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    } else {

                    }
                }
            }
        }
    }

    private long computePresentationTime(long frameIndex) {
        return 132 + frameIndex * 1000000/ 24;
    }
}
