package com.kevin.armcontrolsforwireless.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kevin.armcontrolsforwireless.R;
import com.kevin.armcontrolsforwireless.entity.ArmBean;

import java.util.List;


/**
 * Created by Administrator
 * on 2016/6/29.
 */
public class ArmAdapter extends BaseAdapter{

    private Context mContext;
    private List<ArmBean> mList;
    private int selected = -1;
    private boolean isSelect;

    public ArmAdapter(Context context, List<ArmBean> list){
        mContext = context;
        mList = list;
    }

    public void setSelected(int positioned,boolean isSelect){
        this.selected = positioned;
        this.isSelect = isSelect;
    }


    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ArmBean armBean = mList.get(position);
        ViewHolder viewHolder;
        View view;
        if(convertView==null){
            viewHolder = new ViewHolder();
            view  = LayoutInflater.from(mContext).inflate(R.layout.agv_list_item,null);
            viewHolder.tvNumber = (TextView)view.findViewById(R.id.tvNumber);
            viewHolder.tvAgvId = (TextView)view.findViewById(R.id.tvAgvId);
            viewHolder.rlAgvItem = (RelativeLayout)view.findViewById(R.id.rlAgvItem);
            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (ViewHolder)view.getTag();
        }

        viewHolder.tvAgvId.setText(armBean.getArmId());
        viewHolder.tvNumber.setText(String.valueOf(position+1));

        if(selected == position){
            if(isSelect){
                viewHolder.rlAgvItem.setBackgroundResource(R.drawable.agv_list_item_press_bg);
            }else{
                viewHolder.rlAgvItem.setBackgroundResource(R.drawable.agv_list_item_bg);
            }
            
        }else{
            viewHolder.rlAgvItem.setBackgroundResource(R.drawable.agv_list_item_bg);
        }

        return view;
    }

    static class ViewHolder{
        TextView tvNumber;
        TextView tvAgvId;
        RelativeLayout rlAgvItem;
    }

}
