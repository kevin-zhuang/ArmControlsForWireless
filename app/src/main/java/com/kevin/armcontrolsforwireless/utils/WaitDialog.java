package com.kevin.armcontrolsforwireless.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;

/**
 * Created by Administrator
 * on 2016/6/23.
 */
public class WaitDialog {

    private static final String TAG = "WaitDialog";
    private static ProgressDialog progressDialog;
    private static Runnable runnable;
    private static Handler handler = new Handler();

    public static void showDialog(Context context, String message, int disappearTime,@Nullable final BroadcastUdp broadcastUdp) {

        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.show();
        runnable = new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                if(broadcastUdp!=null){
                    broadcastUdp.stop();
                }
            }
        };

        handler.postDelayed(runnable, disappearTime);
    }

    public static void immediatelyDismiss() {
        if (progressDialog != null && handler != null && runnable != null) {
            progressDialog.dismiss();
            handler.removeCallbacks(runnable);
        }
    }


}
