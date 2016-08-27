package com.kevin.armcontrolsforwireless.activity;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kevin.armcontrolsforwireless.R;
import com.kevin.armcontrolsforwireless.utils.Constant;
import com.kevin.armcontrolsforwireless.utils.OnReceiveListen;
import com.kevin.armcontrolsforwireless.utils.SingleUdp;
import com.kevin.armcontrolsforwireless.utils.SpHelper;
import com.kevin.armcontrolsforwireless.utils.ToastUtil;
import com.kevin.armcontrolsforwireless.utils.Util;
import com.kevin.armcontrolsforwireless.utils.WaitDialog;

import java.util.List;

public class ArmControlActivity extends AppCompatActivity {

    private TextView tvArmId;
    private TextView tvArmIp;
    private TextView tvArmActs;
    private LinearLayout llBtnActionNumber;
    private EditText etExecute;
    private Button btnExecute;
    private SpHelper spHelper;
    private SingleUdp singleUdp;
    private SwipeRefreshLayout id_swipe_ly;
    private boolean runOnece;
    private int eNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arm_control);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("机械臂控制");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setSubtitle("解锁面");
        }

        tvArmId = (TextView)findViewById(R.id.tvArmId);
        tvArmIp = (TextView)findViewById(R.id.tvArmIp);
        tvArmActs = (TextView)findViewById(R.id.tvArmActs);
        etExecute = (EditText)findViewById(R.id.etExecute);
        btnExecute = (Button)findViewById(R.id.btnExecute);
        llBtnActionNumber = (LinearLayout) findViewById(R.id.llBtnActionNumber);
        id_swipe_ly = (SwipeRefreshLayout)findViewById(R.id.id_swipe_ly);
        spHelper = new SpHelper(this);
        String executeInfo = spHelper.getSpArmActs();
        // TODO: 2016/8/27 这边做一个判断，如果没有数据，需要重新去获取机械臂的信息，然后保存在sp
        //07 00 00 00
        String executePart1 = Util.hexString2binaryString(executeInfo.substring(0, 2));
        String executePart2 = Util.hexString2binaryString(executeInfo.substring(2, 4));
        String executePart3 = Util.hexString2binaryString(executeInfo.substring(4, 6));
        String executePart4 = Util.hexString2binaryString(executeInfo.substring(6, 8));
        String executeComb = executePart4 + executePart3 + executePart2 + executePart1;
        int eNumber = 0;
        for (int i = 0; i < executeComb.length(); i++) {
            if (executeComb.substring(i, i + 1).equalsIgnoreCase("1")) {
                eNumber++;
            }
        }

        tvArmId.setText(spHelper.getSpArmId());
        tvArmIp.setText(spHelper.getSpArmIp());
        tvArmActs.setText(String.valueOf(eNumber));

        setActButtons(executeComb, eNumber, llBtnActionNumber);


        btnExecute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (singleUdp != null) {
                    String exId = etExecute.getText().toString();
                    int n;
                    try {
                        n = Integer.parseInt(exId);
                        if (n > 32 || n < 0) {
                            ToastUtil.customToast(ArmControlActivity.this, "输入的范围过大，请输入小于32的数字");
                            etExecute.setText("");
                            return;
                        }
                        if (n < 16) {
                            singleUdp.send(Util.HexString2Bytes(Constant.SEND_DATA_EXECUTE(spHelper.getSpArmId(), "0" + Integer.toHexString(n)).replace(" ", "")));

                        } else {
                            singleUdp.send(Util.HexString2Bytes(Constant.SEND_DATA_EXECUTE(spHelper.getSpArmId(), Integer.toHexString(n)).replace(" ", "")));

                        }

                    } catch (Exception e) {
                        ToastUtil.customToast(ArmControlActivity.this, "请输入数字！！！");
                        etExecute.setText("");
                    }

                }
            }
        });


        id_swipe_ly.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (TextUtils.isEmpty(spHelper.getSpArmIp())) {
                    ToastUtil.customToast(ArmControlActivity.this, "ip为空");
                    id_swipe_ly.setRefreshing(false);
                } else {
                    if(singleUdp!=null) {
                        runOnece = true;
                        singleUdp.send(Util.HexString2Bytes(Constant.SEND_DATA_SEARCH.replace(" ", "")));
                    }
                }
            }
        });

    }

    private void setActButtons(String executeComb, int eNumber,LinearLayout llBtnActionNumber) {
        if (eNumber != 0) {
            llBtnActionNumber.removeAllViews();
            Button[] btns = new Button[eNumber];
            int linearNumber = eNumber / 3;
            if(eNumber%3!=0){
                linearNumber++;
            }
            //linearLayout数组
            LinearLayout[] linearLayouts = new LinearLayout[linearNumber];
            //循环标志
            int linearSize = 0;


            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 10, 0, 10);

            //按钮的左右间距
            LinearLayout.LayoutParams btnLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            btnLayoutParams.setMargins(10,0,10,0);

            List<Integer> executeInfoList = Util.doWithArmExecuteActionInfoList(executeComb);

            //第一个线性布局初始化
            linearLayouts[0] = new LinearLayout(this);
            linearLayouts[0].setLayoutParams(layoutParams);
            //获取屏幕的宽度
            int w = this.getResources().getDisplayMetrics().widthPixels;
            //按钮初始化
            for (int n = 0; n < eNumber; n++) {
                btns[n] = new Button(this);
                btns[n].setTextColor(this.getResources().getColor(R.color.btnTextColor));
                btns[n].setBackgroundResource(R.drawable.btn_arm_acts_bg);
                btnLayoutParams.width = (int)(w * 0.25);
                //设置按钮宽度
                btns[n].setLayoutParams(btnLayoutParams);
                btns[n].setText(executeInfoList.get(n) + "");
                final int finalN = executeInfoList.get(n);
                btns[n].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(singleUdp!=null){
//                            FFAA1C8100008800 0100 11FF55
                            if(finalN<16){
                                singleUdp.send(Util.HexString2Bytes(Constant.SEND_DATA_EXECUTE(spHelper.getSpArmId(), "0" + Integer.toHexString(finalN)).replace(" ", "")));

                            }else{
                                singleUdp.send(Util.HexString2Bytes(Constant.SEND_DATA_EXECUTE(spHelper.getSpArmId(), Integer.toHexString(finalN)).replace(" ", "")));
                            }
                        }else{
                            ToastUtil.customToast(ArmControlActivity.this, "没有连接，请重新连接！");
                        }

                    }
                });
                //每3个按钮加入一个线性布局
                if((n>=1)&&((n)%3 == 0)&&linearSize<=linearNumber){
                    linearSize++;
                    linearLayouts[linearSize] = new LinearLayout(this);
                    linearLayouts[linearSize].setLayoutParams(layoutParams);
                }
                linearLayouts[linearSize].addView(btns[n]);
            }
            for(int k =0;k<linearNumber;k++){
                llBtnActionNumber.addView(linearLayouts[k]);
            }
        }
    }

    private void analysisData(String data) {
        Log.e("ArmControlActivity","data="+data+" dataLength="+data.length()+" cmd="+data.substring(12,16)+" status="+data.substring(20,22));
        if (Util.checkData2(data)) {

            String cmd = data.substring(12,16);

            if (Constant.CMD_EXECUTE_RESPOND.equalsIgnoreCase(cmd)) {
               String status = data.substring(20,22);
//                FFAA 1C810000 00C9 0100 0200 FF55
//                FFAA 1C810000 00C9 0100 0000 FF55
                if(status.equalsIgnoreCase("00")){
                    ToastUtil.customToast(this, "执行成功");
                    Log.e("ArmControlActivity","00");
                }else if(status.equalsIgnoreCase("01")){
                    ToastUtil.customToast(this,"机械臂忙");
                    Log.e("ArmControlActivity", "01");

                }else if(status.equalsIgnoreCase("02")){
                    ToastUtil.customToast(this,"该区域没有动作");
                    Log.e("ArmControlActivity", "02");

                }

            }else if(Constant.CMD_SEARCH_RESPOND.equalsIgnoreCase(cmd)){

                if(!runOnece){
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        id_swipe_ly.setRefreshing(false);
                    }
                });

                String acts = data.substring(20, 28);
                String executePart1 = Util.hexString2binaryString(acts.substring(0, 2));
                String executePart2 = Util.hexString2binaryString(acts.substring(2, 4));
                String executePart3 = Util.hexString2binaryString(acts.substring(4, 6));
                String executePart4 = Util.hexString2binaryString(acts.substring(6, 8));
                final String executeComb = executePart4 + executePart3 + executePart2 + executePart1;
                int eNumbers = 0;
                for (int i = 0; i < executeComb.length(); i++) {
                    if (executeComb.substring(i, i + 1).equalsIgnoreCase("1")) {
                        eNumbers++;
                    }
                }
                eNumber = eNumbers;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setActButtons(executeComb, eNumber, llBtnActionNumber);
                    }
                });

                runOnece = false;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!TextUtils.isEmpty(spHelper.getSpArmId()) && !TextUtils.isEmpty(spHelper.getSpArmIp())) {
            singleUdp = SingleUdp.getUdpInstance();
            singleUdp.setUdpIp(spHelper.getSpArmIp());
            singleUdp.setUdpRemotePort(Constant.REMOTE_PORT);
            singleUdp.start();
            singleUdp.receiveUdp();
            singleUdp.setOnReceiveListen(new OnReceiveListen() {
                @Override
                public void onReceiveData(byte[] data, int len, @Nullable String remoteIp) {
                    String mData = Util.bytes2HexString(data, len);
                    analysisData(mData);
                }
            });

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

}
