package com.kevin.armcontrolsforwireless.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.kevin.armcontrolsforwireless.R;


/**
 * Created by Administrator
 * on 2016/6/28.
 */
public class ToastUtil {
    private static Toast customToast; //自定义Toast实例
    private static View layout;//自定义布局
    private static TextView textView;//自定义显示文本
    private static Typeface typeface;
    private static Activity mActivity;
    private static CharSequence text;

    /**
     * 自定义Toast
     *
     * @param context  上下文
     * @param showInfo 要显示的文本
     */
    @SuppressLint("InflateParams")
    public static void customToast(Activity context, CharSequence showInfo) {
        mActivity = context;
        text = showInfo;
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (layout == null)
                    layout = LayoutInflater.from(mActivity).inflate(R.layout.layout_custom_toast, null);

                if (typeface == null)
                    typeface = Typeface.createFromAsset(mActivity.getAssets(), "MNJLX.TTF");

                if (textView == null) {
                    textView = (TextView) layout.findViewById(R.id.tvShowInfo);
                    textView.setTypeface(typeface);
                }

                if (customToast == null) {
                    customToast = new Toast(mActivity);
                    textView.setText(text);
                } else {
                    textView.setText(text);
                }

                customToast.setView(layout);
                customToast.show();
            }
        });
    }

    /**
     * 自定义Toast
     *
     * @param context 上下文
     * @param resId   要显示的文本id
     */
    public static void customToast(Activity context, int resId) {
        customToast(context, context.getResources().getText(resId));
    }


}
