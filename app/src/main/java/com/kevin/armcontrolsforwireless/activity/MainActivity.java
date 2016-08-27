package com.kevin.armcontrolsforwireless.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.kevin.armcontrolsforwireless.R;
import com.kevin.armcontrolsforwireless.utils.Constant;
import com.kevin.armcontrolsforwireless.utils.OnReceiveListen;
import com.kevin.armcontrolsforwireless.utils.SingleUdp;
import com.kevin.armcontrolsforwireless.utils.SpHelper;
import com.kevin.armcontrolsforwireless.utils.ToastUtil;
import com.kevin.armcontrolsforwireless.utils.Util;
import com.kevin.armcontrolsforwireless.utils.WaitDialog;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Button btnUnlockAgv;
    private SpHelper spHelper;
    private TextView tvCurrentArmId;
    private SingleUdp singleUdp;
    private long exitTime = 0;
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if (drawer != null) {
            drawer.setDrawerListener(toggle);
        }
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }
        spHelper = new SpHelper(this);
        btnUnlockAgv = (Button)findViewById(R.id.btnUnlockAgv);
        tvCurrentArmId = (TextView)findViewById(R.id.tvCurrentArmId);

        btnUnlockAgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    //**************
                    if (spHelper == null || TextUtils.isEmpty(spHelper.getSpArmIp()) || TextUtils.isEmpty(spHelper.getSpArmId())) {
                        ToastUtil.customToast(MainActivity.this, "当前没有选择机械臂，请搜索机械臂");
                    } else {
                        if (singleUdp == null) {
                            singleUdp = SingleUdp.getUdpInstance();
                        } else if (TextUtils.isEmpty(singleUdp.getIpAddress())) {
                            singleUdp.setUdpIp(spHelper.getSpArmIp());
                            singleUdp.setUdpRemotePort(Constant.REMOTE_PORT);
                            singleUdp.start();
                        } else {
                            singleUdp.send(Util.HexString2Bytes(Constant.SEND_DATA_SHAKE.replace(" ", "")));
                            WaitDialog.showDialog(MainActivity.this, "正在解锁", Constant.UNLOCK_WAIT_DIALOG_MAX_TIME, null);
                        }

                    }

                    //**************


            }
        });

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
                    analysisData(mData,remoteIp);
                }
            });
            if (spHelper.getSpArmId() == null) {
                tvCurrentArmId.setText("没有ID");
            } else {
                tvCurrentArmId.setText(spHelper.getSpArmId());
            }

        }

    }


    private void analysisData(String data,String ip) {
        if (Util.checkData(data)&&data.length()==34) {
            String cmd = data.substring(12,16);
            if (Constant.CMD_SEARCH_RESPOND.equalsIgnoreCase(cmd)) {
                WaitDialog.immediatelyDismiss();
                //保存数据
//                FFAA 1C810000 0001 0400 0F000000 00FF55
                String armId = data.substring(4, 8);
                String acts = data.substring(20, 28);
                spHelper.saveSpAgvId(armId);
                spHelper.saveSpAgvIp(ip);
                spHelper.saveSpAgvActs(acts);

                startActivity(new Intent(MainActivity.this, ArmControlActivity.class));
            }
        }
    }

//    @Override
//    public void onBackPressed() {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
//            super.onBackPressed();
//        }
//    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                if (System.currentTimeMillis() - exitTime > 2000) {
                    ToastUtil.customToast(MainActivity.this, "再按一次退出");
                    exitTime = System.currentTimeMillis();
                } else {
                    finish();
                }
            }

            return true;
        }
        return super.onKeyDown(keyCode, event);
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
                    analysisData(mData,remoteIp);
                }
            });
            if (spHelper.getSpArmId() == null) {
                tvCurrentArmId.setText("没有ID");
            } else {
                tvCurrentArmId.setText(spHelper.getSpArmId());
            }

        }


    }



//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_search) {
//            Toast.makeText(this,"search",Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this,ArmListActivity.class));
        } else if (id == R.id.nav_setting) {
//            Toast.makeText(this,"setting",Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this,SmartLinkActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer!=null){
            drawer.closeDrawer(GravityCompat.START);
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(singleUdp!=null){
            singleUdp.stop();
        }
    }
}
