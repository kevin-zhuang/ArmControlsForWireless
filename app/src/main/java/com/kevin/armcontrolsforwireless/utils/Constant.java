package com.kevin.armcontrolsforwireless.utils;

/**
 * Created by Administrator
 * on 2016/7/8.
 */
public class Constant {

    public Constant() {
    }

    public static final String CMD_SEARCH_RESPOND = "0001";//搜索设备
    public static final String CMD_EXECUTE_RESPOND = "00C9";//握手设备
    public static final int REMOTE_PORT = 5679;//远程端口

    public static final int SEARCH_WAIT_DIALOG_TIME = 5000;//搜索设备时间
    public static final int UNLOCK_WAIT_DIALOG_MAX_TIME = 2000;//解锁设备时间


    public static final String SEND_DATA_SEARCH = "FF AA 00 00 00 00 01 00 00 00 00 FF 55";//广播数据
    public static final String SEND_DATA_SHAKE = "FF AA 00 00 00 00 0A 00 00 00 00 FF 55";//握手数据


    public static String SEND_DATA_EXECUTE(String armId, String exId) {
//        return "FFAA " + armId + " 0200 0000 00 FF55";//执行动作
        return "FF AA   " + armId + " 00 00   88 00   01 00   " + exId + exId + " FF 55";
    }
    // FF AA 1C 81 00 00 88 00 01 00 01 01 FF 55


}

