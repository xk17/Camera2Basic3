package com.example.android.camera2basic;

import android.annotation.SuppressLint;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by xuke on 2017/4/20.
 */

public class EncoderH264 {
    private final static String TAG = "MeidaCodec";

    private int TIMEOUT_USEC = 12000;


    private MediaCodec mediaCodec;
    int m_width;
    int m_height;
    int m_framerate;
    MediaFormat mOutputFormat; // member variable
    private byte[] data;
    private Image m_image;

    public byte[] HeadInfo;

    public EncoderH264(int imageW, int imageH,int framerate){
        m_width = imageW;
        m_height = imageH;
        Log.d(TAG, "输入编码器数据宽" + imageW + "高" +imageH);
        m_framerate = framerate;

        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", imageW, imageH);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, imageW * imageH * 3);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        try {
            mediaCodec = MediaCodec.createEncoderByType("video/avc");
        } catch (IOException e) {
        }

        mediaCodec.configure(mediaFormat,null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mOutputFormat = mediaCodec.getOutputFormat();
        mediaCodec.start();
        Log.d(TAG,"encoder config ok");
    }

    long pts = 0;
    long generateIndex = 0;

    public void code(Image image) {
        //long startMs = System.currentTimeMillis();

        YUV_888To420(image);

        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
        ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();


        ////获取可用的inputBuffer -1代表一直等待，0表示不等待 建议-1,避免丢帧
        int inputBufferId = mediaCodec.dequeueInputBuffer(10000);
        if (inputBufferId >= 0) {
//            ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferId);
            pts = computePresentationTime(generateIndex);
            ByteBuffer inputBuffer = inputBuffers[inputBufferId];
            // fill inputBuffer with valid data
            inputBuffer.put(data);
            Log.d(TAG, "放入的data" + data);
            mediaCodec.queueInputBuffer(inputBufferId, 0, data.length, pts, 0);
            generateIndex += 1;
            Log.d(TAG, "放入数据成功");
        }
        //执行上面的操作后就把待编解码的数据存入了输入缓冲区，然后下一步就是操作然后把编解码的数据存入输出缓冲区
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

        int outputBufferId = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
        Log.d(TAG, "outputBufferId "+outputBufferId);
        switch (outputBufferId) {
            case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED://当buffer变化时，client必须重新指向新的buffer
                Log.d(TAG, ">> output buffer changed ");
                outputBuffers = mediaCodec.getOutputBuffers();
                break;
            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED://当buffer的封装格式变化,须指向新的buffer格式
                Log.d(TAG, ">> buffer的封装格式变化output buffer changed ");
                break;
            case MediaCodec.INFO_TRY_AGAIN_LATER://当dequeueOutputBuffer超时,会到达此case
                Log.d(TAG, ">> dequeueOutputBuffer timeout超时 ");
                break;
            default:
//                ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputBufferId);
                ByteBuffer outputBuffer = outputBuffers[outputBufferId];
                MediaFormat bufferFormat = mediaCodec.getOutputFormat(outputBufferId); // option A
                // bufferFormat is identical to outputFormat
                // outputBuffer is ready to be processed or rendered.
                //outputBuffer.slice();
                byte[] outData = new byte[bufferInfo.size];
                outputBuffer.get(outData);
                //deal key frame v3.0
                if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {
                    //startSendH264 frame
                    HeadInfo = new byte[outData.length];
                    HeadInfo = outData;
                    Log.d(TAG, "head");
                } else if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME) {
                    //key frame
                    byte[] key = new byte[outData.length + HeadInfo.length];
                    //param: src srcpos dec decpos length
                    System.arraycopy(HeadInfo, 0, key, 0, HeadInfo.length);
                    System.arraycopy(outData, 0, key, HeadInfo.length, outData.length);
                    //version 1.0
                    //write key frame to h264
                    try {
                        outputStream.write(key, 0, key.length);
                    } catch (IOException e) {
                    }
                    Log.d(TAG, "key");
                    //put key frame to queue
                } else if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                    //end frame
                    Log.d(TAG, "end");
                } else {
                    try {
                        outputStream.write(outData, 0, outData.length);
                    } catch (IOException e) {
                    }
                    Log.d(TAG, "normal");
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mediaCodec.releaseOutputBuffer(outputBufferId, false);
                image.close();
                break;
        }
    }


    private void StopEncoder() {
        try {
            mediaCodec.stop();
            mediaCodec.release();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    private static String path = Environment.getExternalStorageDirectory() + "/carxk.h264";
    private FileOutputStream outputStream;
    public void createFile() throws FileNotFoundException {
        File file = new File(path);
        if (file.exists()){
            file.delete();
        }

        Log.d(TAG, "init file");
        outputStream = new FileOutputStream(path, true);
    }

    public void CloseFile() throws IOException {
        StopEncoder();
        outputStream.flush();
        outputStream.close();
    }

    /**
     * Generates the presentation time for frame N, in microseconds.
     */
    private long computePresentationTime(long frameIndex) {
        return 132 + frameIndex * 1000000 / m_framerate;
    }



    public void YUV_888To420(Image image){
        //将YUV_888格式的图片转换为YUV_420
        Rect crop = image.getCropRect();
        int format = image.getFormat();//format: 35 = 0x23
        int width = crop.width();
        int height = crop.height();

        Image.Plane[] planes = image.getPlanes();

        data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];
        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i++) {
            switch (i) {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    channelOffset = width * height;
                    outputStride = 1;
                    break;
                case 2:
                    channelOffset = (int) (width * height * 1.25);
                    outputStride = 1;
                    break;
            }
            ByteBuffer planebuffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();
            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;
            planebuffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = w;
                    planebuffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;
                    planebuffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1) {
                    planebuffer.position(planebuffer.position() + rowStride - length);
                }
            }
        }

        Log.d(TAG,"data come");
    }

}
