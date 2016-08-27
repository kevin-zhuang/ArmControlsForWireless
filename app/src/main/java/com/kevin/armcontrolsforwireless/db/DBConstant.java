package com.kevin.armcontrolsforwireless.db;

/**
 * Created by zhuangbinbin
 * 2016年8月27日 星期六
 */
public class DBConstant {
    public static final String DB_NAME = "miniArm.db";
    public static int DB_VERSION = 1;


    /**
     * 小车临时列表
     */
    public static final String TEMP_AGV_TABLE_NAME = "arm_temp";
    public static final String TEMP_AGV_ID = "arm_id";
    public static final String TEMP_AGV_IP = "arm_ip";
    public static final String TEMP_AGV_ACTS = "arm_acts";

    //创建数据表
    public static final String CREATE_TEMP_AGV_DB_SQL = "create table " + TEMP_AGV_TABLE_NAME + "("
            + TEMP_AGV_ID + " varchar(20) primary key ,"
            + TEMP_AGV_IP + " varchar(20),"
            + TEMP_AGV_ACTS + " varchar(20)"
            + ")";
    //查找所有数据
    public static final String SELECT_ALL_TEMP_AGV_SQL = "select * from " + TEMP_AGV_TABLE_NAME;
    //删除所有数据
    public static final String DEL_ALL_TEMP_AGV_SQL = "delete from " + TEMP_AGV_TABLE_NAME;
    //插入数据
    public static final String INSERT_TEMP_AGV_SQL = "insert into " + TEMP_AGV_TABLE_NAME + "("
            + TEMP_AGV_ID + ","
            + TEMP_AGV_IP +","
            + TEMP_AGV_ACTS +
            ") values (?,?,?)";

}
