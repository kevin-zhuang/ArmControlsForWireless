package com.kevin.armcontrolsforwireless.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Author zhuangbinbin
 * 2016年8月27日 星期六
 */
public class SpHelper {


    private final String spFileName = "miniArm";
    private final String spArmId = "armId";
    private final String spArmIp = "armIp";
    private final String spArmActs = "armActs";

    private SharedPreferences armShared;
    private SharedPreferences.Editor armEditor;

    public SpHelper(Context context) {
        armShared = context.getSharedPreferences(
                spFileName, Activity.MODE_PRIVATE);
    }

    //armId
    public String getSpArmId() {
        return armShared.getString(spArmId, null);
    }

    public void saveSpAgvId(String armId) {
        armEditor = armShared.edit();
        armEditor.putString(spArmId, armId);
        armEditor.apply();

    }


    //armIp
    public String getSpArmIp() {
        return armShared.getString(spArmIp, null);
    }

    public void saveSpAgvIp(String armIp) {
        armEditor = armShared.edit();
        armEditor.putString(spArmIp, armIp);
        armEditor.apply();

    }


    //armActs
    public String getSpArmActs() {
        return armShared.getString(spArmActs, null);
    }

    public void saveSpAgvActs(String armAtcs) {
        armEditor = armShared.edit();
        armEditor.putString(spArmActs, armAtcs);
        armEditor.apply();

    }





}
