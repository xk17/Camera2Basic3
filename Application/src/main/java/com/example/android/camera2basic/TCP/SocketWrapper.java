package com.example.android.camera2basic.TCP;

import android.os.Environment;
import android.util.Log;

import com.example.android.camera2basic.CameraActivity;
import com.example.android.camera2basic.Fragment.Camera2BasicFragment;
import com.example.android.camera2basic.Fragment.ShowFragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by xuke on 2017/4/26.
 */

public class SocketWrapper {
    private String TAG = "socket";
    private Socket clientSocket;
    private ServerSocket serverSocket;
    private boolean client;
    private String serverIP;
    private Stream stream;
    private ArrayList<Stream> streams;
    private boolean encodeFlag;



    public SocketWrapper(Boolean client,String serverIP){
        if (streams == null){
        this.streams = CameraActivity.streams;
        }
        this.client = client;
        this.serverIP = serverIP;
        this.encodeFlag = Camera2BasicFragment.isRecord;
        new initSocket().start();
    }

    private class initSocket extends Thread{
        int THREADPOOLSIZE = 3;
        @Override
        public void run(){
            super.run();
//            TCP连接
            try {
                if (client) {
                    clientSocket = new Socket(serverIP, 18888);
                    stream = new Stream(clientSocket);
                    streams.add(stream);
                    Log.d(TAG, "与服务器连接成功");
                    if (!encodeFlag) {
                        startRecord();
                    }
                    Log.d(TAG, "stream长度"+ streams.size());
                }else {
                    serverSocket = new ServerSocket(18888);
                    Log.d(TAG, "服务器开始监听");
                    for (int i = 0; i < THREADPOOLSIZE; i++) {
                        Thread thread = new Thread() {
                            public void run() {
                                //线程为某连接提供完服务后，循环等待其他的连接请求
                                while (true) {
                                    try {
//                                        Socket connectSocket;
                                        clientSocket = serverSocket.accept();
                                        Log.d(TAG, "与客户端连接成功");
                                        stream = new Stream(clientSocket);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    streams.add(stream);
                                    if (!encodeFlag) {
                                        startRecord();
                                    }
                                }
                            }
                        };
                        thread.start();
                    }

                }
                } catch(IOException e){
                    e.printStackTrace();
                }
        }
    }

        //record
    public void startRecord(){
        Camera2BasicFragment.startRecord();
    }

    private FileOutputStream outputStream;
    private static String path = Environment.getExternalStorageDirectory() + "/carxk2.h264";
    public void createFile() throws FileNotFoundException {
        File file = new File(path);
        if (file.exists()){
            file.delete();
        }

        Log.d(TAG, "init file");
        outputStream = new FileOutputStream(path, true);
    }
}

