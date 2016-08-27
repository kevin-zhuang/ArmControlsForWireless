package com.kevin.armcontrolsforwireless.activity;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.kevin.armcontrolsforwireless.R;
import com.kevin.armcontrolsforwireless.db.DBCurd;
import com.kevin.armcontrolsforwireless.entity.ArmBean;
import com.kevin.armcontrolsforwireless.utils.ArmAdapter;
import com.kevin.armcontrolsforwireless.utils.BroadcastUdp;
import com.kevin.armcontrolsforwireless.utils.Constant;
import com.kevin.armcontrolsforwireless.utils.OnReceiveListen;
import com.kevin.armcontrolsforwireless.utils.SingleUdp;
import com.kevin.armcontrolsforwireless.utils.SpHelper;
import com.kevin.armcontrolsforwireless.utils.ToastUtil;
import com.kevin.armcontrolsforwireless.utils.Util;
import com.kevin.armcontrolsforwireless.utils.WaitDialog;

import java.util.ArrayList;
import java.util.List;

public class ArmListActivity extends AppCompatActivity implements View.OnClickListener {


    private Button btnSearchAgv;
    private Button btnConnectAgv;
    private ListView lvAgv;
    private ArmAdapter armAdapter;
    private List<ArmBean> armList = new ArrayList<>();
    private LinearLayout.LayoutParams params = new
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    private int lastSelect = -1;
    private boolean isSelect = false;
    private int selected = -1;
    private SingleUdp singleUdp;
    private BroadcastUdp broadcastUdp;
    private SpHelper spHelper;
    private Handler handler = new Handler();
    private Runnable broadCastSendRunnable;
    private int sendTimes = 0;
    private DBCurd dbCurd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arm_list);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("机械臂列表");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setSubtitle("主页面");
        }
        dbCurd = DBCurd.getInstance(ArmListActivity.this);
        spHelper = new SpHelper(this);
        btnSearchAgv = (Button) findViewById(R.id.btnSearchAgv);
        btnConnectAgv = (Button) findViewById(R.id.btnConnectAgv);
        btnSearchAgv.setOnClickListener(this);
        btnConnectAgv.setOnClickListener(this);

        lvAgv = (ListView)findViewById(R.id.lvAgv);
        armAdapter = new ArmAdapter(this,armList);
        lvAgv.setAdapter(armAdapter);
        View emptyView = LayoutInflater.from(this).inflate(R.layout.programmed_list_empty_layout, null);
        params.gravity = Gravity.CENTER;
        ((ViewGroup) lvAgv.getParent()).addView(emptyView, params);
        lvAgv.setEmptyView(emptyView);


        lvAgv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                if (lastSelect == position) {
                    isSelect = !isSelect;
                    armAdapter.setSelected(position, isSelect);
                    armAdapter.notifyDataSetChanged();
                } else {
                    isSelect = true;
                    armAdapter.setSelected(position, true);
                    armAdapter.notifyDataSetChanged();
                }

                selected = isSelect ? position : -1;
                lastSelect = position;
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSearchAgv:
                armAdapter.setSelected(selected, false);
                selected = -1;
                isSelect = false;
                armAdapter.notifyDataSetChanged();
                armList.clear();
                dbCurd.delALLTempAgvData();
                armAdapter.notifyDataSetChanged();
                if (broadcastUdp == null) {
                    broadcastUdp = new BroadcastUdp();
                }
                sendTimes = 0;
                broadcastUdp.stop();
                broadcastUdp.init();
                broadcastUdp.send(Util.HexString2Bytes(Constant.SEND_DATA_SEARCH.replace(" ", "")));
                broadCastSendRunnable = new Runnable() {
                    @Override
                    public void run() {
                        sendTimes++;
                        broadcastUdp.send(Util.HexString2Bytes(Constant.SEND_DATA_SEARCH.replace(" ", "")));
                        handler.postDelayed(broadCastSendRunnable, 1000);
                        if (sendTimes == 4) {
                            handler.removeCallbacks(broadCastSendRunnable);
                        }
                    }
                };
                handler.postDelayed(broadCastSendRunnable, 1000);
                broadcastUdp.setReceiveListen(new OnReceiveListen() {
                    @Override
                    public void onReceiveData(byte[] data, int len, String remoteIp) {
                        String da = Util.bytes2HexString(data, len);
                        analysisData(da, remoteIp);
                    }
                });
                WaitDialog.immediatelyDismiss();
                WaitDialog.showDialog(ArmListActivity.this, "正在搜索。。。", Constant.SEARCH_WAIT_DIALOG_TIME, broadcastUdp);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        armList = dbCurd.getAllTempAgvData();
                        armAdapter = new ArmAdapter(ArmListActivity.this, armList);
                        lvAgv.setAdapter(armAdapter);
                        lvAgv.smoothScrollToPosition(armList.size());
                    }
                }, Constant.SEARCH_WAIT_DIALOG_TIME);
                break;
            case R.id.btnConnectAgv:
                if (selected == -1) {
                    ToastUtil.customToast(this, "没有选择！！！");
                } else {

                    final ArmBean armBean = (ArmBean) armAdapter.getItem(selected);
                    spHelper.saveSpAgvId(armBean.getArmId());
                    spHelper.saveSpAgvIp(armBean.getArmIp());
                    spHelper.saveSpAgvActs(armBean.getArmActs());

                    singleUdp = SingleUdp.getUdpInstance();
                    singleUdp.stop();
                    singleUdp = SingleUdp.getUdpInstance();
                    singleUdp.setUdpIp(armBean.getArmIp());
                    singleUdp.setUdpRemotePort(Constant.REMOTE_PORT);
                    singleUdp.start();
                    singleUdp.receiveUdp();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            armAdapter.setSelected(selected, false);
                            armAdapter.notifyDataSetChanged();
                        }
                    });
                    selected = -1;
                    isSelect = false;
                    finish();

                }

                break;
        }
    }

    private void analysisData(String data, String ip) {
        if (Util.checkData(data)&&data.length() == 34) {
            String cmd = data.substring(12,16);
            if (Constant.CMD_SEARCH_RESPOND.equalsIgnoreCase(cmd)) {
                String armId = data.substring(4, 8);
                String acts = data.substring(20, 28);
                dbCurd.addTempAgvData(armId, ip, acts);

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


}
