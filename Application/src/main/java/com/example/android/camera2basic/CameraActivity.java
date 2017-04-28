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
import com.example.android.camera2basic.TCP.SocketWrapper;

public class CameraActivity extends FragmentActivity{
    private final String TAG = "CameraActivity";

    private ShowFragment showFragment;
    private Camera2BasicFragment camera2BasicFragment;
    public static Quene quene;
    private SocketWrapper clientSocket;
    private boolean client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        quene = new Quene();
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

        Button serverBtn = (Button)findViewById(R.id.server);
        serverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"服务器——按钮");
                client = false;
                clientSocket = new SocketWrapper(client);
            }
        });
        Button clientBtn = (Button)findViewById(R.id.client);
        clientBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"客户端——按钮");
                client = true;
                clientSocket = new SocketWrapper(client);
            }
        });
    }

    private boolean isRecvH264 = false;
    public void startRecvH264(){
        isRecvH264 = true;
        if (isRecvH264){
            clientSocket.startRecv();
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
    private boolean isSendH264 = false;
    public void startSend(){
        Log.d(TAG,"end start");
        isSendH264 = true;
        clientSocket.startSend();
    }



}




