package com.kevin.armcontrolsforwireless.entity;

import java.io.Serializable;

/**
 * Created by Administrator
 * on 2016/6/29.
 */
public class ArmBean implements Serializable{

    private String armId;
    private String armIp;
    private String armActs;


    public String getArmActs() {
        return armActs;
    }

    public void setArmActs(String armActs) {
        this.armActs = armActs;
    }

    public String getArmIp() {
        return armIp;
    }

    public void setArmIp(String armIp) {
        this.armIp = armIp;
    }

    public String getArmId() {
        return armId;
    }

    public void setArmId(String armId) {
        this.armId = armId;
    }




}
