package com.example.android.camera2basic;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.camera2basic.CameraActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

/**
 * Created by xuke on 2017/4/20.
 */

public class EncoderH264 {
    private final static String TAG = "MeidaCodec";
    private Quene queue;
    private int TIMEOUT_USEC = 11000;

    private MediaCodec mediaCodec;
    int m_width;
    int m_height;
    int m_framerate;
    MediaFormat mOutputFormat; // member variable
    private byte[] data;
    private Image m_image;

    public byte[] HeadInfo;

    public EncoderH264(int imageW, int imageH,int framerate){
        queue = CameraActivity.quene;
        m_width = imageW;
        m_height = imageH;
        Log.d(TAG, "输入编码器数据宽" + imageW + "高" +imageH);
        m_framerate = framerate;

        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", imageW, imageH);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
//        同样分辨率下，视频文件的码流越大，压缩比就越小，画面质量就越高。
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, imageW * imageH * 5);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, m_framerate);
        mediaFormat.setInteger(MediaFormat.KEY_ROTATION, 90);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
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

      public void code(Image image){

          isImageFormatSupported(image);


//          image格式转换获得I420格式的数据
            data = getDataFromImage(image,2);

            ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
            ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();


            ////获取可用的inputBuffer -1代表一直等待，0表示不等待 建议-1,避免丢帧
            int inputBufferId = mediaCodec.dequeueInputBuffer(-1);
            if (inputBufferId >= 0) {
//            ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferId);
                ByteBuffer inputBuffer = inputBuffers[inputBufferId];
                pts = computePresentationTime(generateIndex);
                // fill inputBuffer with valid data
                inputBuffer.clear();
                inputBuffer.put(data);
                Log.d(TAG, "放入的data" + data);
                //xk注释4／27
//                mediaCodec.queueInputBuffer(inputBufferId, 0, data.length,pts, 1);
                mediaCodec.queueInputBuffer(inputBufferId, 0, data.length,pts, 0);

                generateIndex += 1;
                Log.d(TAG, "放入数据成功");
            }
            //执行上面的操作后就把待编解码的数据存入了输入缓冲区，然后下一步就是操作然后把编解码的数据存入输出缓冲区
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

            int outputBufferId = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
//          int outputBufferId = mediaCodec.dequeueOutputBuffer(bufferInfo, 100);

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
//                    while (outputBufferId >= 0) {
//                        ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputBufferId);
                    ByteBuffer outputBuffer = outputBuffers[outputBufferId];
                        MediaFormat bufferFormat = mediaCodec.getOutputFormat(outputBufferId); // option A
                        // bufferFormat is identical to outputFormat
                        // outputBuffer is ready to be processed or rendered.
//                        outputBuffer.slice();
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
                            queue.offerSendH264Queue(key);
                            Log.d("key", "key");
                            //put key frame to queue
                        } else if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                            //end frame
                            Log.d(TAG, "end");
                        } else {
                            try {
                                outputStream.write(outData, 0, outData.length);

                            } catch (IOException e) {
                            }
                            queue.offerSendH264Queue(outData);
                            Log.d("normal", "normal");
                        }
                        mediaCodec.releaseOutputBuffer(outputBufferId, false);
//                        outputBufferId = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
//                    }
                        break;

            }
          image.close();
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
        return  132 + frameIndex * 1000000/ 24;
    }


//编码器颜色
    private static final int COLOR_FormatI420 = 1;
//   摄像头拍摄颜色
    private static final int COLOR_FormatNV21 = 2;

    private static boolean isImageFormatSupported(Image image) {
        int format = image.getFormat();
//        测试格式为35，即YUV_420_888
//        Log.d(TAG, format + "");
        switch (format) {
            case ImageFormat.YUV_420_888:
            case ImageFormat.NV21:
            case ImageFormat.YV12:
                return true;
        }
        return false;
    }

// 设置参数colorFormat把Image转换成NV21（参数为2）或I420（参数为1）格式，数据为byte型
    private static byte[] getDataFromImage(Image image, int colorFormat) {
        if (colorFormat != COLOR_FormatI420 && colorFormat != COLOR_FormatNV21) {
            throw new IllegalArgumentException("only support COLOR_FormatI420 " + "and COLOR_FormatNV21");
        }
        if (!isImageFormatSupported(image)) {
            throw new RuntimeException("can't convert Image to byte array, format " + image.getFormat());
        }
        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];
        if (true) Log.v(TAG, "get data from " + planes.length + " planes");
        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i++) {
            switch (i) {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    if (colorFormat == COLOR_FormatI420) {
                        channelOffset = width * height;
                        outputStride = 1;
                    } else if (colorFormat == COLOR_FormatNV21) {
                        channelOffset = width * height + 1;
                        outputStride = 2;
                    }
                    break;
                case 2:
                    if (colorFormat == COLOR_FormatI420) {
                        channelOffset = (int) (width * height * 1.25);
                        outputStride = 1;
                    } else if (colorFormat == COLOR_FormatNV21) {
                        channelOffset = width * height;
                        outputStride = 2;
                    }
                    break;
            }
            ByteBuffer buffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();
            if (true) {
                Log.v(TAG, "pixelStride " + pixelStride);
                Log.v(TAG, "rowStride " + rowStride);
                Log.v(TAG, "width " + width);
                Log.v(TAG, "height " + height);
                Log.v(TAG, "buffer size " + buffer.remaining());
            }
            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = w;
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
            if (true) Log.v(TAG, "Finished reading data from plane " + i);
        }
        return data;
    }

}
