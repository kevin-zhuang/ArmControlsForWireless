package com.kevin.armcontrolsforwireless.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kevin.armcontrolsforwireless.R;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;


/**
 * smartLink
 */
public class SmartLinkActivity extends AppCompatActivity {

    private TextView tvShowCurrentWifi;
    private EditText etSSIDPW;
    private Button btnStart;
    private CheckBox cbShowPw;
    private ProgressBar progressBar;
    private Button btnSettingWifi;
    private BroadcastReceiver mWifiChangedReceiver;//广播
    private final int ConnRobotAPMax = 15;//连接robotAP最大等待秒数

    //设备AP
    private static final String robotAPSSID = "ZB_WG31";
    private static final String robotAPSSIDPW = "02164705662";

    private String currentWifi = "";//保存最开始连接的wifi.stop后要清零

    private int connRobotAPTime = 0;//连接设备AP的时间次数，1s一次，最多3次
    private String TAG = "MainActivity";
    private int flagConnSucessRobotAP = -1;//连接设备AP成功的标志，成功为1
    private boolean whileConnRobotAP = false;//连接设备AP时while的判断标志

    //连上robotAP后建立udp1
    private static final String udp1Ip = "192.168.1.1";//默认192.168.1.1
    private static final String udp1Port = "2378";//默认 2378
    private InetAddress firstUdpInetAddress;//第一次的udp1的InetAddress
    private DatagramSocket firstUdpSocket;//第一次udp1的socket
    private boolean firstUdpRevWhile = false;//第一次udp1的接收线程的while标志
    private DatagramPacket firstDataPacket;//第一次udp1的DatagramPacket
    private byte[] firstUdpReceiveByte = new byte[512];//第一次udp1的接收缓存
    private Handler firstUdpHandler = new Handler();//第一次udp1的Handler
    private Runnable firstUdpRunnable;//第一次udp1的Runable
    private DatagramPacket firstUdpPacketToSend;//第一次udp1的发送DatagramPacket
    private int firstUdpSendCount = 0;//第一次udp1的定时发送计数，最多发送3次
    private boolean firstUdpBreakDOwhile = false;//第一次udp1的发送次数while标志

    //只运行一次的标志
    private int firstUdpRunOnce = 0;
    private int secondUdpRunOnce = 0;
    private int noConnRunOnce = -1;

    private boolean reConnBeforWifiFlag = false;

    //第二次udp2
    private static final String udp2Ip = "255.255.255.255";
    private static final String udp2Port = "5678";//默认5678
    private InetAddress secondUdpInetAddress;//第2次的udp2
    private DatagramSocket secondUdpSocket;//第2次udp1 secondUdpSocket
    private boolean secondUdpRevWhile = false;//第2次udp2
    private DatagramPacket secondUdpDataPacket;//第2次udp2
    private byte[] secondUdpReceiveByte = new byte[512];//第2次udp2
    private Handler secondUdpHandler = new Handler();//第2次udp2
    private Runnable secondUdpRunnable;//第2次udp2
    private DatagramPacket secondUdpPacketToSend;//第2次udp2
    private int secondUdpSendCount = 0;
    private boolean secondUdpBreakDOwhile = false;


    //连接robotAP时的handler和runnable
    private Handler connRobotAPHandler = new Handler();
    private Runnable connRobotAPRunnable;//

    private boolean step1 = false;
    private boolean step2 = false;
    private boolean step3 = false;

    private boolean step1go = true;
    private boolean step2go = true;

    private boolean btnTurn = false;

    private static final String BUTTON_STRING_START = "START";
    private static final String BUTTON_STRING_STOP = "STOP";

    //数据处理
    private byte[] dataHead = {(byte) 0xFF, (byte) 0xAA};// 0、1
    private byte[] dataID = {(byte) 0x11, (byte) 0x12, (byte) 0x13,
            (byte) 0x14, (byte) 0x15, (byte) 0x16, (byte) 0x17, (byte) 0x18};// 2、9
    private byte[] dataCommand = {(byte) 0xff, (byte) 0x08};// 10、11
    private byte[] dataTail = {(byte) 0xFF, (byte) 0x55}; // dataContext.length

    // 广播的数据
    private byte[] seek = {(byte) 0xff, (byte) 0xaa, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xff,
            (byte) 0x55};

    private String mac = "";
    private MyHandler myHandler;

    private final int MSG_CONN_FAIL_1 = 1;
    private final int MSG_CONN_FAIL_2 = 2;
    private final int MSG_CONN_FAIL_3 = 3;
    private final int MSG_CONN_FAIL_4 = 4;//成功

    private boolean btnStartClick = false;
    private boolean btnStartClick2 = false;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static final String SP_FILE_NAME = "smartLink";
    private static final String SP_WIFI_PW = "wifiPw";
    private ActionBar.LayoutParams params = new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_link);
        init();
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("SmartLink");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setSubtitle("主页面");
        }
        myHandler = new MyHandler(Looper.getMainLooper());
        brodcastWifi();
    }

    private void init() {
        tvShowCurrentWifi = (TextView) findViewById(R.id.tvShowCurrentWifi);
        etSSIDPW = (EditText) findViewById(R.id.etSSIDPW);
        btnStart = (Button) findViewById(R.id.btnStart);
        cbShowPw = (CheckBox) findViewById(R.id.cbShowPw);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnSettingWifi = (Button) findViewById(R.id.btnSettingWifi);
        sharedPreferences = getSharedPreferences(SP_FILE_NAME, Activity.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        clicks();
        if(!TextUtils.isEmpty(sharedPreferences.getString(SP_WIFI_PW, null))){
            etSSIDPW.setText(sharedPreferences.getString(SP_WIFI_PW,null));
            cbShowPw.setChecked(true);
        }
    }

    private void clicks() {

        cbShowPw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    // 如果选中，显示密码
                    etSSIDPW.setTransformationMethod(HideReturnsTransformationMethod
                            .getInstance());
                    CharSequence charSequence = etSSIDPW.getText();
                    if (charSequence != null) {
                        Spannable spanText = (Spannable) charSequence;
                        Selection.setSelection(spanText, charSequence.length());
                    }
                } else {
                    // 否则隐藏密码
                    etSSIDPW.setTransformationMethod(PasswordTransformationMethod
                            .getInstance());
                    CharSequence charSequence = etSSIDPW.getText();
                    if (charSequence != null) {
                        Spannable spanText = (Spannable) charSequence;
                        Selection.setSelection(spanText, charSequence.length());
                    }
                }

            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!btnTurn) {

                    if (TextUtils.isEmpty(etSSIDPW.getText().toString())) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(SmartLinkActivity.this);
                        builder.setTitle("密码为空");
                        builder.setMessage("没有输入密码？如果该wifi没有密码，请点击确定继续，如果忘记输入密码，请点击取消，重新输入密码");
                        builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                btnStartDo();
                            }
                        });
                        builder.setNeutralButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                btnTurn = !btnTurn;
                                dialog.dismiss();

                            }
                        });
                        builder.setCancelable(false);
                        builder.create();
                        builder.show();

                    } else {
                        btnStartDo();
                        //保存wifi密码
                        editor.putString(SP_WIFI_PW,etSSIDPW.getText().toString());
                        editor.commit();
                    }


                } else {
                    //stop
                    btnStopDo();
                }
                btnTurn = !btnTurn;

            }
        });

        btnSettingWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                //判断手机系统的版本  即API大于10 就是3.0或以上版本
                if (android.os.Build.VERSION.SDK_INT > 10) {
                    intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                } else {
                    intent = new Intent();
                    ComponentName component = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
                    intent.setComponent(component);
                    intent.setAction("android.intent.action.VIEW");
                }
                startActivity(intent);
            }
        });

    }


    //分步停止

    /**
     * 1、step1是第一步，连接到指定robotAP上。在这步上，wifi会断开一次，进入6秒的判断，如果要停止，记得重置标志位
     * 2、step2是第二步，连接到robotAP上后。要开始建立udp1了，如果已经建立udp1，接收线程也已经开启，这时应该关掉udp和结束接收线程
     * 3、step3是第三步，连接到robotAP上后，建立udp2，发送数据10次后，接收到了数据，要重新连接之前的wifi，然后去建立udp2
     */

    private void btnStopDo() {
        Log.e(TAG, "btnStopDo");
        btnStartClick = false;
        btnStartClick2 = false;
        btnStart.setText(BUTTON_STRING_START);//btnTurn == true;
        btnStart.setBackgroundResource(R.drawable.btn_bg);
        removeRobotAP(robotAPSSID);//停止时，直接去除设备的AP信息
        /*
        已经进入第一步，就是已经开始连接robotAP，可能连上了，也可能没连上
        如果连上了（在6s内），就停止往下走，
        如果没连上，有2中情况，一种是在6s内，一种是超过6s
          超过6s就会提示，此时已经有提示窗了，就不用再处理了
          没有超过6s的，就要停止提示
        */
        SmartLinkActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(0);
            }
        });
        if (step1) {

            // 连上了robotAP（在6s内），停止往下走，并连到之前的wifi
            step1go = false;
            //没有超过6s的，就要停止提示
            if (connRobotAPTime < ConnRobotAPMax) {
                whileConnRobotAP = true;
            }
            if (whileConnRobotAP) {
                connFirstWifi(currentWifi);
            }
        }

        if (step2) {
            firstUdpRevWhile = false;
            firstUdpHandler.removeCallbacks(firstUdpRunnable);
            firstUdpBreakDOwhile = true;
            if (firstUdpSocket != null) {
                firstUdpSocket.close();
            }
            step2go = false;
            connFirstWifi(currentWifi);
        }

        if (step3) {
            secondUdpRevWhile = false;
            secondUdpHandler.removeCallbacks(secondUdpRunnable);
            secondUdpBreakDOwhile = true;
            if (secondUdpSocket != null) {
                secondUdpSocket.close();
            }
        }


    }

    private void btnStartDo() {
        btnStartClick2 = true;
        Log.e(TAG, "btnStartDo");
        btnStartClick = true;
        btnStart.setText(BUTTON_STRING_STOP);
        btnStart.setBackgroundResource(R.drawable.btn_bg2);
        SmartLinkActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(5);
            }
        });
        currentWifi = getSSid();
        Log.e(TAG, "currentWifi=" + currentWifi);
        step1 = false;
        step1go = true;
        step2 = false;
        step2go = true;
        step3 = false;

        //连接6s，6s内连不上就提示，连接上就下一步
        connRobotAPTime = 0;
        flagConnSucessRobotAP = -1;
        whileConnRobotAP = false;
        firstUdpSendCount = 0;
        firstUdpBreakDOwhile = false;
        firstUdpRunOnce = 0;
        secondUdpRunOnce = 0;
        noConnRunOnce = 0;

        reConnBeforWifiFlag = false;

        secondUdpSendCount = 0;//第二次udp
        secondUdpBreakDOwhile = false;//第二次udp

        addnet(robotAPSSID, robotAPSSIDPW);

        connRobotAPRunnable = new Runnable() {
            @Override
            public void run() {
                connRobotAPTime++;
                connRobotAPHandler.postDelayed(connRobotAPRunnable, 1000);
                if (connRobotAPTime > ConnRobotAPMax) {
                    connRobotAPHandler.removeCallbacks(connRobotAPRunnable);
                }
            }
        };
        connRobotAPHandler.postDelayed(connRobotAPRunnable, 1000);

    }


    /**
     * 广播形式实现显示当前连接上的wifi名称
     */
    private void brodcastWifi() {
        mWifiChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivityManager == null) {
                    Log.e(TAG, "connectivityManager is null");
                }
                NetworkInfo networkInfo = connectivityManager != null ? connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI) : null;
                if (networkInfo != null && networkInfo.isConnected()) {
                    btnSettingWifi.setVisibility(View.GONE);
                    String stringssid = getSSid();
                    btnStart.setEnabled(true);
                    //第一次连接到robotAP验证
                    Log.e(TAG, "robotAPSSID=" + robotAPSSID + " stringssid=" + stringssid + " firstUdpRunOnce=" + firstUdpRunOnce);
                    if (robotAPSSID.equals(stringssid)) {

                        if (firstUdpRunOnce == 0) {
                            firstUdpRunOnce = 1;
                            flagConnSucessRobotAP = 1;
                            Log.e(TAG, "连接成功");
                            SmartLinkActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setProgress(10);
                                }
                            });

                            whileConnRobotAP = true;
                            btnStartClick2 = false;
                            if (step1go) {
                                startUdp();
                            }

                        }
                    } else {

                        if (btnStartClick2) {
                            Log.e(TAG, "connRobotAPTime=" + connRobotAPTime);
                            addnet(robotAPSSID, robotAPSSIDPW);
                            if (connRobotAPTime == ConnRobotAPMax) {
                                btnStartClick2 = false;
                            }
                        }
                        tvShowCurrentWifi.setText("您当前的网络：" + stringssid);
                    }
                    //第一次udp1结束，开始第二次的udp
                    if (reConnBeforWifiFlag) {
                        if (currentWifi.equals(stringssid)) {
                            //开始第10次的广播
                            if (secondUdpRunOnce == 0) {
                                secondUdpRunOnce = 1;
                                if (step2go) {//如果按下了stop，step2go=false，接下来就不用做了
                                    SmartLinkActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressBar.setProgress(80);
                                        }
                                    });
                                    startUdp2();

                                }

                            }
                        }
                    }


                } else {


//                    tvShowCurrentWifi.setText("没有连接到wifi");
                    //第一次连接到robotAP验证
//                    if(onCreateFlag){
//                        onCreateFlag = false;
//                        tvShowCurrentWifi.setText("没有连接到wifi");
//                    }
                    if (!btnStartClick) {
                        btnSettingWifi.setVisibility(View.VISIBLE);
                        tvShowCurrentWifi.setText("没有连接到wifi");
                        btnStart.setEnabled(false);

                    }

                    if (noConnRunOnce == 0) {
                        noConnRunOnce = 1;
                        new Thread() {
                            @Override
                            public void run() {
                                while (!whileConnRobotAP) {
                                    if (flagConnSucessRobotAP == -1 && connRobotAPTime == ConnRobotAPMax) {
                                        Log.e(TAG, "连接失败");
                                        myHandler.sendEmptyMessage(MSG_CONN_FAIL_1);
                                        connFirstWifi(currentWifi);
                                        whileConnRobotAP = true;
                                        btnTurn = false;
                                        SmartLinkActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                btnStart.setText(BUTTON_STRING_START);
                                                btnStart.setBackgroundResource(R.drawable.btn_bg);
                                            }
                                        });
                                        break;
                                    }
                                }

                            }
                        }.start();
                    }


                }
            }
        };
        //注册广播
        registerReceiver(mWifiChangedReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    //得到wifi名称
    private String getSSid() {
        Log.e(TAG, "getSSID()");
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        if (wm != null) {
            WifiInfo wi = wm.getConnectionInfo();
            if (wi != null) {
                wi.getHiddenSSID();
                String ssid = wi.getSSID();
                if (ssid.length() > 2 && ssid.startsWith("\"") && ssid.endsWith("\"")) {
                    return ssid.substring(1, ssid.length() - 1);
                } else {
                    return ssid;
                }
            }
        }

        return "";
    }

    /**
     * 加入指定网络
     *
     * @param SSID   wifi名称
     * @param SSIDPW wifi密码
     */
    private void addnet(String SSID, String SSIDPW) {

        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiConfiguration wifiCong = new WifiConfiguration();
        wifiCong.SSID = "\"" + SSID + "\"";
        wifiCong.preSharedKey = "\"" + SSIDPW + "\"";
        wifiCong.hiddenSSID = true;
        wifiCong.status = WifiConfiguration.Status.ENABLED;
        wifiCong.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wifiCong.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wifiCong.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wifiCong.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wifiCong.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wifiCong.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        wifiCong.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
//        int res = wifiManager.addNetwork(wifiCong);
        wifiManager.enableNetwork(wifiManager.addNetwork(wifiCong), true);
        step1 = true;
    }


    //连接到之前的wifi
    public void connFirstWifi(String SSIDWIFI) {
        Log.e("main", "concWifi===");
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        //判断网卡可不可用
        if (wm.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
            Log.e(TAG, "WIFI_STATE_DISABLED");
            return;
        }

        List<WifiConfiguration> wifiConfigList = wm.getConfiguredNetworks();

        for (int i = 0; i < wifiConfigList.size(); i++) {
            if (wifiConfigList.get(i).SSID.equals("\"" + SSIDWIFI + "\"")) {
                Log.e("main", "ssidiffififififi" + wifiConfigList.get(i).SSID);
                wm.enableNetwork(wifiConfigList.get(i).networkId, true);
            }
        }
    }


    //删除设备的热点信息

    /**
     * 抹掉手机内的指定的wifi信息，如果有就抹掉。
     *
     * @param robotAPSSID 设备的SSID即wifi名称
     */
    private void removeRobotAP(String robotAPSSID) {
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        //判断网卡可不可用
        if (wm.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
            Log.e(TAG, "WIFI_STATE_DISABLED");
            return;
        }
        List<WifiConfiguration> wifiConfigList = wm.getConfiguredNetworks();
        for (int i = 0; i < wifiConfigList.size(); i++) {
            if (wifiConfigList.get(i).SSID.equals("\"" + robotAPSSID + "\"")) {
                Log.e(TAG, "去除robotAP");
                //删除完要保存
                wm.removeNetwork(wifiConfigList.get(i).networkId);
                wm.saveConfiguration();

            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mWifiChangedReceiver);
            Log.e(TAG, "取消广播");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //第一次udp1开始
    private void startUdp() {
        step1 = false;
        Log.e(TAG, "startUdp");
        try {
            firstUdpInetAddress = InetAddress.getByName(udp1Ip);
            firstUdpSocket = new DatagramSocket();
            firstUdpRevWhile = true;
            udp1SendTimeTask();
            SmartLinkActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setProgress(20);
                }
            });
            doFirstUdpSendFun();
            receiveUdp();
            step2 = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    //第一次udp1接收线程
    private void receiveUdp() {
        Log.e(TAG, "receiveUdp");
        firstDataPacket = new DatagramPacket(firstUdpReceiveByte, firstUdpReceiveByte.length);
        new Thread() {
            public void run() {
                while (firstUdpRevWhile) {
                    try {
                        firstUdpSocket.receive(firstDataPacket);
                        int len = firstDataPacket.getLength();
                        if (len > 0) {
                            Log.e("Main", "getSocketAddress=" + firstDataPacket.getSocketAddress() + " getAddress=" + firstDataPacket.getAddress());
                            String receiveStr = bytes2HexString(firstUdpReceiveByte, len);
                            if (doWithFirstUdpRvData(receiveStr)) {
                                firstUdpRevWhile = false;
                                firstUdpHandler.removeCallbacks(firstUdpRunnable);
                                firstUdpBreakDOwhile = true;
                                connFirstWifi(currentWifi);//连接之前的wifi
                                SmartLinkActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar.setProgress(60);
                                    }
                                });
                                reConnBeforWifiFlag = true;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        Log.e(TAG, "第一次udp1接收后的finally，关闭socket");
                        firstUdpSocket.close();
                    }
                }

            }
        }.start();
    }


    //第一次udp1定时发送
    private void udp1SendTimeTask() {
        firstUdpRunnable = new Runnable() {
            @Override
            public void run() {
//                send("firstUDP".getBytes());
                send(getFirstSendData());
                firstUdpSendCount++;
                Log.e(TAG, "定时发送firstUdpSendCount=" + firstUdpSendCount);

                firstUdpHandler.postDelayed(firstUdpRunnable, 1000);
                if (firstUdpSendCount > 3) {
                    firstUdpHandler.removeCallbacks(firstUdpRunnable);

                }
            }
        };
        firstUdpHandler.postDelayed(firstUdpRunnable, 1000);
    }


    //第一次udp发送
    private void send(byte[] data) {
        firstUdpPacketToSend =
                new DatagramPacket(data, data.length,
                        firstUdpInetAddress, Integer.parseInt(udp1Port));

        new Thread() {
            @Override
            public void run() {
                try {
                    firstUdpSocket.send(firstUdpPacketToSend);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    //用于判断第一次udp1发送数据的次数
    //如果等于3次，就关掉socket的接收while
    private void doFirstUdpSendFun() {
        new Thread() {
            @Override
            public void run() {
                while (!firstUdpBreakDOwhile) {
                    if (firstUdpSendCount == 4) {
                        Log.e(TAG, "已经发送3次");
                        firstUdpRevWhile = false;//关掉udp1的socket的接收while
                        connFirstWifi(currentWifi);//连接之前的wifi
                        if (firstUdpSocket != null) {
                            firstUdpSocket.close();
                        }
                        step2 = false;
                        btnTurn = false;
                        SmartLinkActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnStart.setText(BUTTON_STRING_START);
                                btnStart.setBackgroundResource(R.drawable.btn_bg);
                            }
                        });

                        myHandler.sendEmptyMessage(MSG_CONN_FAIL_2);
                        break;
                    }
                }
            }
        }.start();
    }


    //第二次 广播
    //第2次udp开始
    private void startUdp2() {
        step2 = false;
        Log.e(TAG, "startUdp22222222");
        try {
            secondUdpInetAddress = InetAddress.getByName(udp2Ip);
            secondUdpSocket = new DatagramSocket();
            secondUdpRevWhile = true;
            udp2SendTimeTask();
            doFirstUdpSendFun2();
            receiveUdp2();
            step3 = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    //第2次udp1接收线程
    private void receiveUdp2() {
        Log.e(TAG, "receiveUdp22222222");
        secondUdpDataPacket = new DatagramPacket(secondUdpReceiveByte, secondUdpReceiveByte.length);
        new Thread() {
            public void run() {
                while (secondUdpRevWhile) {
                    try {
                        secondUdpSocket.receive(secondUdpDataPacket);
                        int len = secondUdpDataPacket.getLength();
                        if (len > 0) {
                            Log.e("Main", "getSocketAddress2222=" + secondUdpDataPacket.getSocketAddress() + " getAddress2222=" + secondUdpDataPacket.getAddress());
                            String receiveStr = bytes2HexString(secondUdpReceiveByte, len);
                            Log.e(TAG,"receiveStr222222222222222222="+receiveStr+" doWithSecondUdpRvData="+doWithSecondUdpRvData(receiveStr));

                            if (doWithSecondUdpRvData(receiveStr)) {
                                secondUdpRevWhile = false;
                                secondUdpHandler.removeCallbacks(secondUdpRunnable);
                                secondUdpBreakDOwhile = true;
                                btnTurn = false;
                                SmartLinkActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        btnStart.setText(BUTTON_STRING_START);
                                        btnStart.setBackgroundResource(R.drawable.btn_bg);
                                    }
                                });
                                Log.e(TAG, "robot已经连上指定路由器");
                                myHandler.sendEmptyMessage(MSG_CONN_FAIL_4);

                            }

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                    finally {
//                        Log.e(TAG, "第2次udp接收后的finally，关闭socket222");
//                        if(secondUdpSocket!=null){
//                            secondUdpSocket.close();
//                        }
//
//                    }
                }

            }
        }.start();
    }


    //第2次udp1定时发送
    private void udp2SendTimeTask() {
        secondUdpRunnable = new Runnable() {
            @Override
            public void run() {
                send2(seek);
                secondUdpSendCount++;
                Log.e(TAG, "定时发送firstUdpSendCount222=" + secondUdpSendCount);
                secondUdpHandler.postDelayed(secondUdpRunnable, 1000);
                if (secondUdpSendCount > 11) {
                    secondUdpHandler.removeCallbacks(secondUdpRunnable);
                }
            }
        };
        secondUdpHandler.postDelayed(secondUdpRunnable, 1000);
    }


    //第2次udp发送
    private void send2(byte[] data) {
        secondUdpPacketToSend =
                new DatagramPacket(data, data.length,
                        secondUdpInetAddress, Integer.parseInt(udp2Port));

        new Thread() {
            @Override
            public void run() {
                try {
                    secondUdpSocket.send(secondUdpPacketToSend);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    //用于判断第2次udp发送数据的次数
    //如果等于10次，就关掉socket的接收while
    private void doFirstUdpSendFun2() {
        new Thread() {
            @Override
            public void run() {
                while (!secondUdpBreakDOwhile) {
                    if (secondUdpSendCount == 11) {
                        Log.e(TAG, "已经发送11次");
                        secondUdpRevWhile = false;//关掉udp1的socket的接收while
                        secondUdpHandler.removeCallbacks(secondUdpRunnable);
                        if (secondUdpSocket != null) {
                            secondUdpSocket.close();
                        }
                        step3 = false;
                        btnTurn = false;
                        SmartLinkActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnStart.setText(BUTTON_STRING_START);
                                btnStart.setBackgroundResource(R.drawable.btn_bg);
                            }
                        });
//                        handlerDialog.sendEmptyMessage(MSG_CONN_FAIL_3);
                        myHandler.sendEmptyMessage(MSG_CONN_FAIL_3);

                        break;
                    }
                }
            }
        }.start();
    }
    //第二次udp结束


    //数据处理
    //第一次连接到robotAP时发送的数据
    private byte[] getFirstSendData() {

        int ssidl = currentWifi.length();
        int pwl = etSSIDPW.getText().toString().length();

        byte[] byteEtsssid = currentWifi.getBytes();// 11

        byte[] byteEtpw = etSSIDPW.getText().toString().getBytes(); // 2233
        byte[] sendData = new byte[17 + ssidl + pwl];

        sendData[0] = dataHead[0];
        sendData[1] = dataHead[1];

        sendData[2] = dataID[0];
        sendData[3] = dataID[1];
        sendData[4] = dataID[2];
        sendData[5] = dataID[3];
        sendData[6] = dataID[4];
        sendData[7] = dataID[5];
        sendData[8] = dataID[6];
        sendData[9] = dataID[7];
        sendData[10] = dataCommand[0];
        sendData[11] = dataCommand[1];

        sendData[12] = (byte) Integer.parseInt(Integer.toHexString(ssidl), 16);
        sendData[13] = (byte) Integer.parseInt(Integer.toHexString(pwl), 16);


        System.arraycopy(byteEtsssid, 0, sendData, 14, ssidl);
        System.arraycopy(byteEtpw, 0, sendData, 14 + ssidl, pwl);

        String ssidAndPwString = bytes2HexString(byteEtsssid,
                byteEtsssid.length)
                + bytes2HexString(byteEtpw, byteEtpw.length);

        //
        // 数据内容长度
        int dataLength = ssidAndPwString.length();
        int sum = 0;
        for (int i = 0; i < (dataLength / 2); i++) {

            sum = sum
                    + Integer.parseInt(
                    ssidAndPwString.substring(i * 2, 2 + i * 2), 16);
        }
        // 校验位
        String temp = "0" + Integer.toHexString(sum);
        sendData[14 + ssidl + pwl] = (byte) Integer.parseInt(
                temp.substring(temp.length() - 2, temp.length()).toUpperCase(),
                16);

        sendData[15 + ssidl + pwl] = dataTail[0];
        sendData[16 + ssidl + pwl] = dataTail[1];
        return sendData;


    }


    //第一次udp接收的数据处理
    private boolean doWithFirstUdpRvData(String data) {
        boolean isTrue = false;
        if (data.length() > 4) {
            if ("FF".equals(data.substring(0, 2))
                    && "AA".equals(data.substring(2, 4))) {
                if (data.length() > 24) {
                    if ("FF".equals(data.substring(20, 22))
                            && "18".equals(data.substring(22, 24))) {
                        mac = data.substring(4, 20);
                        isTrue = true;
                    }
                }
            }
        }

        return isTrue;

    }


    //第二次udp数据处理
    private boolean doWithSecondUdpRvData(String data) {
        boolean isTrue = false;
        if (data.length() > 4) {
            if ("FF".equals(data.substring(0, 2))
                    && "AA".equals(data.substring(2, 4))) {
                // 01 10
                if (data.length() > 24) {
                    if ("01".equals(data.substring(20, 22))
                            && "10".equals(data.substring(22, 24))) {
                        if (mac.equals(data.substring(4, 20))) {
                            isTrue = true;
                            //数据头 mac 命令类型 数据长度 校验码 数据尾
//                            FFAA 5634001900000000 0110 2A00 2E1600000A05FFFE0000 0000000000000000C3C5 B4C5B4B0B4C5CFB5CDB3 00000000000000000000 0000 32 FF55
                            Log.e(TAG, "保存网关的信息=" + data);
                        }

                    }
                }

            }
        }
        return isTrue;
    }

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


    private void showDialog(String title, String showInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(showInfo);
        builder.setNeutralButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressBar.setProgress(0);
                dialog.dismiss();
            }
        });
        builder.create();
        builder.show();
    }


    private class MyHandler extends Handler {

        public MyHandler(Looper looper){
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {

                showDialog("连接失败", "没有开启SmartLink功能，请长按设备上的SmartLink按钮直到等快闪之后重新尝试，如果多次尝试，请给系统复位");
                SmartLinkActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress(0);
                        removeRobotAP(robotAPSSID);
                        btnStartClick = false;
                        btnStartClick2 = false;
                    }
                });
                if (secondUdpSocket != null) {
                    secondUdpSocket.close();
                }
            }

            if (msg.what == 2) {
                showDialog("连接失败", "请检查是否有别的设备开启过SmartLink功能，请复位系统尝试重新连接到该网络");
                progressBar.setProgress(0);
                removeRobotAP(robotAPSSID);
                btnStartClick = false;
                if (secondUdpSocket != null) {
                    secondUdpSocket.close();
                }
            }

            if (msg.what == 3) {

                showDialog("连接失败", "设备连接失败，请检查输入的账户密码是否正确，或者路由器设备是否已满，设备连接失败");
                progressBar.setProgress(0);
                removeRobotAP(robotAPSSID);
                btnStartClick = false;
                if (secondUdpSocket != null) {
                    secondUdpSocket.close();
                }

            }

            if (msg.what == 4) {

                progressBar.setProgress(100);
                //连接上指定wifi后再保存一次，以防之前输入的wifi密码（按下start时保存的密码）可能错误
                editor.putString(SP_WIFI_PW, etSSIDPW.getText().toString());
                editor.commit();
                showDialog("连接成功", "设备已经连上路由器");
                removeRobotAP(robotAPSSID);
                btnStartClick = false;
                if (secondUdpSocket != null) {
                    secondUdpSocket.close();
                }
            }

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {

                finish();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}


//        Android之Socket的基于UDP传输
//        接收方创建步骤：
//
//        1.  创建一个DatagramSocket对象，并指定监听的端口号
//
//        DatagramSocket socket = new  DatagramSocket (4567);
//
//        2. 创建一个byte数组用于接收
//
//        byte data[] = new byte[1024];
//
//        3. 创建一个空的DatagramPackage对象
//
//        DatagramPackage package = new DatagramPackage(data , data.length);
//
//        4. 使用receive方法接收发送方所发送的数据,同时这也是一个阻塞的方法
//
//        socket.receive(package);
//
//        5. 得到发送过来的数据
//
//        new String(package.getData() , package.getOffset() , package.getLength());
//
//
//        发送方创建步骤：
//
//        1.  创建一个DatagramSocket对象
//
//        DatagramSocket socket = new  DatagramSocket (4567);
//
//        2.  创建一个 InetAddress ， 相当于是地址
//
//        InetAddress serverAddress = InetAddress.getByName("想要发送到的那个IP地址");
//
//        3.  这是随意发送一个数据
//
//        String str = "hello";
//
//        4.  转为byte类型
//
//        byte data[] = str.getBytes();
//
//        5.  创建一个DatagramPacket 对象，并指定要讲这个数据包发送到网络当中的哪个地址，以及端口号
//
//        DatagramPacket  package = new DatagramPacket (data , data.length , serverAddress , 4567);
//
//        6.  调用DatagramSocket对象的send方法 发送数据
//
//        socket . send(package);
//



