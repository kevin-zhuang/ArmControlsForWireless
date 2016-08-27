package com.kevin.armcontrolsforwireless.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Created by Administrator
 * on 2016/7/11.
 */
public class BroadcastUdp {

    private static final String TAG = "BroadcastUdp";
    private static final String BROADCAST_IP = "255.255.255.255";
    private static final int BROADCAST_PORT = 5679;
    private static final int HANDLER_MESSAGE = 0x123;
    private DatagramSocket udpSocket;
    private DatagramPacket udpSendPacket;
    private DatagramPacket udpReceivePacket;
    private byte[] receiveBytes = new byte[512];
    private InetAddress inetAddress;
    private MyHandler myHandler;
    private OnReceiveListen onReceiveListen;
    private Thread receiveThread;
    public BroadcastUdp() {

    }


    /**
     * 初始化
     */
    public void init() {
        try {
            udpSocket = new DatagramSocket();
            myHandler = new MyHandler(Looper.getMainLooper());
            receive();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送方法
     *
     * @param data 发送的数据
     */
    public void send(byte[] data) {
        try {
            if(inetAddress == null){
                inetAddress = InetAddress.getByName(BROADCAST_IP);
            }

            if(udpSendPacket==null){
                udpSendPacket = new DatagramPacket(data, data.length, inetAddress, BROADCAST_PORT);
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        udpSocket.send(udpSendPacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * 接收方法
     */
    public void receive() {

        receiveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!receiveThread.isInterrupted()) {
                    udpReceivePacket = new DatagramPacket(receiveBytes, receiveBytes.length);
                    try {
                        udpSocket.receive(udpReceivePacket);
                        int len = udpReceivePacket.getLength();
                        if(len > 0){
                            if(onReceiveListen!=null){

                                Log.e(TAG, "ip=" + udpReceivePacket.getAddress().toString());
                                onReceiveListen.onReceiveData(receiveBytes,len,udpReceivePacket.getAddress().toString().substring(1));
                                myHandler.sendEmptyMessage(HANDLER_MESSAGE);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        });
        receiveThread.start();

    }

    /**
     * 停止广播
     */
    public void stop(){

        if(receiveThread!=null){
            receiveThread.interrupt();
        }
        if(udpSocket!=null){
            udpSocket.close();
            Log.e(TAG,"broadcastClose");
        }
    }


    /**
     * 设置监听
     * @param listen 监听接口
     */
    public void setReceiveListen(OnReceiveListen listen){
        this.onReceiveListen = listen;
    }


    /**
     * Handler处理
     */
    private class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if(msg.what == HANDLER_MESSAGE){
                Arrays.fill(receiveBytes,(byte)0x00);
            }
        }
    }


}
