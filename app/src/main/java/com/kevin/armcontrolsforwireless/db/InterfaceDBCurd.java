package com.kevin.armcontrolsforwireless.db;

import com.kevin.armcontrolsforwireless.entity.ArmBean;

import java.util.List;

public interface InterfaceDBCurd {

    //添加数据
    void addTempAgvData(String id, String ip, String acts);
    //删除所有数据
    void delALLTempAgvData();
    //得到所有数据
    List<ArmBean> getAllTempAgvData();

}
