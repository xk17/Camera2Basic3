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
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.camera2basic.Fragment.Camera2BasicFragment;
import com.example.android.camera2basic.Fragment.ShowFragment;
import com.example.android.camera2basic.TCP.SocketWrapper;
import com.example.android.camera2basic.TCP.Stream;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

public class CameraActivity extends FragmentActivity{
    private final String TAG = "CameraActivity";

    private ShowFragment showFragment1;
    private ShowFragment showFragment2;
    private ShowFragment showFragment3;
    private Camera2BasicFragment camera2BasicFragment;
    private boolean clientFlag;
    public static ArrayList<Stream> streams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        streams = new ArrayList<Stream>();
        //camera preview fragment
        camera2BasicFragment = Camera2BasicFragment.newInstance();
        setContentView(R.layout.activity_camera);
        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.cameraPreview, camera2BasicFragment)
                    .commit();
        }


        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        showFragment1 = ShowFragment.newInstance(1);
        transaction.replace(R.id.videoPlay1,showFragment1);
        showFragment2 = ShowFragment.newInstance(2);
        transaction.replace(R.id.videoPlay2,showFragment2);
        showFragment3 = ShowFragment.newInstance(3);
        transaction.replace(R.id.videoPlay3,showFragment3);
        transaction.commit();
        Log.d(TAG,"init codec");



//        Button recordBtn = (Button)findViewById(R.id.encode);
//        recordBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG,"录制——按钮");
//                startRecord();
//            }
//        });


        final Button serverBtn = (Button)findViewById(R.id.server);
        serverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"服务器——按钮");
                clientFlag = false;
                new SocketWrapper(clientFlag,"");
                serverBtn.setText("READY!");
            }
        });

        // Get and display the local IP address for fill in another devices
        String localIP;
        localIP = getLocalHostIp();
        TextView textLocalIP = (TextView)findViewById(R.id.localIP);
        textLocalIP.setText(localIP);


        final IPEditText ipEditText_0 = (IPEditText)findViewById(R.id.ip_0);
        final Button connect_0_Btn = (Button)findViewById(R.id.connect_0);
        connect_0_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isClient = true;
                String targetIP = ipEditText_0.getText();
                if (targetIP == null){
                    return;
                }
                new SocketWrapper(isClient,targetIP);
                connect_0_Btn.setText("READY!");
            }
        });

        final IPEditText ipEditText_1 = (IPEditText)findViewById(R.id.ip_1);
        final Button connect_1_Btn = (Button)findViewById(R.id.connect_1);
        connect_1_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isClient = true;
                String targetIP = ipEditText_1.getText();
                if (targetIP == null){
                    return;
                }
                new SocketWrapper(isClient,targetIP);
                connect_1_Btn.setText("READY!");
            }
        });

        final IPEditText ipEditText_2 = (IPEditText)findViewById(R.id.ip_2);
        final Button connect_2_Btn = (Button)findViewById(R.id.connect_2);
        connect_2_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isClient = true;
                String targetIP = ipEditText_2.getText();
                if (targetIP == null){
                    return;
                }
                new SocketWrapper(isClient,targetIP);
                connect_2_Btn.setText("READY!");
            }
        });
    }


//    //record
//    public void startRecord(){
//        camera2BasicFragment.startRecord();
//    }

    @Nullable
    private String getLocalHostIp()
    {
        try
        {
            Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces();
            // ergodic all the network interfaces which can be used.
            while (en.hasMoreElements())
            {
                NetworkInterface nif = en.nextElement();// get every IP binding to each network interface
                Enumeration<InetAddress> inet = nif.getInetAddresses();
                // ergodic each IP binding to each network interface
                while (inet.hasMoreElements())
                {
                    InetAddress ip = inet.nextElement();
                    //if (!ip.isLoopbackAddress()&& InetAddressUtils.isIPv4Address(ip.getHostAddress()))
                    if (!ip.isLoopbackAddress()&& (ip instanceof Inet4Address))
                    {
                        return ip.getHostAddress();
                    }
                }
            }
        }
        catch (SocketException e)
        {
            Log.e(TAG, "fail to get local IP address");
            e.printStackTrace();
        }
        return null;
    }

}




