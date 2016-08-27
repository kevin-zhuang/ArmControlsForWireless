package com.kevin.armcontrolsforwireless.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kevin.armcontrolsforwireless.entity.ArmBean;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhuangbinbin
 * on 2016/1/5.
 */
public class DBCurd implements InterfaceDBCurd {

    private static final String TAG = "DBCurd";
    private SQLiteDatabase databaseWrite, databaseRead;

    private static DBCurd instance;

    private DBCurd(Context context) {
        DBHelper dbHelper = new DBHelper(context, DBConstant.DB_NAME, null, DBConstant.DB_VERSION);
        //读和写分开处理
        databaseRead = dbHelper.getReadableDatabase();
        databaseWrite = dbHelper.getWritableDatabase();
    }

    public static DBCurd getInstance(Context context){
        if(instance==null){
            instance = new DBCurd(context);
        }
        return instance;
    }



    @Override
    public void addTempAgvData(String id, String ip, String acts) {
        if (databaseWrite != null) {
            databaseWrite.beginTransaction();//开启事务
            try {

                databaseWrite.execSQL(DBConstant.INSERT_TEMP_AGV_SQL, new Object[]{
                        id, ip, acts
                });
                databaseWrite.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                databaseWrite.endTransaction();
            }
        }
    }

    @Override
    public void delALLTempAgvData() {
        if (databaseWrite != null) {
            databaseWrite.execSQL(DBConstant.DEL_ALL_TEMP_AGV_SQL);
        }
    }

    @Override
    public List<ArmBean> getAllTempAgvData() {
        List<ArmBean> agvBeans = new ArrayList<>();
        ArmBean armBean;
        Cursor cursor;
        if (databaseRead != null) {
            cursor = databaseRead.rawQuery(DBConstant.SELECT_ALL_TEMP_AGV_SQL, null);
            if (cursor.moveToFirst()) {
                do {
                    armBean = new ArmBean();
                    armBean.setArmId(cursor.getString(cursor.getColumnIndex(DBConstant.TEMP_AGV_ID)));
                    armBean.setArmIp(cursor.getString(cursor.getColumnIndex(DBConstant.TEMP_AGV_IP)));
                    armBean.setArmActs(cursor.getString(cursor.getColumnIndex(DBConstant.TEMP_AGV_ACTS)));
                    agvBeans.add(armBean);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return agvBeans;
    }

}
