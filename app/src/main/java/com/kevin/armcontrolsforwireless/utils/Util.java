package com.kevin.armcontrolsforwireless.utils;

import android.content.Context;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator
 * on 2016/1/12.
 */
public class Util {

    /**
     * 得到设备屏幕的宽度
     */
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 得到设备屏幕的高度
     */
    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 得到设备的密度
     */
    public static float getScreenDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    /**
     * 把密度转换为像素
     */
    public static int dip2px(Context context, float px) {
        final float scale = getScreenDensity(context);
        return (int) (px * scale + 0.5);
    }

    /**
     * 16进制字符串转byte数组
     */
    public static byte[] HexString2Bytes(String hexString) {
        int stringLength = hexString.length();
        byte[] data = new byte[(stringLength / 2)];
        for (int i = 0, j = 0; i < data.length; i++, j = j + 2) {
            data[i] = (byte) Integer.parseInt(hexString.substring(j, (j + 2)), 16);
        }
        return data;
    }

    /**
     * byte数组转16进制字符串
     */
    public static String bytes2HexString(byte[] b, int byteLength) {
        String ret = "";
        for (int i = 0; i < byteLength; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret;
    }

    //数据校验码
    public static byte CheckCode(String hexData) {
        byte reData;
        int sum = 0;
        int dataLength = hexData.length();
        for (int i = 0; i < (dataLength); i = i + 2) {
            sum = sum + Integer.parseInt(hexData.substring(i, 2 + i), 16);
        }
        String temp = "0" + Integer.toHexString(sum);
        reData = (byte) Integer.parseInt(temp.substring(temp.length() - 2, temp.length()).toUpperCase(), 16);
        return reData;
    }

    //16进制字符串转中文字符
    public static String hexString2Characters(String hexString) {
        String mCharacters = null;
        try {
            mCharacters = new String(Util.HexString2Bytes(hexString), "utf-8").trim();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return mCharacters;
    }


    /**
     * 检验数据
     * @param data 需要检验的数据
     * @return 验证的结果
     */
    public static boolean checkData(String data) {
        if (data.length() >= 4) {
            // 数据头ffaa
            if ("FF".equalsIgnoreCase(data.substring(0, 2))
                    && "AA".equalsIgnoreCase(data.substring(2, 4))) {
                if (data.length() >= 26) {
                    //数据内容长度
                    int dataLength = Integer.parseInt(data.substring(24, 26), 16);
                    if (data.length() >= (dataLength * 2 + 34)) {
                        //数据尾ff55
                        if (data.substring(dataLength * 2 + 30,
                                dataLength * 2 + 32).equalsIgnoreCase("FF")
                                && data.substring(dataLength * 2 + 32,
                                dataLength * 2 + 34).equalsIgnoreCase("55")) {
                            if (data.substring(data.length() - 4, data.length() - 2).equalsIgnoreCase("FF")
                                    && data.substring(data.length() - 2, data.length()).equalsIgnoreCase("55")) {
//                                int sum = 0;
//                                for (int i = 0; i < (dataLength); i++) {
//                                    sum = sum + Integer.parseInt(data.substring(28 + i * 2, 30 + i * 2), 16);
//                                }
//                                String temp = "0" + Integer.toHexString(sum);
//                                if (data.substring(data.length() - 6, data.length() - 4).equalsIgnoreCase(temp.substring(temp.length() - 2, temp.length()).toUpperCase())) {
                                    //数据验证通过
                                    Log.e("Util", "数据为真。命令=" + data.substring(20, 24));
                                    return true;
//                                }
                            }
                        }
                    }

                }

            }
        }
        return false;
    }


    /**
     * 检验数据
     * @param data 需要检验的数据
     * @return 验证的结果
     */
    public static boolean checkData2(String data) {
//        FFAA 1C810000 00C9 0100 0000 FF55 //24
        if (data.length() >= 4) {
            // 数据头ffaa
            if ("FF".equalsIgnoreCase(data.substring(0, 2))
                    && "AA".equalsIgnoreCase(data.substring(2, 4))) {
                if (data.length() >= 26) {
                        //数据尾ff55

                            if (data.substring(data.length() - 4, data.length() - 2).equalsIgnoreCase("FF")
                                    && data.substring(data.length() - 2, data.length()).equalsIgnoreCase("55")) {
  //数据验证通过
                                Log.e("Util", "数据为真。命令=" + data.substring(20, 24));
                                return true;
//                                }
                            }



                }

            }
        }
        return false;
    }

    public static String hexString2binaryString(String hexString)
    {
        if (hexString == null || hexString.length() % 2 != 0)
            return null;
        String bString = "", tmp;
        for (int i = 0; i < hexString.length(); i++)
        {
            tmp = "0000"
                    + Integer.toBinaryString(Integer.parseInt(hexString
                    .substring(i, i + 1), 16));
            bString += tmp.substring(tmp.length() - 4);
        }
        return bString;
    }



    public static String hexStringToAscii(String hex) {
        int length = hex.length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length / 2; i++) {
            sb.append((char) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16));
        }
        return sb.toString();

    }

    public static String asciiToHexString(String asciiString) {
        StringBuilder builder = new StringBuilder();
        char[] asciiChars = asciiString.toCharArray();
        for (char anChar : asciiChars) {
            builder.append(Integer.toHexString((int) anChar));
        }

        return builder.toString();
    }

    public static String doWithArmExecuteActionInfoString(String data) {
        String reData = "";
        List<Integer> executeList = new ArrayList<>();
        List<Integer> executeList2 = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            if (data.substring(i, i + 1).equalsIgnoreCase("1")) {
                executeList.add((data.length() - 1 - i));
            }
        }

        for (int j = executeList.size() - 1; j >= 0; j--) {
            executeList2.add(executeList.get(j));
        }

        for (int k = 0; k <executeList2.size(); k++) {
            reData = reData + executeList2.get(k)+" ";
        }
        return reData;
    }


    public static List<Integer> doWithArmExecuteActionInfoList(String executeInfo) {
        List<Integer> executeList = new ArrayList<>();
        List<Integer> executeList2 = new ArrayList<>();
        for (int i = 0; i < executeInfo.length(); i++) {
            if (executeInfo.substring(i, i + 1).equalsIgnoreCase("1")) {
                executeList.add((executeInfo.length() - 1 - i));
            }
        }

        for (int j = executeList.size() - 1; j >= 0; j--) {
            executeList2.add(executeList.get(j));
        }
        return executeList2;
    }

}


